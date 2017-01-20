/*
 * Copyright (c) 2017 Enigma Bridge Ltd.
 *
 * This file is part of the GlobalPlatformProREST project.
 *
 *     GlobalPlatformProREST is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GlobalPlatformProREST is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GlobalPlatformProREST.  If not, see <http://www.gnu.org/licenses/>.
 *
 *     If you have any support question, use the GitHub facilities. Visit http://enigmabridge.com
 *     if you want to speak to us directly.
 */

package com.enigmabridge.restgppro.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Enigma Bridge Ltd (dan) on 17/01/2017.
 */
public class GlobalConfiguration {
    private final static byte STATUS_CARDID = 0x40;
    private final static byte STATUS_KEYPAIR = 0x41;
    private final static byte STATUS_EPHEMERAL = 0x42;
    private final static byte STATUS_MEMORY = 0x43;


    private static String instanceFolder = null;
    private static String protocolFolder = null;
    private static boolean readerUse = false;
    private static String readerIO = null;
    private static LinkedList<String> readerSet = null;
    private static boolean simonaUse;
    private static String simonaIO;
    private static LinkedList<String> simonaSet;
    private static LinkedList<String> emptyReaders = new LinkedList<>();
    private static LinkedList<String> readers = new LinkedList<>();
    private static HashMap<String, LinkedList<String>> simonaReaders = new HashMap<>();
    private static ConcurrentHashMap<String, HashMap<String, AppletStatus>> cards = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, LinkedList<AppletStatus>> applets = new ConcurrentHashMap<>();

    public static void setProtocolFolder(String path) {
        GlobalConfiguration.protocolFolder = path;
    }

    public static void setInstanceFolder(String instanceFolder) {
        GlobalConfiguration.instanceFolder = instanceFolder;
    }

    public static void setReaderUse(boolean readerScanning) {
        GlobalConfiguration.readerUse = readerScanning;
    }

    public static String getReaderIO() {
        return GlobalConfiguration.readerIO;
    }

    public static void setReaderIO(String readerIO) {
        GlobalConfiguration.readerIO = readerIO;
    }

    public static void setReaderSet(LinkedList<String> readerSet) {
        GlobalConfiguration.readerSet = readerSet;
    }

    public static void setSimonaUse(boolean simonaUse) {
        GlobalConfiguration.simonaUse = simonaUse;
    }

    public static String getSimonaIO() {
        return GlobalConfiguration.simonaIO;
    }

    public static void setSimonaIO(String simonaIO) {
        GlobalConfiguration.simonaIO = simonaIO;
    }

    public static void setSimonaSet(LinkedList<String> simonaSet) {
        GlobalConfiguration.simonaSet = simonaSet;
    }

    public static LinkedList<String> getSimonaIPs() {
        return simonaSet;
    }

    public static void addEmptyReader(String emptyReader) {
        emptyReaders.add(emptyReader);
    }

    public static void addReader(String reader) {
        readers.add(reader);
    }

    public static void addSimonaReader(String ip, String reader) {
        simonaReaders.putIfAbsent(ip, new LinkedList<>());
        simonaReaders.get(ip).add(reader);
    }

    public static LinkedList<String> getReaders() {

        return readers;
    }

    public static void addApplet(String reader, String aid, AppletStatus status) {
        cards.putIfAbsent(reader, new HashMap<>());
        cards.get(reader).put(aid, status);

        applets.putIfAbsent(aid, new LinkedList<>());
        applets.get(aid).add(status);
    }

    public static void updateAppletStatus(String reader, String aid, int status) {
        if (cards.containsKey(reader)) {
            if (cards.get(reader).containsKey(aid)) {
                cards.get(reader).get(aid).setStatus(status);
            }
        }
    }

    public static HashMap<String, AppletStatus> getCardApplets(String reader) {
        if (cards.containsKey(reader)) {
            return cards.get(reader);
        } else {
            return null;
        }
    }

    public static LinkedList<String> getSimonaReaders(String simona) {
        LinkedList<String> result = simonaReaders.get(simona);
        if (result == null) {
            result = new LinkedList<>();
        }
        return result;
    }

    public static LinkedList<String> getCardAppletsIDs() {
        LinkedList<String> result = new LinkedList<>();
        for (String test : applets.keySet()) {
            result.add(test);
        }
        return result;
    }

    public static LinkedList<AppletStatus> getAppletInstaces(String keys) {
        return applets.get(keys);
    }

    public static String getStatusAPDU() {
        String statusAPDU = "B0020000";
        return statusAPDU;
    }

    /**
     * @param apdu
     * @return 0 -> not MPC
     * 1 -> reset
     * 2 -> busy
     * -- for future
     * 2 -> hashes exchanged - ephemeral
     * 4 -> keys combined  - ephemeral
     * 8 -> hashes exchanged - permanent
     * 16 -> keys combined  - - permanent
     */
    public static int parseAppletStatus(AppletStatus applet, String apdu) {
        int status = 0;

        if (((apdu.length() % 2) == 0) && (apdu.length() > 4)) {
            final byte result[] = new byte[apdu.length() / 2];
            final char enc[] = apdu.toCharArray();
            for (int i = 0; i < enc.length; i += 2) {
                StringBuilder curr = new StringBuilder(2);
                curr.append(enc[i]).append(enc[i + 1]);
                result[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
            }
            int counter = 0;
            int length = 0;
            if (result[counter] == STATUS_CARDID) {
                length = (result[counter + 1] & 0xff) * 256 + (result[counter + 2] & 0xff);
                counter = counter + 3;
                applet.setAppletID(result, counter, length);
                counter += length;
                if (result[counter] == STATUS_KEYPAIR) {
                    counter += 3;
                    int stat = (result[counter] & 0xff) * 256 + (result[counter + 1] & 0xff);
                    if (stat == 65535) {
                        status = 1;
                    } else {
                        status = 2;
                    }
                    counter += 2;
                    if (result[counter] != STATUS_EPHEMERAL) {
                        status = 0;
                    } else {
                        counter += 5;
                        if (result[counter] != STATUS_MEMORY) {
                            status = 0;
                        } else {
                            length = (result[counter + 1] & 0xff) * 256 + (result[counter + 2] & 0xff);
                            if (length != 6) {
                                status = 0;
                            } else {
                                counter += 3;
                                stat = (result[counter] & 0xff) * 256 + (result[counter + 1] & 0xff);
                                applet.setMemoryPersistent(stat);
                                stat = (result[counter + 2] & 0xff) * 256 + (result[counter + 3] & 0xff);
                                applet.setMemoryReset(stat);
                                stat = (result[counter + 4] & 0xff) * 256 + (result[counter + 5] & 0xff);
                                applet.setMemoryDeselect(stat);

                            }
                        }
                    }
                }
            }
        }
        applet.setStatus(status);
        return status;
    }

    public static String getSelectCommand(String keys) {
        String result = "00A40400";
        if (keys.length() < 32) {
            result += "0";
        }
        result += Integer.toHexString(keys.length() / 2) + keys;
        return result;
    }
}
