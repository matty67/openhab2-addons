/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lupus;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link LupusConnection} is responsible for managing the connection for the Contact ID protocol
 * and getting the inital data values from the XT2.
 *
 * @author Martin Schlaak - Initial contribution
 */
public class LupusConnection {

    private final Logger logger = LoggerFactory.getLogger(LupusConnection.class);
    public static final String MODE_A1 = "mode_a1";
    public static final String MODE_A2 = "mode_a2";
    public static final String STATE_A1 = "state_a1";
    public static final String STATE_A2 = "state_a2";
    public static final String STATE_A1_MSG = "state_a1_msg";
    public static final String STATE_A2_MSG = "state_a2_msg";

    private String hostname, user, pass;
    JsonObject currentValues;

    public LupusConnection(String hostname, String user, String pass) {
        this.hostname = hostname;
        this.user = user;
        this.pass = pass;
    }

    private boolean getValues() throws Exception {
        try {
            String stringUrl = "https://" + hostname + "/action/panelCondGet";
            URL url = new URL(stringUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new AlwaysTrustManager() }, null);
            SSLSocketFactory factory = ctx.getSocketFactory();
            ((HttpsURLConnection) connection).setSSLSocketFactory(factory);
            ((HttpsURLConnection) connection).setHostnameVerifier(new TrustingHostnameVerifier());

            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    /*
                     * System.out.printf("url=%s, host=%s, ip=%s, port=%s%n", getRequestingURL(), getRequestingHost(),
                     * getRequestingSite(), getRequestingPort());
                     */

                    return new PasswordAuthentication(user, pass.toCharArray());
                }
            });

            Reader reader = new InputStreamReader(connection.getInputStream());
            JsonElement elem = new JsonParser().parse(reader);
            JsonObject object = elem.getAsJsonObject();
            if (object.has("updates")) {
                currentValues = object.getAsJsonObject("updates");
            }
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    class TrustingHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    class AlwaysTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private long last = 0;

    public String getCurrentModes() {
        try {
            if (System.currentTimeMillis() > last + 1000) {
                getValues();
                last = System.currentTimeMillis();
            }
        } catch (Exception e) {
            logger.info("error gettings modes", e);
        }
        return "";
    }

    public Integer getIntValue(String channel) {
        try {
            if (currentValues != null && currentValues.has(channel)) {
                String val = currentValues.get(channel).getAsString();
                if (channel.equals(LupusConnection.MODE_A1) || channel.equals(LupusConnection.MODE_A2)) {
                    String regex = "\\{AREA_MODE_(\\d)\\}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(val);
                    if (matcher.find() && matcher.groupCount() == 1) {
                        int iRet = Integer.parseInt(matcher.group(1));
                        return iRet;
                    }
                }
            }
        } catch (Exception ex) {
        }
        return -1;
    }

}
