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

import com.enigmabridge.restgppro.response.*;
import com.enigmabridge.restgppro.response.data.*;
import com.enigmabridge.restgppro.rest.JsonEnvelope;
import com.enigmabridge.restgppro.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import static com.enigmabridge.restgppro.utils.GlobalConfiguration.LOG;

/**
 * Created by dusanklinec on 20.07.16.
 */
@RestController
@PreAuthorize("hasAuthority('" + ApiConfig.BUSINESS_ROLE + "')")
public class MPCController {
    private static final String template = "Hello, %s!";
    private static final String MPC_PATH = ApiConfig.API_PATH + "/mpc";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(MPC_PATH + "/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }


    @RequestMapping(MPC_PATH + "/inventory")
    public GeneralResponse inventory(HttpServletRequest request) {
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        InventoryResponse msgBack = new InventoryResponse();

        InventoryResponseData data = new InventoryResponseData();

        for (String aid : GlobalConfiguration.getCardAppletsIDs()) {
            LinkedList<AppletStatus> allApplets = GlobalConfiguration.getAppletInstances(aid);
            data.addApplets(aid, allApplets);
        }

        LinkedList<String> readers = GlobalConfiguration.getReaders();
        data.addReaders(readers);

        LinkedList<String> addresses = GlobalConfiguration.getSimonaIPs();
        for (String ip : addresses) {
            LinkedList<String> readers2 = GlobalConfiguration.getSimonaReaders(ip);
            data.addSimonas(readers2);
        }

        msgBack.setResponse(data);
        long elapsedTime = System.currentTimeMillis() - timeStart;
        msgBack.setLatency(elapsedTime);

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
            if (GlobalConfiguration.isProtocol(protocol)) {
                //let's find some free smartcards
                String protocolInstanceUID = Long.toString(System.currentTimeMillis());
                LinkedList<AppletStatus> instanceProcessors = GlobalConfiguration.getFreeApplets(protocol, size, protocolInstanceUID);
                msgData = new CreateResponseData();
                if (instanceProcessors == null) {
                    int test = GlobalConfiguration.getReadyCardsNumber(protocol);
                    if (test < 0) {
                        status = Consts.SW_STAT_UNKNOWN_PROTOCOL;
                    } else if (test < size) {
                        status = Consts.SW_STAT_NO_RESOURCES;
                    } else {
                        status = Consts.SW_STAT_PROCESSING_ERROR;
                    }
                    msgData.setDetail((test < 0) ? 0 : test, size, protocolInstanceUID,
                            null);
                } else {
                    String password = "password";
                    ProtocolInstance prot = new ProtocolInstance();
                    prot.setUID(protocolInstanceUID);
                    prot.setProcessors(size);
                    prot.setProtocol(GlobalConfiguration.getProtocol(protocol));
                    prot.setPassword(password);
                    for (AppletStatus onecard : instanceProcessors) {
                        prot.addProcessor(onecard.getAppletID(), onecard.getReader(), -1);
                    }
                    // let's store it in a file
                    GlobalConfiguration.addInstance(protocolInstanceUID, prot);
                    // now we will initialize the protocol instance
                    boolean result = GlobalConfiguration.InitializeInstance(prot);
                    if (result) {
                        prot.persist();
                        msgData.setInstance(protocolInstanceUID);
                        msgData.setPassword(password);
                        msgData.setDetail(size, size, protocolInstanceUID,
                                instanceProcessors);
                    } else {
                        msgData = null;
                        status = Consts.SW_STAT_SETUP_FAILED;
                    }


                }

            } else {
                status = Consts.SW_STAT_UNKNOWN_PROTOCOL;
            }

        } catch (Exception ex) {
            status = Consts.SW_STAT_INPUT_PARSE_FAIL;

        } finally {
            if (msgBack == null) {
                msgBack = new CreateResponse();
                status = Consts.SW_STAT_PROCESSING_ERROR;
            }

            msgBack.setStatus(status);
            msgBack.setResponse(msgData);
            long elapsedTime = System.currentTimeMillis() - timeStart;
            msgBack.setLatency(elapsedTime);
        }

        return msgBack;
    }


    @RequestMapping(value = MPC_PATH + "/destroy", method = RequestMethod.POST)
    public GeneralResponse destroy(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        DestroyResponse msgBack = null;
        DestroyResponseData msgData = null;
        int status = Consts.SW_STAT_OK;

        try {
            msgBack = new DestroyResponse();
            JSONObject parsedContent = new JSONObject(jsonStr);
            String instance = parsedContent.getString("instance");
            String password = parsedContent.getString("key");
            ProtocolInstance prot = GlobalConfiguration.isInstance(instance, password);
            if (prot != null) {
                boolean result = GlobalConfiguration.DestroyInstance(prot);
                if (!prot.removeFile()) {
                    LOG.error("Unsuccessful protocol instance delete: {}", prot.getUID());
                }
                msgData = new DestroyResponseData(instance);
                msgData.setDetail();

            } else {
                status = Consts.SW_STAT_UNKNOWN_INSTANCE;
            }

        } catch (Exception ex) {
            status = Consts.SW_STAT_INPUT_PARSE_FAIL;

        } finally {
            if (msgBack == null) {
                msgBack = new DestroyResponse();
                status = Consts.SW_STAT_PROCESSING_ERROR;
            }

            msgBack.setStatus(status);
            msgBack.setResponse(msgData);
            long elapsedTime = System.currentTimeMillis() - timeStart;
            msgBack.setLatency(elapsedTime);
            //msgBack.setLatency(elapsedTime);
        }

        return msgBack;
    }

    @RequestMapping(value = MPC_PATH + "/run", method = RequestMethod.POST)
    public GeneralResponse run(@RequestBody String jsonStr, HttpServletRequest request) {
        JsonEnvelope message = null;
        Long timeStart = System.currentTimeMillis();
        String remoteIPAddress = request.getRemoteAddr();
        RunResponse msgBack = null;
        RunResponseData msgData = null;
        int status = Consts.SW_STAT_OK;

        try {
            msgBack = new RunResponse();
            JSONObject parsedContent = new JSONObject(jsonStr);
            String instance = parsedContent.getString("instance");
            String phase = parsedContent.getString("phase");
            String protocolName = parsedContent.getString("protocol");
            JSONArray parameters = parsedContent.getJSONArray("input");
            HashMap<String, String> params = new HashMap<>();
            for (Object oneParam : parameters) {
                if (oneParam instanceof JSONObject) {
                    JSONObject oneParamJSON = (JSONObject) oneParam;
                    String name = "@" + oneParamJSON.getString("name");
                    String value = oneParamJSON.getString("value");
                    params.put(name, value);
                } else {
                    LOG.error("Input parameters not valid: {}", oneParam);
                }
            }
            ProtocolInstance prot = GlobalConfiguration.isInstance(instance);
            if (GlobalConfiguration.isProtocol(protocolName) && (prot != null) && GlobalConfiguration.isPhase(protocolName, phase)) {
                ProtocolDefinition.Phase detail = GlobalConfiguration.getPhase(protocolName, phase);
                prot.runPhase(phase, params, detail);

                boolean result = GlobalConfiguration.DestroyInstance(prot);
                if (!prot.removeFile()) {
                    LOG.error("Unsuccessful protocol instance delete: {}", prot.getUID());
                }
                msgData = new RunResponseData();
                msgData.setDetail(0, 0, null, null);

            } else {
                status = Consts.SW_STAT_UNKNOWN_INSTANCE;
            }


        } catch (Exception ex) {
            status = Consts.SW_STAT_INPUT_PARSE_FAIL;

        } finally {
            if (msgBack == null) {
                msgBack = new RunResponse();
                status = Consts.SW_STAT_PROCESSING_ERROR;
            }

            msgBack.setStatus(status);
            msgBack.setResponse(msgData);
            long elapsedTime = System.currentTimeMillis() - timeStart;
            msgBack.setLatency(elapsedTime);
            //msgBack.setLatency(elapsedTime);
        }

        return msgBack;
    }


    @RequestMapping(value = MPC_PATH + "/instances", method = RequestMethod.POST)
    public GeneralResponse instances(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = -System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        InstanceResponseData ird;
        GeneralResponse msgBack = null;

        JSONObject parsedContent = new JSONObject(jsonStr);

        HashMap<String, ProtocolInstance> runs = GlobalConfiguration.GetInstances();

        msgBack = new InstanceResponse();
        ird = new InstanceResponseData();
        for (String name: runs.keySet()){
            ird.addInstance(name, runs.get(name));
        }

        msgBack.setResponse(ird);
        timeStart += System.currentTimeMillis();
        msgBack.setLatency(timeStart);

        return msgBack;
    }

}
