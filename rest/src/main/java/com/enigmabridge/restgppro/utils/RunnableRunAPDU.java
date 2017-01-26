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
    private final String m_apdu;
    private String m_aid;
    private AppletStatus m_applet;
    private String m_result;
    private Long latency;
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public RunnableRunAPDU(String aid, AppletStatus status, String apdu) {
        m_aid = aid;
        m_applet = status;
        m_apdu = apdu;

    }

    public String GetResult(){
        return m_result;
    }

    public String GetStatus(){
        if (m_result.length()<4){
            return null;
        } else {
            return m_result.substring(m_result.length()-4);
        }
    }

    public AppletStatus GetApplet(){
        return m_applet;
    }

    public Long GetLatency(){
        return latency;
    }

    @Override
    public void run() {

        String request = m_applet.getCommand() + " -a "
                + GlobalConfiguration.getSelectCommand(m_aid) + " -a "
                + m_apdu;

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
            for (String line : outputLines) {
                if (counting >= 0) {
                    counting += 1;
                } else {
                    line = line.trim();
                    if (line.equalsIgnoreCase("# Sent")) {
                        counting = 0;
                    }
                }
                if (counting == 7) {
                    line = line.trim();
                    m_result = line;
                    counting = -1;
                }
            }
        } catch (Exception e) {
            LOG.error("Exception in GPTool: {}", errout);
        }
    }
}
