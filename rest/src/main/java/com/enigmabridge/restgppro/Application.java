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

import com.enigmabridge.restgppro.utils.AppletStatus;
import com.enigmabridge.restgppro.utils.Consts;
import com.enigmabridge.restgppro.utils.GlobalConfiguration;
import com.enigmabridge.restgppro.utils.NamedThreadFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@org.springframework.context.annotation.Configuration
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final String ROUTER_RELOAD_EXECUTOR = "reloadExecutor";
    private static final String SERVER_RESYNC_EXECUTOR = "resyncExecutor";
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

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
        LOG.info("Reading global configuration file from", globalPathAbs);
        if (globalPathAbs != null) {
            if (!ReadGlobalConfiguration(globalFile)) {
                LOG.error("Global configuration file not found or invalid", globalPathAbs);
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

        if (status == SW_STAT_OK) {
            SpringApplication.run(Application.class, args);
        }
    }

    private static void UpdateAppletStates() {

        for (String keys : GlobalConfiguration.getCardAppletsIDs()) {
            LinkedList<AppletStatus> instances = GlobalConfiguration.getAppletInstaces(keys);
            for (AppletStatus oneInstance : instances) {
                String request = oneInstance.getCommand() + " -a "
                        + GlobalConfiguration.getSelectCommand(keys) + " -a "
                        + GlobalConfiguration.getStatusAPDU();

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
                            if (line.substring(line.length()-4).equals("9000")) {
                                GlobalConfiguration.parseAppletStatus(oneInstance, line);
                            }
                            counting = -1;
                        }
                        String[] lineParts = line.split(":");
                        if (lineParts[0].equalsIgnoreCase("Applet")) {

                        }
                    }
                } catch (Exception e) {
                    LOG.error("Exception in GPTool", errout);
                }
            }
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
                    Runnable oneSC = () -> {
                        String localReader = reader;
                        String localCmd = readerCmd;
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
                            final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));

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
                            }
                        } catch (Exception e) {
                        }
                    };
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
                    LOG.warn("Creating protocol folder", protFolder.getCanonicalPath());
                    if (!protFolder.mkdirs()) {
                        LOG.error("Creating protocol folder failed");
                        ok = false;
                    }
                }
                if (protFolder.exists() && protFolder.isDirectory()) {
                    GlobalConfiguration.setProtocolFolder(protFolder.getCanonicalPath());
                } else {
                    LOG.error("Protocol folder not found", protFolder.getCanonicalPath());
                    ok = false;
                }
                if (ok) {
                    String instanceFolder = json.getString("instancefolder");
                    File instFolder = new File(instanceFolder);
                    if (!instFolder.exists()) {
                        LOG.warn("Creating instance folder", instFolder.getCanonicalPath());
                        if (!instFolder.mkdirs()) {
                            LOG.error("Creating instance folder failed");
                            ok = false;
                        }
                    }

                    if (instFolder.exists() && instFolder.isDirectory()) {
                        GlobalConfiguration.setInstanceFolder(instFolder.getCanonicalPath());
                    } else {
                        LOG.error("Instance folder not found", instFolder.getCanonicalPath());
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
                                LOG.error("Global configuration file - reader name is not a string", oneReader);
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
                            LOG.error("Global configuration file - smartcardio for readers doesn't exist", fSmartCardIO.getCanonicalPath());
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
                                LOG.error("Global configuration file - simona address is not a string", oneReader);
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
                            LOG.error("Global configuration file - smartcardio for simonas doesn't exist", fSmartCardIO.getCanonicalPath());
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
