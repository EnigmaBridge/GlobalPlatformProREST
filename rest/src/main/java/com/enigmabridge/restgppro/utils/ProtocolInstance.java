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
import java.util.Set;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class ProtocolInstance {
    private HashMap<String, String> parameters = new HashMap<>();
    private String ID;
    private String protocol = null;
    private HashMap<String, Pair<AppletStatus,Integer>> cards = new HashMap<>();
    private HashMap<String, String> results = new HashMap<>();
    private int processors = 0;

    ;
    private String password;
    private long lastEvent;
    private InstanceStatus status;
    private int lastCardID = 0;
    private String AID;

    public ProtocolInstance() {
        lastEvent = System.currentTimeMillis();
    }

    public Pair<AppletStatus, Integer> getCard(String key) {
        return cards.get(key);
    }

    public Set<String> getCardKeys() {
        return cards.keySet();
    }

    public String ReplacePx(String pX, Integer playerID) {
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
            } else {
                String temp = null;
                for (String oneInput : this.parameters.keySet()) {
                    if (pX.equalsIgnoreCase(oneInput)) {
                        if (temp.length() > 2) {
                            temp = null;
                        } else {
                            temp = "00" + this.parameters.get(oneInput);
                            temp = temp.substring(temp.length() - 2);
                        }
                        break;
                    }
                }
                return temp;
            }
        } else {
            // no change
            return pX;
        }
    }

    public String ReplaceData(String data, Integer playerID) {
        if (data.startsWith("@")) {
            if (data.equalsIgnoreCase("@dst")) {
                String temp = Integer.toHexString(playerID);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else if (data.equalsIgnoreCase("@processors")) {
                String temp = Integer.toHexString(this.processors);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                temp = temp.substring(0, 2);
                return temp;
            } else {
                String temp = null;
                for (String oneInput : this.parameters.keySet()) {
                    if (data.equalsIgnoreCase(oneInput)) {
                        if ((temp.length() % 2) == 1) {
                            temp = "0" + temp;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean addProcessor(String cardID, String readerName) {

        this.status = InstanceStatus.ALLOCATED;
        //let's find the applet in a given reader
        HashMap<String, AppletStatus> as = GlobalConfiguration.getCardApplets(readerName);

        if (this.AID == null){
            this.AID = GlobalConfiguration.getProtocolAID(this.protocol);
        }

        if ((as!=null)&&(as.containsKey(this.AID))){
            this.cards.put(cardID, new Pair<AppletStatus, Integer>(as.get(this.AID), lastCardID));
            lastCardID += 1;
            return true;
        } else {
            return false;
        }
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

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean persist() {
        boolean bResult = true;
        String folderPath = GlobalConfiguration.getInstanceFolder();
        if (folderPath == null) {
            folderPath = ".";
        }
        folderPath += "/" + this.getID() + ".json";
        File newFile = new File(folderPath);

        JSONObject json = new JSONObject();
        json.put("id", this.ID);
        json.put("processors", processors);
        json.put("protocol", protocol);
        json.put("result", (Object) null);
        JSONArray jsoncards = new JSONArray();
        for (String key : cards.keySet()) {
            JSONObject onejsoncard = new JSONObject();
            onejsoncard.put("id", key);
            onejsoncard.put("reader", cards.get(key).getL());
            onejsoncard.put("index", cards.get(key).getR());
            jsoncards.put(onejsoncard);
        }
        json.put("cards", jsoncards);
        try {
            PrintWriter out = new PrintWriter(folderPath);
            out.println(json.toString());
            out.close();
        } catch (Exception ex) {
            bResult = false;
        }
        return bResult;
    }

    public enum InstanceStatus {ALLOCATED, INITIALIZED, DESTROYED}


}
