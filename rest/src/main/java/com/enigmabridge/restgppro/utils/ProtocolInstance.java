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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.enigmabridge.restgppro.utils.AppletStatus.Status.READY;
import static com.enigmabridge.restgppro.utils.GlobalConfiguration.LOG;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class ProtocolInstance {
    private HashMap<String, String> parameters = new HashMap<>();
    private String UID;
    private String protocolName = null;
    private HashMap<String, Pair<AppletStatus, Integer>> cards = new HashMap<>();
    private HashMap<String, String> results = new HashMap<>();
    private int processors = 0;

    ;
    private String password;
    private long lastEvent;
    private InstanceStatus status;
    private int lastCardID = 0;
    private String AID;
    private ProtocolDefinition protocol = null;

    public ProtocolInstance() {

        lastEvent = System.currentTimeMillis();
        this.status = InstanceStatus.CREATED;

    }

    public Pair<AppletStatus, Integer> getCard(String key) {
        return cards.get(key);
    }

    public Set<String> getCardKeys() {
        return cards.keySet();
    }

    public String ReplacePx(String pX, Integer playerID, HashMap<String, String[]> results, int srcCard) {
        if (pX.startsWith("@")) {
            if (pX.equalsIgnoreCase("@dst")) {
                String temp = Integer.toHexString(playerID);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else if (pX.equalsIgnoreCase("@processors")) {
                String temp = Integer.toHexString(this.processors);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else if (pX.equalsIgnoreCase("@src")) {
                String temp = Integer.toHexString(srcCard);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;

            } else {
                String temp = null;
                for (String oneInput : this.parameters.keySet()) {
                    if (pX.equalsIgnoreCase(oneInput)) {
                        temp = this.parameters.get(oneInput);
                        if (temp.length() > 2) {
                            temp = null;
                        } else {
                            temp = "00" + temp;
                            temp = temp.substring(temp.length() - 2);
                        }
                        break;
                    }
                }
                if (temp == null) {
                    for (String oneInput : results.keySet()) {
                        if (pX.equalsIgnoreCase(oneInput)) {
                            if (results.get(oneInput).length > 1) {
                                temp = "00" + results.get(oneInput)[srcCard];
                                temp = temp.substring(temp.length() - 2);
                            } else {
                                temp = "00" + results.get(oneInput)[0];
                                temp = temp.substring(temp.length() - 2);
                            }
                            break;
                        }
                    }
                }
                return temp;
            }
        } else {
            // no change
            return pX;
        }
    }

    public String ReplaceData(String data, Integer playerID, HashMap<String, String[]> results, Integer srcCard) {

        String temp = null;

        if (data.startsWith("@")) {
            if (data.equalsIgnoreCase("@dst")) {
                temp = Integer.toHexString(playerID);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else if (data.equalsIgnoreCase("@processors")) {
                temp = Integer.toHexString(this.processors);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else if (data.equalsIgnoreCase("@src")) {
                temp = Integer.toHexString(srcCard);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else {
                for (String oneInput : this.parameters.keySet()) {
                    if (data.equalsIgnoreCase(oneInput)) {
                        temp = this.parameters.get(oneInput);
                        if ((temp.length() % 2) == 1) {
                            temp = "0" + temp;
                        }
                        break;
                    }
                }
                if (temp == null) {
                    for (String oneInput : results.keySet()) {
                        if (data.equalsIgnoreCase(oneInput)) {
                            if (results.get(oneInput).length > 1) {
                                temp = "00" + results.get(oneInput)[srcCard];
                                temp = temp.substring(temp.length() - 2);
                            } else {
                                temp = "00" + results.get(oneInput)[0];
                                temp = temp.substring(temp.length() - 2);
                            }
                        }
                        break;
                    }
                }
                return temp;
            }
        } else {
            // no change
            return data;
        }
    }

    public String getProtocolName() {
        return protocolName.toLowerCase();
    }

    public void setProtocolName(ProtocolDefinition protocol) {
        this.protocolName = protocol.getName();
        this.protocol = protocol;

    }

    public boolean addProcessor(String cardID, String readerName, int index) {

        //let's find the applet in a given reader
        HashMap<String, AppletStatus> as = GlobalConfiguration.getCardApplets(readerName);

        if (this.AID == null) {
            this.AID = GlobalConfiguration.getProtocolAID(this.protocolName);
        }

        if ((as != null) && (as.containsKey(this.AID))) {
            if (index >= 0) {
                this.cards.put(cardID, new Pair<AppletStatus, Integer>(as.get(this.AID), index));
            } else {
                this.cards.put(cardID, new Pair<AppletStatus, Integer>(as.get(this.AID), lastCardID));
            }
            lastCardID += 1;
            return true;
        } else {
            return false;
        }
    }

    public AppletStatus.Status GetCardStatus() {
        AppletStatus.Status temp = AppletStatus.Status.BUSY;
        for (String index : cards.keySet()) {
            if (cards.get(index).getL().getStatus() == READY) {
                temp = READY;
            }
        }
        return temp;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public boolean isCardNumberCorrect() {

        return processors == cards.size();
    }

    public void addResult(String name, String value) {
        this.results.put(name, value);
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String ID) {
        this.UID = ID;
    }

    public boolean persist() {
        boolean bResult = true;
        String folderPath = GlobalConfiguration.getInstanceFolder();
        if (folderPath == null) {
            folderPath = ".";
        }
        folderPath += "/" + this.getUID() + ".json";
        File newFile = new File(folderPath);

        JSONObject json = new JSONObject();
        json.put("id", this.UID);
        json.put("processors", processors);
        json.put("protocol", protocolName);
        json.put("result", (Object) null);
        json.put("key", password);
        JSONArray jsoncards = new JSONArray();
        for (String key : cards.keySet()) {
            JSONObject onejsoncard = new JSONObject();
            onejsoncard.put("id", key);
            onejsoncard.put("reader", cards.get(key).getL().getReader());
            onejsoncard.put("index", cards.get(key).getR());
            jsoncards.put(onejsoncard);
        }
        json.put("group", jsoncards);
        try {
            PrintWriter out = new PrintWriter(folderPath);
            out.println(json.toString());
            out.close();
        } catch (Exception ex) {
            bResult = false;
        }
        return bResult;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean removeFile() {
        String folderPath = GlobalConfiguration.getInstanceFolder();
        if (folderPath == null) {
            folderPath = ".";
        }
        folderPath += "/" + this.getUID() + ".json";
        File newFile = new File(folderPath);

        if (newFile.exists()) {
            return newFile.delete();
        } else {
            return false;
        }
    }

    public void SetStatus(InstanceStatus status) {
        this.status = status;
    }

    public InstanceStatus GetStatus() {
        return this.status;
    }

    public HashMap<String, String[]> runPhase(String phase, ProtocolDefinition.Phase detail) {

        LinkedList<RunnableRunAPDU> apduThreads = new LinkedList<>();

        HashMap<String, String[]> results = new HashMap<>();

        //let's first check we have all input parameters
        if (!detail.checkInputs(this.parameters)) {
            LOG.error("Missing entry parameters");
            return null;
        }


        for (ProtocolDefinition.PhaseStep oneStep : detail.steps) {
            ProtocolDefinition.Instruction ins = this.protocol.getInstruction(oneStep.apdu);

            BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
            ThreadPoolExecutor executor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS, queue);
            // init is always from "host" to all smartcards
            for (String playerID : this.getCardKeys()) {
                Pair<AppletStatus, Integer> player = this.getCard(playerID);

                String[] apduArray;
                if (oneStep.from.equals("@worker")) {
                    apduArray = new String[this.processors - 1];
                    for (int srcCard = 0; srcCard < this.processors; srcCard++) {
                        if (srcCard != player.getR()) {
                            apduArray[srcCard] = CreateAPDU(player, ins, results, srcCard);
                        }
                    }
                } else {
                    apduArray = new String[1];
                    apduArray[0] = CreateAPDU(player, ins, results, -1);
                }
                // and now we should call the smartcard
                RunnableRunAPDU oneSC = new RunnableRunAPDU(player.getL().getAID(),
                        player.getL(), player.getR(), apduArray);
                executor.execute(oneSC);
                apduThreads.add(oneSC);

            }
            executor.shutdown();
            try {
                executor.awaitTermination(20, TimeUnit.SECONDS);
                for (RunnableRunAPDU value : apduThreads) {
                    if (value.GetStatus().equals("9000")) {
                        String apduResponse = value.GetResponse();
                        if ((ins.result != null) && ins.result.startsWith("@")) {
                            if (!results.containsKey(ins.result)) {
                                results.put(ins.result, new String[this.processors]);
                            }
                            results.get(ins.result)[value.GetIndex()] = value.GetResponse();
                        }
                    } else {
                        LOG.error("Error executing APDU command {} {}", value.GetStatus(), value.GetApplet().getReader(), value.GetAPDU());
                    }
                }
            } catch (InterruptedException e) {
                LOG.error("Processing timeout: {}", oneStep.apdu);
                return null;
            }
            apduThreads = new LinkedList<>();
        }
        return results;
    }

    private String CreateAPDU(Pair<AppletStatus, Integer> player, ProtocolDefinition.Instruction ins, HashMap<String, String[]> results, int srcCard) {
        // let's now create a command
        String apduString = ins.cls + ins.ins;

        String p1 = this.ReplacePx(ins.p1, player.getR(), results, srcCard);
        if (p1 == null) {
            return null;
        } else {
            apduString += p1;
        }
        String p2 = this.ReplacePx(ins.p2, player.getR(), results, srcCard);
        if (p2 == null) {
            return null;
        } else {
            apduString += p2;
        }
        if (ins.data != null) {
            String data = this.ReplaceData(ins.data, player.getR(), results, srcCard);
            String dataLen = Integer.toHexString(data.length() / 2);
            if (dataLen.length() == 1) {
                dataLen = "0" + dataLen;
            }
            apduString += dataLen + data;
        }

        return apduString;
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public enum InstanceStatus {CREATED, ALLOCATED, INITIALIZED, ERROR, DESTROYED}


}
