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

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.enigmabridge.restgppro.utils.AppletStatus.Status.READY;
import static com.enigmabridge.restgppro.utils.GlobalConfiguration.LOG;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class ProtocolInstance {
    //public ExecutorCompletionService<String> completionService;
    public ThreadPoolExecutor executor;
    public LinkedBlockingDeque queue;
    private HashMap<String, String> parameters = new HashMap<>();
    private String UID;
    private String protocolName = null;
    private HashMap<String, Pair<AppletStatus, Integer>> cards = new HashMap<>();

    private HashMap<String, String[][]> results = new HashMap<>();
    private int processors = 0;
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

    public String ReplacePx(String pX, Integer playerID, int srcCard) {
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
                                temp = "00" + results.get(oneInput)[srcCard][0];
                                temp = temp.substring(temp.length() - 2);
                            } else {
                                temp = "00" + results.get(oneInput)[0][0];
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

    public String ReplaceData(String data, Integer playerID, Integer srcCard) {

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
                            if (oneInput.startsWith("sum_")) {
                                // sum -> we add up 1 .. n numbers into one string

                                BigInteger result = BigInteger.ZERO;

                                for (int srcCard_S = 0; srcCard_S < results.get(oneInput).length; srcCard_S++) {
                                    int arrayLen = results.get(oneInput)[srcCard_S].length;
                                    temp = results.get(oneInput)[srcCard_S][arrayLen - 1];
                                    if (temp.length() % 2 == 1) {
                                        LOG.error("Data has odd length: {} {}", oneInput, temp);
                                        temp = temp + "0";
                                    }
                                    BigInteger newItem = new BigInteger(temp);
                                    result.add(newItem);
                                }
                                // result now contains the sum of all items, we need to add it to the
                                // hash map of values
                                temp = result.toString(16);


                            } else if (results.get(oneInput).length > 1) {
                                // we will use the last response
                                int arrayLen = results.get(oneInput)[srcCard].length;
                                temp = results.get(oneInput)[srcCard][arrayLen - 1];
                                if (temp.length() % 2 == 1) {
                                    LOG.error("Data has odd length: {} {}", oneInput, temp);
                                    temp = temp + "0";
                                }
                            } else {
                                temp = results.get(oneInput)[0][0];
                                if (temp.length() % 2 == 1) {
                                    LOG.error("Data has odd length: {} {}", oneInput, temp);
                                    temp = temp + "0";
                                }
                            }
                            break;
                        }
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

    public void setProtocol(ProtocolDefinition protocol) {
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
        String[][] valueIn = new String[1][1];
        valueIn[0][0] = value;
        this.results.put(name, valueIn);
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

    public Pair<HashMap<String, String[][]>, HashMap<String, Long[][]>> runPhase(String phase, ProtocolDefinition.Phase detail) {

        LinkedList<RunnableRunAPDU> apduThreads = new LinkedList<>();
        HashMap<String, Long[][]> timings = new HashMap<>();

        //let's first check we have all input parameters
        if (!detail.checkInputs(this.parameters)) {
            LOG.error("Missing entry parameters");
            return null;
        }


        for (ProtocolDefinition.PhaseStep oneStep : detail.steps) {
            ProtocolDefinition.Instruction ins = this.protocol.getInstruction(oneStep.apdu);

            // init is always from "host" to all smartcards
            for (String playerID : this.getCardKeys()) {
                Pair<AppletStatus, Integer> player = this.getCard(playerID);

                String[] apduArray;
                if (oneStep.from.equals("@worker")) {
                    apduArray = new String[this.processors - 1];
                    int arrayIndex = 0;
                    for (int srcCard = 0; srcCard < this.processors; srcCard++) {
                        if (srcCard != player.getR()) {
                            apduArray[arrayIndex] = CreateAPDU(player, ins, srcCard);
                            arrayIndex += 1;
                        }
                    }
                } else {
                    apduArray = new String[1];
                    apduArray[0] = CreateAPDU(player, ins, -1);
                }
                // and now we should call the smartcard
                RunnableRunAPDU oneSC = new RunnableRunAPDU(player.getL().getAID(),
                        player.getL(), player.getR(), apduArray, false);
                executor.execute(oneSC);
                apduThreads.add(oneSC);

            }
            executor.shutdown();
            try {
                executor.awaitTermination(20, TimeUnit.SECONDS);
                timings.putIfAbsent(oneStep.apdu, new Long[apduThreads.size()][1]);
                int threadIndex = 0;
                for (RunnableRunAPDU value : apduThreads) {
                    timings.get(oneStep.apdu)[threadIndex][0] = value.GetLatency();
                    threadIndex += 1;
                    String result = null;
                    String[][] dataIn = null;
                    if ((ins.result != null) && ins.result.startsWith("@")) {
                        result = ins.result;
                        if (!results.containsKey(ins.result)) {
                            String[][] dataTemp = new String[processors][value.GetAPDUNumber()];
                            results.putIfAbsent(ins.result, dataTemp);
                        }
                        dataIn = results.get(ins.result);
                    }

                    for (int index = 0; index < value.GetAPDUNumber(); index++) {
                        if (value.GetStatus(index).equals("9000")) {
                            if (result != null) {
                                if (dataIn != null) {
                                    dataIn[value.GetIndex()][index] = value.GetResponse(index);
                                } else {
                                    LOG.error("Result array is null, integrity problem: {}", ins.result);
                                }
                            }
                        } else {
                            LOG.error("Error executing APDU command {} {}", value.GetStatus(index), value.GetApplet().getReader(), value.GetAPDU(index));
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.error("Processing timeout: {}", oneStep.apdu);
                return null;
            }
            apduThreads = new LinkedList<>();
        }
        LinkedList<String> resultNames = detail.getResults();

        HashMap<String, String[][]> output = new HashMap<>();

        for (String oneResultName : resultNames) {
            if (this.results.containsKey(oneResultName)) {
                output.putIfAbsent(oneResultName, results.get(oneResultName));
            } else {
                output.putIfAbsent(oneResultName, new String[0][0]);
            }
        }


        return new Pair<>(output, timings);
    }

    private String CreateAPDU(Pair<AppletStatus, Integer> player, ProtocolDefinition.Instruction ins,
                              int srcCard) {
        // let's now create a command
        String apduString = ins.cls + ins.ins;

        String p1 = this.ReplacePx(ins.p1, player.getR(), srcCard);
        if (p1 == null) {
            return null;
        } else {
            apduString += p1;
        }
        String p2 = this.ReplacePx(ins.p2, player.getR(), srcCard);
        if (p2 == null) {
            return null;
        } else {
            apduString += p2;
        }
        if (ins.data != null) {
            String data = this.ReplaceData(ins.data, player.getR(), srcCard);
            String dataLen = Integer.toHexString(data.length() / 2);
            if (dataLen.length() == 1) {
                dataLen = "0" + dataLen;
            }
            apduString += dataLen + data;
        }

        return apduString;
    }

    /**
     * Extracts data from the instruction and returns it as a byte array.
     *
     * @param player - information about the target smart card.
     * @param ins    - an instruction result from the previous step.
     * @return a data byte array or NULL
     */
    private byte[] GetInstructionData(Pair<AppletStatus, Integer> player,
                                      ProtocolDefinition.Instruction ins) {

        if (ins.data != null) {
            String data = this.ReplaceData(ins.data, player.getR(), -1);
            String dataLen = Integer.toHexString(data.length() / 2);
            if (dataLen.length() == 1) {
                dataLen = "0" + dataLen;
            }
            return DatatypeConverter.parseHexBinary(data);
        } else {
            return null;
        }

    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public int getSize() {
        return processors;
    }

    public enum InstanceStatus {CREATED, ALLOCATED, INITIALIZED, ERROR, DESTROYED}

}
