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

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class ProtocolInstance {
    private String ID;
    private String protocol = null;
    private HashMap<String, String> cards = new HashMap<>();
    private HashMap<String, String> results = new HashMap<>();
    private String status;
    private int processors = 0;
    private String password;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void addCard(String cardID, String readerName) {
        this.cards.put(cardID, readerName);
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
        folderPath += "/" + this.getID()+".json";
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
            onejsoncard.put("reader", cards.get(key));
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
}
