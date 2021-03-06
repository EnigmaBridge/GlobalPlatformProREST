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

import static com.enigmabridge.restgppro.utils.GlobalConfiguration.LOG;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class RunnableRunAPDU implements Runnable {
    private final String[] m_apdu;
    private final Integer m_index;
    private final boolean m_doReset;
    private String m_aid;
    private String m_error;
    private AppletStatus m_applet;
    private String[] m_result;
    private Long latency;
    private int m_apduCommands;


    public RunnableRunAPDU(String aid, AppletStatus status, Integer index, String[] apdu, boolean doReset) {
        m_aid = aid;
        m_applet = status;
        m_apdu = apdu;
        m_apduCommands = apdu.length;
        m_result = new String[m_apduCommands];
        m_index = index;
        m_error = null;
        m_doReset = doReset;

    }

    int GetAPDUNumber() {
        return m_apduCommands;
    }

    int GetIndex() {
        return m_index;
    }

    public String GetAPDU(int index) {
        return m_apdu[index];
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
            if ((m_result[index] == null) || (m_result[index].length() < 4)) {
                LOG.error("Response from smartcard is invalid: {} {}", this.m_applet.getReader(), m_result[index]);
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

        String select = GlobalConfiguration.getSelectCommand(m_aid);
        String request = m_applet.getCommand();
        GPTool tool = m_applet.getSession();

        if (((tool == null) || (m_doReset)) && (select != null)) {
            request += " -a " + select;
            m_applet.logAPDU(select);
        }
        for (String currentAPDU : m_apdu) {
            request = request + " -a " + currentAPDU;
            m_applet.logAPDU(currentAPDU);
        }

        ByteArrayOutputStream stdout;
        ByteArrayOutputStream errout;
        if (tool == null) {
            stdout = new ByteArrayOutputStream();
            errout = new ByteArrayOutputStream();

            PrintStream psout = new PrintStream(stdout);
            PrintStream pserr = new PrintStream(errout);
            tool = new GPTool(psout, pserr);
            m_applet.setSession(tool, stdout, errout);
        } else {
            stdout = m_applet.getStdout();
        }

        List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        try {
            latency = -System.currentTimeMillis();
            final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));

            latency += System.currentTimeMillis();

            // lets' now parse the output
            String[] outputLines = stdout.toString("UTF-8").split("\\r?\\n");
            stdout.reset();
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
                    if (command >= m_apduCommands) {
                        break;
                    }
                }
            }
            if (command == 0) {
                LOG.error("No command result detected in output file {}", m_applet.getReader());
            }

        } catch (Exception e) {
            m_error = e.getMessage();
            LOG.error("Exception in GPTool: {}", e.getMessage());
        }
    }
}
