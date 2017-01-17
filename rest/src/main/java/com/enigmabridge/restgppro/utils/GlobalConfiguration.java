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

    public static void setReaderSet(LinkedList<String> readerSet) {
        GlobalConfiguration.readerSet = readerSet;
    }

    public static void setSimonaUse(boolean simonaUse) {
        GlobalConfiguration.simonaUse = simonaUse;
    }

    public static void setSimonaIO(String simonaIO) {
        GlobalConfiguration.simonaIO = simonaIO;
    }

    public static void setSimonaSet(LinkedList<String> simonaSet) {
        GlobalConfiguration.simonaSet = simonaSet;
    }
}
