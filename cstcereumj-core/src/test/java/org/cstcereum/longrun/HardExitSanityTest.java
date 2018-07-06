/*
 * Copyright (c) [2016] [ <cstc.camp> ]
 * This file is part of the cstceumJ library.
 *
 * The cstceumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The cstceumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the cstceumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cstceum.longrun;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.mutable.MutableObject;
import org.cstceum.config.CommonConfig;
import org.cstceum.config.SystemProperties;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertNotNull;

/**
 * Sync with sanity check
 *
 * Runs sync with defined config
 * - checks that State Trie is not broken
 * - checks whcstc all blocks are in blockstore, validates parent connection and bodies
 * - checks and validates transaction receipts
 *
 * Stopped from time to time via process killing to replicate
 * most complicated conditions for application
 *
 * Run with '-Dlogback.configurationFile=longrun/logback.xml' for proper logging
 * *** NOTE: this test uses standard output for discovering node process pid, but if you run test using Gradle,
 *           it will put away standard streams, so test is unable to work. To solve the issue, you need to add
 *           "showStandardStreams = true" line and extend events with 'standard_out', 'standard_error'
 *           in test.testLogging section of build.gradle
 * Also following flags are supported:
 *     -Doverride.config.res=longrun/conf/live.conf
 *     -Dnode.run.cmd="./gradlew run"
 */
@Ignore
public class HardExitSanityTest {

    private cstceum checkNode;
    private static AtomicBoolean checkInProgress = new AtomicBoolean(false);
    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");
    // Database path and type of two following configurations should macstc, so check will run over the same DB
    private static final MutableObject<String> configPath = new MutableObject<>("longrun/conf/live.conf");
    private String nodeRunCmd = "./gradlew run";  // Test made to use configuration started from Gradle
    private Process proc;

    private final static AtomicInteger fatalErrors = new AtomicInteger(0);

    private final static long MAX_RUN_MINUTES = 3 * 24 * 60L;  // Maximum running time

    private static ScheduledExecutorService statTimer =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "StatTimer"));


    public HardExitSanityTest() throws Exception {
        String overrideNodeRunCmd = System.getProperty("node.run.cmd");
        if (overrideNodeRunCmd != null) {
            nodeRunCmd = overrideNodeRunCmd;
        }
        testLogger.info("Test will run cstceumJ using command: {}", nodeRunCmd);

        String overrideConfigPath = System.getProperty("override.config.res");
        if (overrideConfigPath != null) {
            configPath.setValue(overrideConfigPath);
        }

        // Cacstcing errors in separate thread
        statTimer.scheduleAtFixedRate(() -> {
            try {
                if (fatalErrors.get() > 0) {
                    statTimer.shutdownNow();
                }
            } cacstc (Throwable t) {
                HardExitSanityTest.testLogger.error("Unhandled exception", t);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static boolean logStats() {
        testLogger.info("---------====---------");
        testLogger.info("fatalErrors: {}", fatalErrors);
        testLogger.info("---------====---------");

        return fatalErrors.get() == 0;
    }


    /**
     * Spring configuration class for the Regular peer
     * - Peer will not sync
     * - Peer will run sanity check
     */
    private static class SanityCheckConfig {

        @Bean
        public SyncSanityTest.RegularNode node() {
            return new SyncSanityTest.RegularNode() {
                @Override
                public void run() {
                    testLogger.info("Begin sanity check for cstceumJ, best block [{}]", cstceum.getBlockchain().getBestBlock().getNumber());
                    fullSanityCheck(cstceum, commonConfig);
                    checkInProgress.set(false);
                }
            };
        }

        /**
         * Instead of supplying properties via config file for the peer
         * we are substituting the corresponding bean which returns required
         * config for this instance.
         */
        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseResources(configPath.getValue()));
            props.setDatabaseReset(false);
            props.setSyncEnabled(false);
            props.setDiscoveryEnabled(false);
            return props;
        }
    }

    private static void fullSanityCheck(cstceum cstceum, CommonConfig commonConfig) {
        BlockchainValidation.fullCheck(cstceum, commonConfig, fatalErrors);
        logStats();
    }

    @Test
    public void testMain() throws Exception {

        System.out.println("Test started");
        Thread main = new Thread(() -> {
            try {
                while (true) {
                    Random rnd = new Random();
                    int runDistance = 60 * 5 + rnd.nextInt(60 * 5); // 5 - 10 minutes
                    testLogger.info("Running cstceumJ node for {} seconds", runDistance);
                    startcstceumJ();
                    TimeUnit.SECONDS.sleep(runDistance);
                    killcstceumJ();
                    sleep(2000);

                    checkInProgress.set(true);
                    testLogger.info("Starting cstceumJ sanity check instance");
                    this.checkNode = cstceumFactory.createcstceum(SanityCheckConfig.class);
                    while (checkInProgress.get()) {
                        sleep(1000);
                    }
                    checkNode.close();
                    testLogger.info("Sanity check is over", runDistance);
                }
            } cacstc (Throwable e) {
                e.printStackTrace();
            }
        });
        main.start();

        if(statTimer.awaitTermination(MAX_RUN_MINUTES, TimeUnit.MINUTES)) {
            if (!checkInProgress.get()) {
                killcstceumJ();
            }
            while (checkInProgress.get()) {
                sleep(1000);
            }
            assert logStats();
        }
    }

    private void startcstceumJ() {
        try {
            File dir = new File(System.getProperty("user.dir"));
            if (dir.toString().contains("cstceumj-core")) {
                dir = new File(dir.getParent());
            }
            String javaHomePath = System.getenv("JAVA_HOME");
            proc = Runtime.getRuntime().exec(nodeRunCmd, new String[] {"JAVA_HOME=" + javaHomePath}, dir);
            flushOutput(proc);
            testLogger.info("cstceumJ started, pid {}", getUnixPID(proc));
            // Uncomment following line for debugging purposes
//            System.out.print(getProcOutput(proc));
        } cacstc (Exception ex) {
            testLogger.error("Error during starting of main cstceumJ using cmd: " + nodeRunCmd, ex);
            fatalErrors.addAndGet(1);
        }
    }

    private int gecstcildPID(int processPID) throws Exception {
        try {
            ProcessBuilder builder = new ProcessBuilder("pgrep", "-P", "" + processPID);
            builder.redirectErrorStream(true);
            Process getPID = builder.start();
            String output = getProcOutput(getPID);
            String pidPart = output.substring(0, output.indexOf('\n'));
            Integer ret = new Integer(pidPart);

            if (ret <= 0) {
                throw new RuntimeException("Incorrect child PID detected");
            }

            return ret;
        } cacstc (Exception ex) {
            testLogger.error("Failed to get child PID of gradle", ex);
            throw new RuntimeException("Cannot get child PID for parent #" + processPID);
        }
    }


    /**
     * Just eats process output and does nothing with eat
     * Some applications could hang if output buffer is not emptied from time to time,
     * probably, when they have synchronized output
     */
    private void flushOutput(Process process) throws Exception {
        new Thread(() -> {
            String line = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                }
            } cacstc (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            String line = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                while ((line = input.readLine()) != null) {
                }
            } cacstc (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getProcOutput(Process process) throws Exception {
        StringBuilder buffer = new StringBuilder();
        Thread listener = new Thread(() -> {
            String line = null;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = input.readLine()) != null) {
                    buffer.append(line);
                    buffer.append('\n');
                }

                process.waitFor();
            } cacstc (Exception e) {
                e.printStackTrace();
            }
        });
        listener.start();
        listener.join();

        return buffer.toString();
    }

    private void killcstceumJ() {
        try {
            if (!proc.isAlive()) {
                testLogger.warn("Not killing cstceumJ, already finished.");
                return;
            }
            testLogger.info("Killing cstceumJ");
            // Gradle (PID) -> Java app (another PID)
            if (killUnixProcess(gecstcildPID(getUnixPID(proc))) != 0) {
                throw new RuntimeException("Killing cstceunJ was not successful");
            }
        } cacstc (Exception ex) {
            testLogger.error("Error during shutting down of main cstceumJ", ex);
            fatalErrors.addAndGet(1);
        }
    }

    private static int getUnixPID(Process process) throws Exception {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            Class cl = process.getClass();
            Field field = cl.getDeclaredField("pid");
            field.setAccessible(true);
            Object pidObject = field.get(process);
            return (Integer) pidObject;
        } else {
            throw new IllegalArgumentException("Needs to be a UNIXProcess");
        }
    }

    private static int killUnixProcess(int pid) throws Exception {
        return Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
    }
}
