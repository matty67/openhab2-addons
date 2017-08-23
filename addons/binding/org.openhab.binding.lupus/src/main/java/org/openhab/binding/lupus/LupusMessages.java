/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lupus;

import java.util.HashMap;

/**
 * The {@link LupusMessages} is responsible for holding the messages.
 *
 * @author Martin Schlaak - Initial contribution
 */
public class LupusMessages {

    static HashMap<Integer, LupusMessage> hmap = null;
    static LupusMessages fac = new LupusMessages();

    public static LupusMessage getMessage(int id) {
        LupusMessage ret = fac.getUnknown();
        synchronized (fac) {
            if (hmap == null) {
                hmap = new HashMap<>();
                for (int i = 0; i < fac.messages.length; i++) {
                    LupusMessage message = fac.messages[i];
                    hmap.put(message.id, message);
                }
            }
            Integer key = new Integer(id);
            if (hmap.containsKey(key)) {
                ret = hmap.get(key);
            }
        }
        return ret;
    }

    private LupusMessage getUnknown() {
        return new LupusMessage(-1, "Unbekannt", "Unbekannte Meldung");
    }

    LupusMessage[] messages = {
            new LupusMessage(100, "Medizinischer Alarm", "Melder mit Eigenschaft „Medizinischer Alarm“"),
            new LupusMessage(101, "Notfallalarm", "Medizinischer Alarmmelder, Panic Button"),
            new LupusMessage(110, "Feueralarm", "Melder mit Eigenschaft „Feueralarm“"),
            new LupusMessage(111, "Rauchalarm", "Rauchmelder"), new LupusMessage(114, "Hitzealarm", "Hitzemelder"),
            new LupusMessage(120, "Überfallalarm", "Panikknopf auf Fernbedienung"),
            new LupusMessage(121, "Nötigungsalarm", "Überfallcode auf Keypad"),
            new LupusMessage(122, "Stiller Alarm", "Melder mit Eigenschaft „Stiller Alarm“"),
            new LupusMessage(130, "Einbruchalarm",
                    "Melder mit der Eigenschaft „Einbruchalarm Instant“ oder „Einbruchalarm Follow“"),
            new LupusMessage(131, "Einbruchalarm (Perimeter)",
                    "Melder mit Eigenschaft „Eingangsverzögerung“ im Arm Modus ausgelöst."),
            new LupusMessage(132, "Einbruchalarm (Innenbereich)",
                    "Melder mit Eigenschaft „Eingangsverzögerung“ im Home Modus ausgelöst."),
            new LupusMessage(136, "Einbruchalarm (Außenbereich)", "Melder mit Eigenschaft „Einbruchalarm Outdoor“"),
            new LupusMessage(147, "Sensorausfall",
                    "Wenn Supervisionüberprüfung bei einem Sensor fehlschlägt oder wiederhergestellt wird"),
            new LupusMessage(151, "Gasalarm", "Melder mit Eigenschaft „Gasalarm“"),
            new LupusMessage(154, "Wasseralarm", "Wassermelder"),
            new LupusMessage(158, "Zu hohe Temperatur", "Zu hoher Temperaturalarm ausgelöst"),
            new LupusMessage(159, "Zu niedrige Temperatur", "Zu niedrige Temperaturalarm ausgelöst"),
            new LupusMessage(162, "CO-Alarm", "CO-Melder STATUSMELDUNGEN"),
            new LupusMessage(301, "Stromverlust",
                    "Stromausfall für mehr als 10 Sekunden / Stromversorgung wiederhergestellt"),
            new LupusMessage(302, "XT2 (Plus) Batterie schwach",
                    "Batteriespannung der Zentrale schwach / Batteriespannung wiederhergestellt"),
            new LupusMessage(311, "XT2 (Plus) Batterie defekt",
                    "Batterie der Zentrale entfernt bzw. abgeschaltet / Batterie wieder verfügbar"),
            new LupusMessage(344, "Funkstörung", "Funk gestört / wiederhergestellt"),
            new LupusMessage(374, "Arm trotz Fehler", "Scharfschaltung obwohl ein Fehlerzustand besteht."),
            new LupusMessage(383, "Sensorsabotage", "Sabotagekontakt eines Sensors ausgelöst / wiederhergestellt"),
            new LupusMessage(384, "Sensorbatterie niedrig",
                    "Batteriespannung eines Sensors niedrig / wiederhergestellt"),
            new LupusMessage(389, "Selbsttest Fehler", "Allgemeine Störungen der Zentrale ? Neustart MODUSMELDUNGEN"),
            new LupusMessage(400, "Scharf-/Unscharfschaltung Fernbedienung",
                    "Scharfschaltung oder Unscharfschaltung per Fernbedienung"),
            new LupusMessage(401, "Scharf-/Unscharfschaltung User",
                    "Scharfschaltung oder Unscharfschaltung per Web, App oder SMS"),
            new LupusMessage(407, "Scharf-/Unscharfschaltung Keypad",
                    "Scharfschaltung oder Unscharfschaltung per Keypad"),
            new LupusMessage(408, "Scharf-/Unscharfschaltung Set/Unset",
                    "Scharfschaltung oder Unscharfschaltung per Sensor mit „Set/Unset“-Option"),
            new LupusMessage(456, "Homemodus", "Homemodusaktivierung"),
            new LupusMessage(465, "Alarm Reset", "Panik Alarm wurde gestoppt durch Panicbutton"),
            new LupusMessage(602, "Periodischer Test", "Zentrale führt periodischen Test durch HAUSAUTOMATION"),
            new LupusMessage(760, "Hausautomationsregel ausgeführt", "Eine Hausautomationsregel") };

    public class LupusMessage {
        int id;
        String msg, msgDetail;

        public LupusMessage(int id, String msg, String msgDetail) {
            this.id = id;
            this.msg = msg;
            this.msgDetail = msgDetail;
        }

        @Override
        public String toString() {
            return "ID: " + id + "  Message: " + msg + " Detail: " + msgDetail;
        }

        public String message() {
            return msg;
        }

        public String detail() {
            return msgDetail;
        }

    }

}
