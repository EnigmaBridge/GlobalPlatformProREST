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

import com.enigmabridge.restgppro.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import pro.javacard.gp.GPArgumentTokenizer;
import pro.javacard.gp.GPTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static com.enigmabridge.restgppro.utils.Consts.*;
import static com.enigmabridge.restgppro.utils.GlobalConfiguration.LOG;

@org.springframework.context.annotation.Configuration
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final String ROUTER_RELOAD_EXECUTOR = "reloadExecutor";
    private static final String SERVER_RESYNC_EXECUTOR = "resyncExecutor";

    @Autowired
    private ErrorAttributes errorAttributes;

    public static void main(String[] args) {
        int status = Consts.SW_STAT_OK;

        // first, let's initialize the server instance by reading configuration data from
        // the disk

        String globalfilename = "global.json";
        File globalFile = new File(globalfilename);
        String globalPathAbs = null;
        try {
            globalPathAbs = globalFile.getCanonicalPath();
        } catch (Exception ex) {
        }
        LOG.info("Reading global configuration file from {}", globalPathAbs);
        if (globalPathAbs != null) {
            if (!ReadGlobalConfiguration(globalFile)) {
                LOG.error("Global configuration file not found or invalid {}", globalPathAbs);
                status = SW_STAT_INVALID_GLOBAL_CONFIG;
            }
        } else {
            LOG.error("Error when creating global file path");
            status = SW_STAT_SYSTEM_ERROR;
        }

        // the next step is to figure out what hardware we have available
        if (status == SW_STAT_OK) {
            if (!ScanReaders()) {
                status = SW_STAT_READERS_ERROR;
            }
            if (!ScanSimonas()) {
                status = SW_STAT_SIMONAS_ERROR;
            }
        }

        // now we have information about all the applets available - let's check which are MPC
        UpdateAppletStates();

        ReadProtocols();

        ReadProtocolInstances();

        if (status == SW_STAT_OK) {
            SpringApplication.run(Application.class, args);
        }
    }

    private static void ReadProtocols() {
        String folderPath = GlobalConfiguration.getProtocolFolder();
        if (folderPath == null) {
            folderPath = ".";
        }
        File folder = new File(folderPath);
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                String oneFile = fileEntry.getName();
                if (!oneFile.endsWith(".json")) {
                    continue;
                }
                LOG.info("Reading instance configuration file from {}", oneFile);
                JSONObject json;
                String lastTag = null;
                try {
                    byte[] encoded = Files.readAllBytes(Paths.get(folderPath + "/" + oneFile));
                    String jsonString = new String(encoded, "UTF-8");
                    json = new JSONObject(jsonString);

                    ProtocolDefinition prot = new ProtocolDefinition();
                    prot.setName(json.getString("protocol"));
                    prot.setAID(json.getString("aid"));

                    //TODO later
                    if (!json.isNull("name")) {
                        json.getJSONArray("names");
                    }

                    JSONArray apdus = json.getJSONArray("apdu");
                    for (Object apdu : apdus) {
                        if (apdu instanceof JSONObject) {
                            String name = ((JSONObject) apdu).getString("name").toLowerCase();
                            String cla = ((JSONObject) apdu).getString("cla");
                            String ins = ((JSONObject) apdu).getString("ins");
                            String p1 = ((JSONObject) apdu).getString("p1");
                            String p2 = ((JSONObject) apdu).getString("p2");
                            lastTag = name;
                            String data;
                            if (((JSONObject) apdu).isNull("data")) {
                                data = null;
                            } else {
                                data = ((JSONObject) apdu).getString("data");
                                if (data.startsWith("@")) {
                                    if (!prot.addData(data)) {
                                        LOG.error("Data in the protocol is not known: {}", data);
                                    }
                                }
                            }
                            String result;
                            if (((JSONObject) apdu).isNull("result")) {
                                result = null;
                            } else {
                                result = ((JSONObject) apdu).getString("result");
                                if (result.startsWith("@")) {
                                    prot.addResult(result);
                                } else {
                                    LOG.info("A result of APDU is not a name {} {}", name, result);
                                }
                            }
                            prot.addInstruction(name, cla, ins, p1, p2, data, result);

                        }
                    }

                    JSONArray phases = json.getJSONArray("phases");
                    for (Object phase : phases) {
                        if (phase instanceof JSONObject) {
                            String phaseName = ((JSONObject) phase).getString("name");
                            lastTag = phaseName;
                            String phaseResult = ((JSONObject) phase).getString("result");
                            JSONArray phaseInput = ((JSONObject) phase).getJSONArray("input");
                            LinkedList<String> inputs = new LinkedList<>();
                            for (Object oneInput : phaseInput) {
                                inputs.add((String) oneInput);
                            }
                            prot.addPhase(phaseName, phaseResult, inputs);
                            LinkedList<String> instructions = new LinkedList<>();
                            JSONArray jsonInstructions = ((JSONObject) phase).getJSONArray("apdus");
                            boolean problem = false;
                            for (Object step : jsonInstructions) {
                                String apdu = ((JSONObject) step).getString("apdu");
                                String from = ((JSONObject) step).getString("from");
                                String to = ((JSONObject) step).getString("to");
                                String result = null;
                                if (((JSONObject) step).has("result")) {
                                    result = ((JSONObject) step).getString("result");
                                } else {
                                    result = null;
                                }
                                if (prot.isParty(from) && prot.isParty(to)) {
                                    prot.addPhaseStep(phaseName, apdu, from, to, result);
                                    LOG.debug("New instruction added to phase {} {}", apdu, phaseName);
                                } else {
                                    problem = true;
                                    LOG.error("Incorrect instruction into phase {} {}", apdu, phaseName);
                                }

                            }
                            if (problem) {
                                prot.removePhase(phaseName);
                            }

                        }
                    }
                    String initIns = json.getString("create");
                    String destroyIns = json.getString("destroy");
                    prot.setInitInstruction(initIns);
                    prot.setDestroyInstruction(destroyIns);

                    if ((prot.getInitInstruction() != null) && (prot.getDestroyInstruction() != null)) {
                        GlobalConfiguration.addProtocol(prot.getName(), prot);
                    } else {
                        LOG.error("Protocol doesn't have init ({}) or destroy ({}) instruction", initIns, destroyIns);
                    }

                    //....
                } catch (Exception ex) {
                    LOG.error("Error reading instance configuration file {} {} {}", lastTag, oneFile, ex.getMessage());
                }
                LOG.info("Finished processing configuration file: {}", oneFile);

            }
        }
    }

    private static void ReadProtocolInstances() {
        String folderPath = GlobalConfiguration.getInstanceFolder();
        if (folderPath == null) {
            folderPath = ".";
        }
        File folder = new File(folderPath);
        for (File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                String oneFile = fileEntry.getName();
                if (!oneFile.endsWith(".json")) {
                    continue;
                }

                LOG.info("Reading instance configuration file from: {}", oneFile);

                try {
                    byte[] encoded = Files.readAllBytes(Paths.get(folderPath + "/" + oneFile));
                    String jsonString = new String(encoded, "UTF-8");
                    JSONObject json = new JSONObject(jsonString);

                    ProtocolInstance prot = new ProtocolInstance();

                    prot.setProcessors(json.getInt("processors"));
                    prot.setID(json.getString("id"));
                    prot.setProtocol(json.getString("protocol"));
                    for (Object member : json.getJSONArray("group")) {
                        if (member instanceof JSONObject) {
                            String cardID = ((JSONObject) member).getString("id");
                            String readerName = ((JSONObject) member).getString("reader");
                            int index = ((JSONObject) member).getInt("index");
                            if (!prot.addProcessor(cardID, readerName, index)){
                                LOG.error("Error finding a processor for a protocol instance: {} {}",
                                        readerName, prot.getID());
                            }
                        }
                    }
                    // we are not reading "status" - take it from cards
                    if (json.has("status")) {
                        if (!json.isNull("status")) {
                            json.getJSONArray("status");
                        }
                    }

                    if (!json.isNull("result")) {
                        for (Object member : json.getJSONArray("result")) {
                            if (member instanceof JSONObject) {
                                String name = ((JSONObject) member).getString("name");
                                String value = ((JSONObject) member).getString("value");
                                prot.addResult(name, value);
                            }
                        }
                    }

                    if (prot.isCardNumberCorrect()) {
                        GlobalConfiguration.addInstance(prot.getID(), prot);
                    } else {
                        LOG.error("Incorrect number of cards in configuration file: {}", oneFile);
                    }

                } catch (Exception ex) {
                    LOG.error("Error reading instance configuration file {}, {}", oneFile, ex.getMessage());
                }
                LOG.info("Finished processing configuration file: {}", oneFile);

            }
        }
    }

    private static void UpdateAppletStates() {

        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS, queue);

        for (String keys : GlobalConfiguration.getCardAppletsIDs()) {
            LinkedList<AppletStatus> instances = GlobalConfiguration.getAppletInstances(keys);
            for (AppletStatus oneInstance : instances) {
                RunnableGetStates oneSC = new RunnableGetStates(keys, oneInstance);
                executor.execute(oneSC);
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }

    private static boolean ScanSimonas() {
        boolean ok = true;
        //"simonasmartcardio.jar:smarthsmfast.simona.Simonaio:bin%40tcp%3A%2F%2F"+ipaddress

        String commandLine = "--bs 246 -d -terminals ";
        if (GlobalConfiguration.getSimonaIO() != null) {
            commandLine += GlobalConfiguration.getSimonaIO();
        }
        // the cli is ready - we will now have to just add ipaddress
        for (String oneIP : GlobalConfiguration.getSimonaIPs()) {
            String request = commandLine + oneIP;
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream errout = new ByteArrayOutputStream();

            PrintStream psout = new PrintStream(stdout);
            PrintStream pserr = new PrintStream(errout);
            GPTool tool = new GPTool(psout, pserr);
            List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
            try {
                final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));

                // lets' now parse the output
                String[] outputLines = stdout.toString("UTF-8").split("\\r?\\n");
                for (String line : outputLines) {
                    line = line.trim();
                    if (line.startsWith("[*]")) {
                        GlobalConfiguration.addSimonaReader(oneIP, line.substring(4));
                    }
                }
            } catch (Exception e) {
                ok = false;
            }

        }

        // let's check smart card readers
        if (ok) {
            BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
            ThreadPoolExecutor executor = new ThreadPoolExecutor(200, 1000, 10, TimeUnit.SECONDS, queue);
            // cmd = javacmd + "-d -l -terminals " + terminalloader + " --dump " + filename + " -r " + self.card + " -v"
            for (String simona : GlobalConfiguration.getSimonaIPs()) {
                String readerCmd = commandLine + simona + " -r ";
                for (String reader : GlobalConfiguration.getSimonaReaders(simona)) {
                    // cmd = javacmd + "-d -l --dump " + filename + " -r " + self.card + " -v"
                    RunnableListApplets oneSC = new RunnableListApplets(reader, readerCmd);
                    executor.execute(oneSC);
                }
            }
            executor.shutdown();
            try {
                executor.awaitTermination(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }

        }

        return ok;
    }

    private static boolean ScanReaders() {
        boolean ok = true;
        //"simonasmartcardio.jar:smarthsmfast.simona.Simonaio:bin%40tcp%3A%2F%2F"+ipaddress

        String commandLine = "--bs 246 -d";
        if (GlobalConfiguration.getReaderIO() != null) {
            commandLine += GlobalConfiguration.getReaderIO();
        }
        // the cli is ready - we will now have to just add ipaddress
        String request = commandLine;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream errout = new ByteArrayOutputStream();

        PrintStream psout = new PrintStream(stdout);
        PrintStream pserr = new PrintStream(errout);
        GPTool tool = new GPTool(psout, pserr);
        List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        try {
            final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));

            // lets' now parse the output
            String[] outputLines = stdout.toString("UTF-8").split("\\r?\\n");
            for (String line : outputLines) {
                line = line.trim();
                if (line.startsWith("[ ]")) {
                    GlobalConfiguration.addEmptyReader(line.substring(4));
                } else if (line.startsWith("[*]")) {
                    GlobalConfiguration.addReader(line.substring(4));
                }
            }
        } catch (Exception e) {
            ok = false;
        }

        // let's check smartcards' content
        if (ok) {
            String readerCmd = commandLine + " -r ";
            for (String reader : GlobalConfiguration.getReaders()) {
                // cmd = javacmd + "-d -l --dump " + filename + " -r " + self.card + " -v"
                request = readerCmd + "\"" + reader + "\"";
                AppletStatus appletStatus = new AppletStatus();
                appletStatus.setCommand(request);
                appletStatus.setStatus(-1);
                appletStatus.setReader(reader);
                request += " -l";

                stdout = new ByteArrayOutputStream();
                errout = new ByteArrayOutputStream();

                psout = new PrintStream(stdout);
                pserr = new PrintStream(errout);
                tool = new GPTool(psout, pserr);
                inputArgs = GPArgumentTokenizer.tokenize(request);
                try {
                    final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));

                    // lets' now parse the output
                    String[] outputLines = stdout.toString("UTF-8").split("\\r?\\n");
                    for (String line : outputLines) {
                        line = line.trim();
                        String[] lineParts = line.split(":");
                        if (lineParts[0].trim().equalsIgnoreCase("Applet")) {
                            if (!lineParts[1].trim().startsWith("A000")) {
                                GlobalConfiguration.addApplet(reader, lineParts[1].trim(), appletStatus);
                            }
                        }
                    }
                } catch (Exception e) {
                    ok = false;
                }
            }

        }


        return ok;
    }

    private static boolean ReadGlobalConfiguration(File globalFile) {
        boolean ok = true;
        if (globalFile.exists()) {
            String path = globalFile.getAbsolutePath();
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(path));
                String jsonString = new String(encoded, "UTF-8");
                JSONObject json = new JSONObject(jsonString);
                String protocolFolder = json.getString("protocolfolder");
                File protFolder = new File(protocolFolder);
                if (!protFolder.exists()) {
                    LOG.warn("Creating protocol folder {}", protFolder.getCanonicalPath());
                    if (!protFolder.mkdirs()) {
                        LOG.error("Creating protocol folder failed {}");
                        ok = false;
                    }
                }
                if (protFolder.exists() && protFolder.isDirectory()) {
                    GlobalConfiguration.setProtocolFolder(protFolder.getCanonicalPath());
                } else {
                    LOG.error("Protocol folder not found: {}", protFolder.getCanonicalPath());
                    ok = false;
                }
                if (ok) {
                    String instanceFolder = json.getString("instancefolder");
                    File instFolder = new File(instanceFolder);
                    if (!instFolder.exists()) {
                        LOG.warn("Creating instance folder {}", instFolder.getCanonicalPath());
                        if (!instFolder.mkdirs()) {
                            LOG.error("Creating instance folder failed");
                            ok = false;
                        }
                    }

                    if (instFolder.exists() && instFolder.isDirectory()) {
                        GlobalConfiguration.setInstanceFolder(instFolder.getCanonicalPath());
                    } else {
                        LOG.error("Instance folder not found: {}", instFolder.getCanonicalPath());
                        ok = false;
                    }
                }

                // let's try to read info about readers
                if (ok) {
                    JSONObject readers = json.getJSONObject("readers");
                    boolean bScan = readers.getBoolean("use");
                    JSONArray aList;
                    if (readers.isNull("list")) {
                        aList = null;
                    } else {
                        aList = readers.getJSONArray("list");
                    }
                    String sSmartCardIO = null;
                    if (!readers.isNull("smartcardio")) {
                        sSmartCardIO = readers.getString("smartcardio");
                    }
                    GlobalConfiguration.setReaderUse(bScan);
                    GlobalConfiguration.setReaderIO(sSmartCardIO);
                    LinkedList<String> listOfReaders = new LinkedList<>();
                    if (aList != null) {
                        for (Object oneReader : aList) {
                            if (oneReader instanceof String) {
                                listOfReaders.add((String) oneReader);
                            } else {
                                LOG.error("Global configuration file - reader name is not a string: {}", oneReader);
                                ok = false;
                            }
                        }
                        GlobalConfiguration.setReaderSet(listOfReaders);
                    }

                    // test if the file exists
                    if (ok && (sSmartCardIO != null)) {
                        String[] pathAndRest = sSmartCardIO.split(":");
                        File fSmartCardIO = new File(pathAndRest[0]);
                        if ((!fSmartCardIO.exists()) || (!fSmartCardIO.isFile())) {
                            LOG.error("Global configuration file - smartcardio for readers doesn't exist: {}", fSmartCardIO.getCanonicalPath());
                            ok = false;
                        }
                    }
                }


                // and info about simona boards
                if (ok) {
                    JSONObject simonas = json.getJSONObject("simonas");
                    boolean bScan = simonas.getBoolean("scan");
                    JSONArray aList = null;
                    if (!simonas.isNull("addresses")) {
                        aList = simonas.getJSONArray("addresses");
                    }

                    String sSmartCardIO = null;
                    if (!simonas.isNull("smartcardio")) {
                        sSmartCardIO = simonas.getString("smartcardio");
                    }

                    GlobalConfiguration.setSimonaUse(bScan);
                    GlobalConfiguration.setSimonaIO(sSmartCardIO);
                    LinkedList<String> listOfReaders = new LinkedList<>();
                    if (aList != null) {
                        for (Object oneReader : aList) {
                            if (oneReader instanceof String) {
                                listOfReaders.add((String) oneReader);
                            } else {
                                ok = false;
                                LOG.error("Global configuration file - simona address is not a string: {}", oneReader);
                            }
                        }
                        GlobalConfiguration.setSimonaSet(listOfReaders);
                    }

                    if (bScan && listOfReaders.isEmpty()) {
                        LOG.error("Global configuration file - misconfiguration, empty Simona list");
                        ok = false;
                    }
                    // test if the file exists
                    if (ok && (sSmartCardIO != null)) {
                        String[] pathAndRest = sSmartCardIO.split(":");
                        File fSmartCardIO = new File(pathAndRest[0]);
                        if ((!fSmartCardIO.exists()) || (!fSmartCardIO.isFile())) {
                            LOG.error("Global configuration file - smartcardio for simonas doesn't exist: {}", fSmartCardIO.getCanonicalPath());
                            ok = false;
                        }
                    }
                }

            } catch (IOException e) {
                LOG.error("Global configuration file - error reading (OS)");
                ok = false;
            } catch (Exception e) {
                LOG.error("Global configuration file - JSON error ");
                ok = false;
            }
        } else {
            LOG.error("Global configuration file doesn't exist");
            ok = false;
        }
        return ok;
    }

    @Bean
    public AppErrorController appErrorController() {
        return new AppErrorController(errorAttributes);
    }

    /*
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setPort(8081);
        factory.setSessionTimeout(50, TimeUnit.MINUTES);
        return factory;
    }*/

    @Override
    public void run(String... args) throws Exception {

        //update/create configuration file
        LOG.info("Started...");
    }

    @Bean(name = ROUTER_RELOAD_EXECUTOR)
    public Executor reloadExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("router-reload-exec"));
    }

    @Bean(name = SERVER_RESYNC_EXECUTOR)
    public Executor resyncExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("server-resync-exec"));
    }
}
