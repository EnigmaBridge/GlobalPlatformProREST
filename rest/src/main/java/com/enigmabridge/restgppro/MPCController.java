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
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AtomicInteger noOfInstances = new AtomicInteger(0);
    private final AtomicInteger tps = new AtomicInteger(0);
    private final AtomicInteger currentTPS = new AtomicInteger(0);
    private final AtomicLong currentTPStime = new AtomicLong(0);
    Object lock1 = new Object();

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
                    msgData.setDetail((test < 0) ? 0 : test, size, protocol,
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
                        msgData.setDetail(size, size, protocol,
                                instanceProcessors);
                        noOfInstances.incrementAndGet();
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

                if (result) {
                    noOfInstances.decrementAndGet();
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
            HashMap<String, String> params = new HashMap<>();
            ProtocolInstance prot = GlobalConfiguration.isInstance(instance);

            if (prot != null) {
                if (!parsedContent.isNull("input")) {
                    JSONArray parameters = parsedContent.getJSONArray("input");
                    for (Object oneParam : parameters) {
                        if (oneParam instanceof JSONObject) {
                            JSONObject oneParamJSON = (JSONObject) oneParam;
                            String name = "@" + oneParamJSON.getString("name");
                            String value = oneParamJSON.getString("value");
                            prot.addParameter(name, value);
                        } else {
                            LOG.error("Input parameters not valid: {}", oneParam);
                        }
                    }
                }

                String name = "@processors";
                String value = Integer.toHexString(prot.getCardKeys().size());
                if ((value.length() % 2) == 1) {
                    value = "0" + value;
                }
                prot.addParameter(name, value);
            }
            if (!GlobalConfiguration.isProtocol(protocolName) || (prot == null)) {
                msgBack.setError("Protocol not known");
                status = Consts.SW_STAT_UNKNOWN_INSTANCE;
            } else if (!GlobalConfiguration.isPhase(protocolName, phase)) {
                msgBack.setError("Protocol phase not known");
                status = Consts.SW_STAT_UNKNOWN_PHASE;
            } else {
                ProtocolDefinition.Phase detail = GlobalConfiguration.getPhase(protocolName, phase);
                Pair<HashMap<String, String[][]>, HashMap<String, Long[][]>> allResult = prot.runPhase(phase, detail);

                if (allResult == null) {
                    msgBack.setError("Protocol phase not known");
                    status = Consts.SW_STAT_UNKNOWN_PHASE;
                } else {
                    msgData = new RunResponseData();
                    msgData.setProtocol(prot.getProtocolName());
                    msgData.setInstance(prot.getUID());
                    msgData.setSize(prot.getSize());
                    msgData.setDetail(allResult.getL(), allResult.getR());

                    synchronized (lock1) {
                        // update TPS stats

                        long timeNow = System.currentTimeMillis();
                        // update every 1000 ms
                        if ((timeNow - currentTPStime.get()) > 1000) {
                            LOG.error("TPS reset: {} {} {}", timeNow - currentTPStime.get(), tps.get(), currentTPS.get());
                            currentTPS.set((int) (1000 * tps.get() / (timeNow - currentTPStime.get())));
                            tps.set(1);
                            currentTPStime.set(timeNow);
                        } else {
                            LOG.error("TPS cont : {} {} {}", timeNow - currentTPStime.get(), tps.get(), currentTPS.get());
                            tps.incrementAndGet();
                        }
                    }


                }
            }


        } catch (Exception ex) {
            status = Consts.SW_STAT_INPUT_PARSE_FAIL;
            msgBack.setError(ex.getMessage());

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
        for (String name : runs.keySet()) {
            ird.addInstance(name, runs.get(name));
        }

        msgBack.setResponse(ird);
        timeStart += System.currentTimeMillis();
        msgBack.setLatency(timeStart);

        return msgBack;
    }

    @RequestMapping(value = MPC_PATH + "/stats", method = RequestMethod.POST)
    public GeneralResponse stats(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = -System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        StatsResponseData ird;
        GeneralResponse msgBack = null;

        JSONObject parsedContent = new JSONObject(jsonStr);

        int tps_local = 0;
        synchronized (lock1) {
            // update TPS stats
            long timeNow = System.currentTimeMillis();
            // update every 1000 ms
            tps_local = currentTPS.get();
            if ((timeNow - currentTPStime.get()) > 1000) {
                LOG.error("TPS reset 2: {} {} {}", timeNow - currentTPStime.get(), tps.get(), currentTPS.get());
                tps_local = (int) (1000 * tps.get() / (timeNow - currentTPStime.get()));
                currentTPS.set(tps_local);
                tps.set(0);
                currentTPStime.set(timeNow);
            }
        }

        msgBack = new StatsResponse();
        ird = new StatsResponseData();
        ird.addTPS(currentTPS.get());

        msgBack.setResponse(ird);
        timeStart += System.currentTimeMillis();
        msgBack.setLatency(timeStart);

        return msgBack;
    }


    @RequestMapping(value = MPC_PATH + "/evil", method = RequestMethod.POST)
    public GeneralResponse evil(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = -System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        StatsResponseData ird;
        GeneralResponse msgBack = null;
        int status = Consts.SW_STAT_OK;
        RunResponseData msgData = null;

        try {
            msgBack = new RunResponse();
            JSONObject parsedContent = new JSONObject(jsonStr);

            String instance = parsedContent.getString("instance");
            String phase = parsedContent.getString("phase");
            String protocolName = parsedContent.getString("protocol");

            HashMap<String, String> params = new HashMap<>();
            ProtocolInstance prot = GlobalConfiguration.isInstance(instance);

            if (prot != null) {
                if (!parsedContent.isNull("input")) {
                    JSONArray parameters = parsedContent.getJSONArray("input");
                    for (Object oneParam : parameters) {
                        if (oneParam instanceof JSONObject) {
                            JSONObject oneParamJSON = (JSONObject) oneParam;
                            String name = "@" + oneParamJSON.getString("name");
                            String value = oneParamJSON.getString("value");
                            prot.addParameter(name, value);
                        } else {
                            LOG.error("Input parameters not valid: {}", oneParam);
                        }
                    }
                }

                String name = "@processors";
                String value = Integer.toHexString(prot.getCardKeys().size());
                if ((value.length() % 2) == 1) {
                    value = "0" + value;
                }
                prot.addParameter(name, value);
            }
            if (!GlobalConfiguration.isProtocol(protocolName) || (prot == null)) {
                msgBack.setError("Protocol not known");
                status = Consts.SW_STAT_UNKNOWN_INSTANCE;
            } else if (!GlobalConfiguration.isPhase(protocolName, phase)) {
                msgBack.setError("Protocol phase not known");
                status = Consts.SW_STAT_UNKNOWN_PHASE;
            } else {
                ProtocolDefinition.Phase detail = GlobalConfiguration.getPhase(protocolName, phase);
                Pair<HashMap<String, String[][]>, HashMap<String, Long[][]>> allResult = prot.runPhase(phase, detail);

                if (allResult == null) {
                    msgBack.setError("Protocol phase not known");
                    status = Consts.SW_STAT_UNKNOWN_PHASE;
                } else {
                    msgData = new RunResponseData();
                    msgData.setProtocol(prot.getProtocolName());
                    msgData.setInstance(prot.getUID());
                    msgData.setSize(prot.getSize());
                    msgData.setDetail(allResult.getL(), allResult.getR());
                }
            }


        } catch (Exception ex) {
            status = Consts.SW_STAT_INPUT_PARSE_FAIL;
            msgBack.setError(ex.getMessage());

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

}
