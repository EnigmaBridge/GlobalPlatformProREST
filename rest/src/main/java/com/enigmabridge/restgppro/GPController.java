/*
 * Copyright (c) 2016 Enigma Bridge Ltd.
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

package com.enigmabridge.restgppro;

import com.enigmabridge.restgppro.response.BasicResponse;
import com.enigmabridge.restgppro.response.GeneralResponse;
import com.enigmabridge.restgppro.response.data.BasicResponseData;
import com.enigmabridge.restgppro.rest.JsonEnvelope;
import com.enigmabridge.restgppro.utils.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.javacard.gp.GPArgumentTokenizer;
import pro.javacard.gp.GPTool;

import javax.servlet.http.HttpServletRequest;
import javax.smartcardio.CardException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.enigmabridge.restgppro.ApiConfig.API_PATH;

/**
 * Created by dusanklinec on 20.07.16.
 */
@RestController
@PreAuthorize("hasAuthority('" + ApiConfig.MANAGEMENT_ROLE + "')")
public class GPController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(API_PATH+"/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }

    @RequestMapping(API_PATH+"/raw")
    public void rawRequest(@RequestParam(value = "request") String request, OutputStream output) throws IOException, NoSuchAlgorithmException {
        final PrintStream ps = new PrintStream(output, true);
        final GPTool tool = new GPTool(ps, ps);
        final List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));
        try {
            tool.close();
        } catch (CardException ignored) {
        }
        ps.println("Done: " + code);
    }

    @RequestMapping(API_PATH+"/basic")
    public GeneralResponse basicRequest(@RequestParam(value = "apdu") String request,
                                        @RequestParam(value = "terminal") String terminal,
                                        HttpServletRequest httpRequest) throws IOException, NoSuchAlgorithmException {
        JsonEnvelope message = null;
        Long timeStart = System.currentTimeMillis();
        String remoteIPAddress = httpRequest.getRemoteAddr();
        BasicResponse msgBack = null;
        BasicResponseData msgData = null;
        int status = Consts.SW_STAT_OK;


        try {
            msgBack = new BasicResponse();

            String protInstanceName = "BASIC-" + terminal;
            ProtocolInstance prot = GlobalConfiguration.isInstance(protInstanceName);

            if (prot == null) {
                String password = "password";
                prot = new ProtocolInstance();
                prot.setUID(protInstanceName);
                prot.setAID("basic");
                prot.setProcessors(1);
                ProtocolDefinition tempBasic = new ProtocolDefinition();
                tempBasic.setName("basic");
                prot.setProtocol(tempBasic);
                prot.setPassword(password);
                // whilst a list, it will always be one card in the current implementation
                LinkedList<AppletStatus> newCards = GlobalConfiguration.getFreeBasicCard(terminal);
                for (AppletStatus onecard : newCards) {
                    prot.addProcessor(onecard.getAppletID(), onecard.getReader(), -1);
                }
                // add the instance to a volatile list
                GlobalConfiguration.addInstance(protInstanceName, prot);
                GlobalConfiguration.InitializeInstance(prot);
            }

            Pair<HashMap<String, String>, HashMap<String, Long>> allResult = prot.runAPDU(request, terminal, false);

            if (allResult == null) {
                msgBack.setError("Protocol phase not known");
                status = Consts.SW_STAT_UNKNOWN_PHASE;
            } else {
                msgData = new BasicResponseData();
                msgData.setProtocol(prot.getProtocolName());
                msgData.setInstance(prot.getUID());
                msgData.setSize(prot.getSize());
                msgData.setDetail(allResult.getL(), allResult.getR());


            }
        } finally {
            if (msgBack == null) {
                msgBack = new BasicResponse();
                status = Consts.SW_STAT_PROCESSING_ERROR;
            }

            msgBack.setStatus(status);
            msgBack.setResponse(msgData);
            long elapsedTime = System.currentTimeMillis() - timeStart;
            msgBack.setLatency(elapsedTime);
        }

        return msgBack;
    }
}
