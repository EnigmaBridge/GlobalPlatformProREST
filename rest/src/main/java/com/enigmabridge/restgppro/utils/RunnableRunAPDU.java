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
import pro.javacard.gp.GPArgumentTokenizer;
import pro.javacard.gp.GPTool;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class RunnableRunAPDU implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final String[] m_apdu;
    private final Integer m_index;
    private String m_aid;
    private AppletStatus m_applet;
    private String[] m_result;
    private Long latency;
    private int m_apduCommands;

    public RunnableRunAPDU(String aid, AppletStatus status, Integer index, String[] apdu) {
        m_aid = aid;
        m_applet = status;
        m_apdu = apdu;
        m_apduCommands = apdu.length;
        m_result = new String[m_apduCommands];
        m_index = index;

    }

    int GetAPDUNumber() {
        return m_apduCommands;
    }

    int GetIndex() {
        return m_index;
    }

    public String GetAPDU() {
        return m_apdu[0];
    }

    public String GetResponse(int index) {
        if ((index < 0) || (index > m_apduCommands)) {
            return null;
        } else {
            if (m_result[index].length() <= 4) {
                return null;
            } else {
                return m_result[index].substring(0, m_result[index].length() - 4);
            }
        }
    }

    public String GetStatus(int index) {
        if ((index < 0) || (index > m_apduCommands)) {
            return null;
        } else {
            if (m_result[index].length() < 4) {
                return null;
            } else {
                return m_result[index].substring(m_result[index].length() - 4);
            }
        }
    }

    public AppletStatus GetApplet() {
        return m_applet;
    }

    public Long GetLatency() {
        return latency;
    }

    @Override
    public void run() {

        String request = m_applet.getCommand() + " -a "
                + GlobalConfiguration.getSelectCommand(m_aid);
        for (String currentAPDU : m_apdu) {
            request = request + " -a " + currentAPDU;
        }

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream errout = new ByteArrayOutputStream();

        PrintStream psout = new PrintStream(stdout);
        PrintStream pserr = new PrintStream(errout);
        GPTool tool = new GPTool(psout, pserr);
        List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        try {
            latency = -System.currentTimeMillis();
            final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));
            latency += System.currentTimeMillis();

            // lets' now parse the output
            String[] outputLines = stdout.toString("UTF-8").split("\\r?\\n");
            int counting = -1;
            int command = 0;
            for (String line : outputLines) {
                if (line.trim().equalsIgnoreCase(m_apdu[command])) {
                    // we found a command
                    counting = 0;
                }

                if (counting >= 0) {
                    counting += 1;
                }
                if (counting == 3) {
                    line = line.trim();
                    m_result[command] = line;
                    command += 1;
                    counting = -1;
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in GPTool: {}", errout);
        }
    }
}
