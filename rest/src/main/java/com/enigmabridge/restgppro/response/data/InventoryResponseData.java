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

package com.enigmabridge.restgppro.response.data;

import com.enigmabridge.restgppro.utils.AppletStatus;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class InventoryResponseData implements GeneralResponseData {

    private HashMap<String, HashMap<String, String>> applets = new HashMap<>();
    private LinkedList<String> readers = new LinkedList<>();
    private LinkedList<String> simonas = new LinkedList<>();

    @Override
    public void setValue(Object value) {

    }

    public void addApplets(String aid, LinkedList<AppletStatus> allApplets) {

        applets.putIfAbsent(aid, new HashMap<>());
        for (AppletStatus element : allApplets) {
            applets.get(aid).put(element.getReader(), element.getStatus().name());
        }
    }

    public HashMap<String, HashMap<String, String>> getApplets(){
        return applets;
    }

    public void addReaders(LinkedList<String> readers) {
        if (readers!=null) {
            this.readers = readers;
        }
    }

    public Object[] getReaders(){
        Object[] readersOut = this.readers.toArray();

        return readersOut;
    }

    public void addSimonas(LinkedList<String> readers) {
        if (simonas == null){
            simonas = new LinkedList<>();
        }
        for (String one: readers) {
            this.simonas.add(one);
        }
    }

    public Object[] getSimona(){
        Object[] readersOut = this.simonas.toArray();

        return readersOut;
    }
}
