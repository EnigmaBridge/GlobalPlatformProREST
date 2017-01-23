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

package com.enigmabridge.restgppro;

import com.enigmabridge.restgppro.response.CreateResponse;
import com.enigmabridge.restgppro.response.GeneralResponse;
import com.enigmabridge.restgppro.response.InventoryResponse;
import com.enigmabridge.restgppro.response.data.CreateResponseData;
import com.enigmabridge.restgppro.response.data.InventoryResponseData;
import com.enigmabridge.restgppro.rest.JsonEnvelope;
import com.enigmabridge.restgppro.utils.AppletStatus;
import com.enigmabridge.restgppro.utils.Consts;
import com.enigmabridge.restgppro.utils.GlobalConfiguration;
import com.enigmabridge.restgppro.utils.ProtocolInstance;
import org.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dusanklinec on 20.07.16.
 */
@RestController
@PreAuthorize("hasAuthority('" + ApiConfig.BUSINESS_ROLE + "')")
public class MPCController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private static final String MPC_PATH = ApiConfig.API_PATH+"/mpc";

    @RequestMapping(MPC_PATH+"/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }


    @RequestMapping(MPC_PATH+"/inventory")
    public GeneralResponse inventory(HttpServletRequest request){
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        InventoryResponse msgBack = new InventoryResponse();

        InventoryResponseData data = new InventoryResponseData();

        for (String aid:GlobalConfiguration.getCardAppletsIDs()){
            LinkedList<AppletStatus> allApplets = GlobalConfiguration.getAppletInstances(aid);
                data.addApplets(aid, allApplets);
        }

        LinkedList<String> readers = GlobalConfiguration.getReaders();
        data.addReaders(readers);

        LinkedList<String> addresses = GlobalConfiguration.getSimonaIPs();
        for (String ip: addresses) {
            LinkedList<String> readers2 = GlobalConfiguration.getSimonaReaders(ip);
            data.addSimonas(readers2);
        }

        msgBack.setResponse(data);

        return msgBack;
    }
    

    @RequestMapping(value = MPC_PATH + "/create", method = RequestMethod.POST)
    public GeneralResponse create(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        CreateResponse msgBack = null;
        CreateResponseData msgData = null;
        int status = Consts.SW_STAT_OK;

        try {
            msgBack = new CreateResponse();
            JSONObject parsedContent = new JSONObject(jsonStr);
            String protocol = parsedContent.getString("protocol");
            int size = parsedContent.getInt("size");
            if (GlobalConfiguration.isProtocol(protocol)){
                //let's find some free smartcards
                String protocolInstance = Long.toString(System.currentTimeMillis());
                LinkedList<AppletStatus> instanceProcessors = GlobalConfiguration.getFreeSmartcards(protocol, size, protocolInstance);
                msgData = new CreateResponseData();
                String password = "password";
                ProtocolInstance prot = new ProtocolInstance();
                prot.setID(protocolInstance);
                prot.setProcessors(size);
                prot.setProtocol(protocol);
                prot.setPassword(password);
                for (AppletStatus onecard: instanceProcessors) {
                    prot.addCard(onecard.getAppletID(), onecard.getReader());
                }
                // let's store it in a file
                GlobalConfiguration.addInstance(protocolInstance, prot);
                prot.persist();
                msgData = new CreateResponseData();
                msgData.setInstance(protocolInstance);
                msgData.setPassword(password);
            } else {
                status = Consts.SW_STAT_UNKNOWN_PROTOCOL;
            }

        } catch (Exception ex){
            status = Consts.SW_STAT_INPUT_PARSE_FAIL;

        } finally {
            if (msgBack == null){
                msgBack = new CreateResponse();
                status = Consts.SW_STAT_PROCESSING_ERROR;
            }

            msgBack.setStatus(status);
            msgBack.setResponse(msgData);
            long elapsedTime = System.currentTimeMillis() - timeStart;
            //msgBack.setLatency(elapsedTime);
        }

        return msgBack;
    }


    @RequestMapping(value = MPC_PATH + "/destroy", method = RequestMethod.POST)
    public GeneralResponse destroy(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        GeneralResponse msgBack = null;

        JSONObject parsedContent = new JSONObject(jsonStr);



        return msgBack;
    }

    @RequestMapping(value = MPC_PATH + "/destroy", method = RequestMethod.POST)
    public GeneralResponse run(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        GeneralResponse msgBack = null;

        JSONObject parsedContent = new JSONObject(jsonStr);



        return msgBack;
    }

}
