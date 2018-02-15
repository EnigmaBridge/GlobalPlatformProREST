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

import pro.javacard.gp.GPArgumentTokenizer;
import pro.javacard.gp.GPTool;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class RunnableListApplets implements Runnable {
    private String m_reader;
    private String m_cmd;
    private String m_simonaIP;

    public RunnableListApplets(String reader, String cmd, String ipAddress) {
        m_reader = reader;
        m_cmd = cmd;
        m_simonaIP = ipAddress;

    }

    @Override
    public void run() {
        String localReader = m_reader;
        String localCmd = m_cmd;
        String localIP = m_simonaIP;
        String request = localCmd + "\"" + localReader + "\"";
        AppletStatus appletStatus = new AppletStatus();
        appletStatus.setCommand(request);
        appletStatus.setStatus(-1);
        appletStatus.setReader(localReader);
        request += " -l";

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream errout = new ByteArrayOutputStream();

        PrintStream psout = new PrintStream(stdout);
        PrintStream pserr = new PrintStream(errout);
        GPTool tool = new GPTool(psout, pserr);
        List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        try {
            boolean allGood = false;
            try {
            final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));
                allGood = true;
            } catch (IllegalStateException ignored) {
                allGood = true;
            }

            if (allGood) {
            // lets' now parse the output
            String[] outputLines = stdout.toString("UTF-8").split("\\r?\\n");
            for (String line : outputLines) {
                line = line.trim();
                String[] lineParts = line.split(":");
                if (lineParts[0].equalsIgnoreCase("Applet")) {
                    if (!lineParts[1].trim().startsWith("A000")) {
                        GlobalConfiguration.addApplet(localReader, lineParts[1].trim(), appletStatus);
                    }
                }
                    if (lineParts[0].equalsIgnoreCase("# ATR")) {
                        //first get the IP address

                        // we add the card into the list of active cards - even if there's not applet present
                        GlobalConfiguration.addSmartcard(localReader, localIP, lineParts[1]);
                    }
                }
            }
        } catch (Exception ignore) {
            System.out.print("ERROR"+localReader);
        }
    }
}
