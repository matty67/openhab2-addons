/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lupus.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lupus.LupusBindingConstants;
import org.openhab.binding.lupus.LupusConnection;
import org.openhab.binding.lupus.LupusMessages;
import org.openhab.binding.lupus.LupusMessages.LupusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LupusHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Martin Schlaak - Initial contribution
 */
public class LupusHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LupusHandler.class);
    private ServerSocket server;
    private ArrayList<CIDClient> clients = new ArrayList<>();
    private LupusConnection lupusConnection;
    Thread serverThread;

    public LupusHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        switch (id) {
            case LupusBindingConstants.CHANNEL_MODE_A1:
            case LupusBindingConstants.CHANNEL_MODE_A2:
                updateModes();
                break;
            case LupusBindingConstants.CHANNEL_STATE_A1:
            case LupusBindingConstants.CHANNEL_STATE_A1_MSG:
                updateState(1, 0);
                break;
            case LupusBindingConstants.CHANNEL_STATE_A2:
            case LupusBindingConstants.CHANNEL_STATE_A2_MSG:
                updateState(2, 0);
                break;
        }
    }

    @Override
    public void initialize() {
        if (thing.getConfiguration().keySet().size() == 4) {
            // Direct connection to Lupus XT2
            String hostname = (String) thing.getConfiguration().get("hostname");
            String user = (String) thing.getConfiguration().get("user");
            String pass = (String) thing.getConfiguration().get("pass");
            int port = ((BigDecimal) thing.getConfiguration().get("port")).intValue();
            port = (port == 0 ? 10508 : port);

            if (hostname != null && user != null) {
                Runnable myLupus = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            lupusConnection = new LupusConnection(hostname, user, pass);
                            updateModes();
                        } catch (Exception e) {
                            logger.info("lupus connect error", e);
                        }
                    }
                };
                Thread thConnection = new Thread(myLupus);
                thConnection.start();
                try {
                    server = new ServerSocket(port);
                    Runnable myServer = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                while (!server.isClosed()) {
                                    Socket client = server.accept();
                                    CIDClient cidClient = new CIDClient(client);
                                    clients.add(cidClient);
                                    cidClient.start();
                                }
                            } catch (SocketException e) {
                                if (!e.getMessage().toLowerCase().equals("socket closed")) {
                                    logger.warn("lupus client socketerror", e);
                                }
                            } catch (Exception e) {
                                logger.warn("lupus client error", e);
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
                            }
                        }
                    };
                    serverThread = new Thread(myServer);
                    serverThread.start();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                    logger.info("lupus server error", e);
                }
            }
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        // Stop all running clients and the server
        if (serverThread != null && server != null) {
            try {
                server.close();
                serverThread.join(2000);
            } catch (Exception e) {
                logger.info("dispose error", e);
            }
        }
        for (CIDClient client : clients) {
            client.terminate();
            try {
                client.join(2000);
            } catch (Exception e) {
            }
        }
        super.dispose();
    }

    private void updateModes() {
        if (lupusConnection != null) {
            lupusConnection.getCurrentModes();
            updateState(LupusBindingConstants.CHANNEL_MODE_A1,
                    new DecimalType(lupusConnection.getIntValue(LupusBindingConstants.CHANNEL_MODE_A1)));
            updateState(LupusBindingConstants.CHANNEL_MODE_A2,
                    new DecimalType(lupusConnection.getIntValue(LupusBindingConstants.CHANNEL_MODE_A2)));
        }
    }

    private void updateState(int area, int eventcode) {
        updateState(area, eventcode, null);
    }

    private void updateState(int area, int eventcode, String msg) {
        State sState = null;
        if (msg != null) {
            sState = new StringType(msg);
        }
        switch (area) {
            case 1:
                updateState(LupusBindingConstants.CHANNEL_STATE_A1, new DecimalType(eventcode));
                if (sState != null) {
                    updateState(LupusBindingConstants.CHANNEL_STATE_A1_MSG, sState);
                }
                break;
            case 2:
                updateState(LupusBindingConstants.CHANNEL_STATE_A2, new DecimalType(eventcode));
                if (sState != null) {
                    updateState(LupusBindingConstants.CHANNEL_STATE_A2_MSG, sState);
                }
                break;
        }
    }

    // Receives Contact ID message from connected Lupus XT2
    class CIDClient extends Thread {
        Socket client;
        OutputStream out;
        InputStream in;
        CID cid;

        public CIDClient(Socket client) throws IOException {
            this.client = client;
            this.client.setTcpNoDelay(true);
            out = client.getOutputStream();
            in = client.getInputStream();
        }

        // @Override
        public void terminate() {
            try {
                client.close();
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            boolean bStart = false;
            try {
                StringBuilder sb = new StringBuilder();
                while (client.isConnected()) {
                    int c = in.read();
                    if (c == -1) {
                        break;
                    }
                    if ((char) c == '[') {
                        bStart = true;
                        client.setSoTimeout(5000);
                    }
                    if (bStart) {
                        sb.append((char) c);
                    }
                    if ((char) c == ']') {
                        bStart = false;
                        // ACK als Bestätigung zurück schreiben
                        out.write(6);
                        cid = new CID(sb.toString());
                        updateModes();
                        String msg = cid.getMessage().message();
                        updateState(cid.getArea(), cid.getEventCode(), msg);
                        sb = new StringBuilder();
                        client.setSoTimeout(120000);
                    }
                }
            } catch (SocketTimeoutException se) {
                try {
                    client.close();
                } catch (IOException e1) {
                }
            } catch (Exception e) {
                logger.info("cid client error", e);
            }
        }
    }

    // Holds the Contact ID data
    class CID {
        Pattern pattern = Pattern.compile("(\\[(\\d{4})(\\s)([1][8])([123])([0-9a-fA-F]{3})([012]{2})(\\d{3}).*\\])");
        private String acct;
        private String mt;
        private String q;
        private String xyz;
        private String gg;
        private String c1;
        LupusMessage message;

        public CID(String msg) {
            parse(msg);
        }

        public LupusMessage getMessage() {
            return message;
        }

        public int getArea() {
            try {
                return Integer.parseInt(gg);
            } catch (Exception e) {
            }
            return -1;
        }

        public int getEventCode() {
            try {
                return Integer.parseInt(xyz, 16);
            } catch (Exception e) {
            }
            return -1;
        }

        public void parse(String msg) {
            Matcher matcher = pattern.matcher(msg);
            if (matcher.matches() && matcher.groupCount() == 8) {
                // ACCT MT QXYZ GG C1 C2 C3
                acct = matcher.group(2);
                mt = matcher.group(4);
                q = matcher.group(5);
                xyz = matcher.group(6);
                message = LupusMessages.getMessage(Integer.parseInt(xyz));
                gg = matcher.group(7);
                c1 = matcher.group(8);
            }
        }
    }
}
