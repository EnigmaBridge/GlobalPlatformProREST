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

import com.enigmabridge.restgppro.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPTool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalConfiguration {

    public static final Logger LOG = LoggerFactory.getLogger(Application.class);


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
    private static ConcurrentHashMap<String, LinkedList<AppletStatus>> appletsReady = new ConcurrentHashMap<>();
    private static HashMap<String, ProtocolDefinition> protocols = new HashMap<>();
    private static HashMap<String, ProtocolInstance> runs = new HashMap<>();
    private static LinkedList<AppletStatus> appletsError = new LinkedList<>();

    private static ConcurrentHashMap<String, GPTool> terminals = new ConcurrentHashMap<>();

    public static boolean getReaderUse() {
        return GlobalConfiguration.readerUse;
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

    public static synchronized void addApplet(String reader, String aid, AppletStatus status) {


        status.setAID(aid);
        cards.putIfAbsent(reader, new HashMap<>());
        cards.get(reader).put(aid, status);

        applets.putIfAbsent(aid, new LinkedList<>());
        appletsReady.putIfAbsent(aid, new LinkedList<>());
        applets.get(aid).add(status);
        // keep a separate list of applets ready for allocation -- this will be always false
        // we first need to check the applet status
//        if (status.getStatus() == AppletStatus.Status.READY) {
//            appletsReady.get(aid).add(status);
//        }
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

    public static LinkedList<AppletStatus> getAppletInstances(String keys) {
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
        if (applet.getStatus() == AppletStatus.Status.READY) {
            appletsReady.get(applet.getAID()).add(applet);
        } else {
            if (applet.getStatus() != AppletStatus.Status.BUSY) {
                appletsError.add(applet);
            }
        }
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

    public static String getInstanceFolder() {
        return instanceFolder;
    }

    public static void setInstanceFolder(String instanceFolder) {
        GlobalConfiguration.instanceFolder = instanceFolder;
    }

    public static void addProtocol(String protocolName, ProtocolDefinition prot) {
        protocols.put(protocolName.toLowerCase(), prot);
    }

    public static void addInstance(String uid, ProtocolInstance prot) {
        runs.put(uid, prot);
    }

    public static boolean isProtocol(String protocol) {
        return (protocols.containsKey(protocol.toLowerCase()));
    }

    public static synchronized LinkedList<AppletStatus> getFreeApplets(String protocol, int size, String instance) {
        LinkedList<AppletStatus> cards = new LinkedList<>();

        if (isProtocol(protocol)) {
            String aid = getProtocolAID(protocol);
            if (appletsReady.containsKey(aid)) {
                if ((appletsReady.containsKey(aid)) && (appletsReady.get(aid).size() >= size)) {
                    for (int counter = 0; counter < size; counter++) {
                        AppletStatus one = appletsReady.get(aid).remove(0);
                        cards.add(one);
                        one.setBusy(instance);
                    }
                } else {
                    cards = null;
                }
            }
        } else {
            cards = null;
        }

        return cards;
    }

    static String getProtocolAID(String protocol) {
        if (isProtocol(protocol)) {
            return protocols.get(protocol.toLowerCase()).getAID();
        } else {
            return null;
        }
    }

    public static String getProtocolFolder() {
        return protocolFolder;
    }

    public static void setProtocolFolder(String path) {
        GlobalConfiguration.protocolFolder = path;
    }

    public static int getReadyCardsNumber(String protocolName) {
        if (isProtocol(protocolName)) {
            String aid = getProtocolAID(protocolName);
            if (appletsReady.get(aid) == null) {
                return -1;
            } else {
                return appletsReady.get(aid).size();
            }
        } else {
            return -1;
        }
    }

    public static boolean InitializeInstance(ProtocolInstance prot) {

        if (isProtocol(prot.getProtocolName())) {
            // lets first find the instruction
            ProtocolDefinition.Instruction ins = GlobalConfiguration.getProtocolInitCommand(prot.getProtocolName());

            prot.queue = new LinkedBlockingDeque<>();
            prot.executor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS, prot.queue);
            // init is always from "host" to all smartcards
            for (String playerID : prot.getCardKeys()) {
                Pair<AppletStatus, Integer> player = prot.getCard(playerID);
                // let's now create a command
                String apduString = ins.cls + ins.ins;

                String p1 = prot.ReplacePx(ins.p1, player.getR(), -1);
                if (p1 == null) {
                    return false;
                } else {
                    apduString += p1;
                }
                String p2 = prot.ReplacePx(ins.p2, player.getR(), -1);
                if (p2 == null) {
                    return false;
                } else {
                    apduString += p2;
                }
                if (ins.data != null) {
                    String data = prot.ReplaceData(ins.data, player.getR(), -1);
                    String dataLen = Integer.toHexString(data.length() / 2);
                    if (dataLen.length() == 1) {
                        dataLen = "0" + dataLen;
                    }
                    apduString += dataLen + data;
                }

                // and now we should call the smartcard
                String[] apduArray = new String[1];
                apduArray[0] = apduString;
                RunnableRunAPDU oneSC = new RunnableRunAPDU(player.getL().getAID(), player.getL(), player.getR(),
                        apduArray, true);
                prot.executor.execute(oneSC);

            }
            prot.executor.shutdown();
            try {
                prot.executor.awaitTermination(50, TimeUnit.SECONDS);
                return true;
            } catch (InterruptedException e) {
                return false;
            }

        } else {
            return false;
        }
    }

    private static String ReplacePx(String p1) {
        return null;
    }

    private static ProtocolDefinition.Instruction getProtocolInitCommand(String protocolName) {
        return protocols.get(protocolName).getInitInstruction();
    }

    public static ProtocolInstance isInstance(String instance, String password) {

        if (runs.containsKey(instance)) {
            if (password.equals(runs.get(instance).getPassword())) {
                return runs.get(instance);
            }
        }
        return null;
    }

    public static ProtocolInstance isInstance(String instance) {

        if (runs.containsKey(instance)) {
            return runs.get(instance);
        }
        return null;
    }


    public static boolean DestroyInstance(ProtocolInstance prot) {

        LinkedList<RunnableRunAPDU> apduThreads = new LinkedList<>();
        if (prot == null) {
            return false;
        }
        // lets first find the instruction
        ProtocolDefinition.Instruction ins = GlobalConfiguration.getProtocolDestroyCommand(prot.getProtocolName());
        prot.executor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS, prot.queue);
        // init is always from "host" to all smartcards
        for (String playerID : prot.getCardKeys()) {
            Pair<AppletStatus, Integer> player = prot.getCard(playerID);
            // let's now create a command
            String apduString = ins.cls + ins.ins;

            String p1 = prot.ReplacePx(ins.p1, player.getR(), -1);
            if (p1 == null) {
                return false;
            } else {
                apduString += p1;
            }
            String p2 = prot.ReplacePx(ins.p2, player.getR(), -1);
            if (p2 == null) {
                return false;
            } else {
                apduString += p2;
            }
            if (ins.data != null) {
                String data = prot.ReplaceData(ins.data, player.getR(), -1);
                String dataLen = Integer.toHexString(data.length() / 2);
                if (dataLen.length() == 1) {
                    dataLen = "0" + dataLen;
                }
                apduString += dataLen + data;
            }

            // and now we should call the smartcard

            String[] apduArray = new String[1];
            apduArray[0] = apduString;
            RunnableRunAPDU oneSC = new RunnableRunAPDU(player.getL().getAID(), player.getL(), player.getR(), apduArray, false);
            prot.executor.execute(oneSC);
            apduThreads.add(oneSC);

        }
        prot.executor.shutdown();
        try {
            prot.executor.awaitTermination(20, TimeUnit.SECONDS);
            for (RunnableRunAPDU value : apduThreads) {
                if (value.GetStatus(0).equals("9000")) {
                    AppletStatus player = value.GetApplet();
                    player.setStatus(1);
                    GlobalConfiguration.SetAppletReady(player);
                } else {
                    LOG.error("Error response to a command {}: {}", value.GetAPDU(0), value.GetStatus(0));
                }
            }
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            if (GlobalConfiguration.runs.containsKey(prot.getUID())) {
                GlobalConfiguration.runs.remove(prot.getUID());
            }
        }

    }

    private static void SetAppletReady(AppletStatus player) {
        appletsReady.get(player.getAID()).add(player);
    }

    private static ProtocolDefinition.Instruction getProtocolDestroyCommand(String protocolName) {
        if (protocols.get(protocolName) != null) {
            return protocols.get(protocolName).getDestroyInstruction();
        } else {
            return null;
        }
    }

    public static boolean isPhase(String protocolName, String phase) {

        protocolName = protocolName.toLowerCase();
        phase = phase.toLowerCase();

        if (protocols.containsKey(protocolName)) {
            if (protocols.get(protocolName).getPhase(phase) != null) {
                return true;
            }
        }
        return false;
    }

    public static ProtocolDefinition.Phase getPhase(String protocolName, String phase) {
        protocolName = protocolName.toLowerCase();
        phase = phase.toLowerCase();

        ProtocolDefinition.Phase detail = null;
        if (protocols.containsKey(protocolName)) {
            detail = protocols.get(protocolName).getPhase(phase);
        }
        return detail;

    }

    public static ProtocolDefinition getProtocol(String protocol) {
        return protocols.get(protocol);
    }

    public static HashMap<String, ProtocolInstance> GetInstances() {

        return runs;
    }

    public static HashMap<String,ProtocolInstance> GetStats(AtomicInteger noOfInstances) {

        return null;
    }
}
