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

/**
 * Created by Enigma Bridge Ltd (dan) on 17/01/2017.
 */
public class GlobalConfiguration {

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
    private static HashMap<String, HashMap<String,AppletStatus>> cards = new HashMap<>();
    private static HashMap<String, LinkedList<AppletStatus>> applets = new HashMap<>();

    public static void setProtocolFolder(String path) {
        GlobalConfiguration.protocolFolder = path;
    }

    public static void setInstanceFolder(String instanceFolder) {
        GlobalConfiguration.instanceFolder = instanceFolder;
    }

    public static void setReaderUse(boolean readerScanning) {
        GlobalConfiguration.readerUse = readerScanning;
    }

    public static void setReaderIO(String readerIO) {
        GlobalConfiguration.readerIO = readerIO;
    }

    public static String getReaderIO() {
        return GlobalConfiguration.readerIO;
    }

    public static void setReaderSet(LinkedList<String> readerSet) {
        GlobalConfiguration.readerSet = readerSet;
    }

    public static void setSimonaUse(boolean simonaUse) {
        GlobalConfiguration.simonaUse = simonaUse;
    }

    public static void setSimonaIO(String simonaIO) {
        GlobalConfiguration.simonaIO = simonaIO;
    }

    public static String getSimonaIO() {
        return GlobalConfiguration.simonaIO;
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

    public static void updateAppletStatus(String reader, String aid, AppletStatus.Status status){
        if (cards.containsKey(reader)){
            if (cards.get(reader).containsKey(aid)){
                cards.get(reader).get(aid).setStatus(status);
            }
        }
    }

    public static HashMap<String, AppletStatus> getCardApplets(String reader) {
        if (cards.containsKey(reader)){
            return cards.get(reader);
        } else {
            return null;
        }
    }

    public static LinkedList<String> getSimonaReaders(String simona) {
        return simonaReaders.get(simona);
    }
}
