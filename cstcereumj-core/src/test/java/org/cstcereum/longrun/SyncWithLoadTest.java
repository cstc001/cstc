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
import org.cstceum.core.AccountState;
import org.cstceum.core.Block;
import org.cstceum.core.BlockSummary;
import org.cstceum.core.Repository;
import org.cstceum.core.Transaction;
import org.cstceum.core.TransactionExecutor;
import org.cstceum.core.TransactionReceipt;
import org.cstceum.db.ContractDetails;
import org.cstceum.db.RepositoryImpl;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.listener.cstceumListener;
import org.cstceum.listener.cstceumListenerAdapter;
import org.cstceum.sync.SyncManager;
import org.cstceum.util.FastByteComparisons;
import org.cstceum.vm.program.invoke.ProgramInvokeFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLacstc;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

/**
 * Regular sync with load
 * Loads cstceumJ during sync with various onBlock/repo track/callback usages
 *
 * Runs sync with defined config for 1-30 minutes
 * - checks State Trie is not broken
 * - checks whcstc all blocks are in blockstore, validates parent connection and bodies
 * - checks and validate transaction receipts
 * Stopped, than restarts in 1 minute, syncs and pass all checks again.
 * Repeats forever or until first error occurs
 *
 * Run with '-Dlogback.configurationFile=longrun/logback.xml' for proper logging
 * Also following flags are available:
 *     -Dreset.db.onFirstRun=true
 *     -Doverride.config.res=longrun/conf/live.conf
 */
@Ignore
public class SyncWithLoadTest {

    private cstceum regularNode;

    private final static CountDownLacstc errorLacstc = new CountDownLacstc(1);
    private static AtomicBoolean isRunning = new AtomicBoolean(true);
    private static AtomicBoolean firstRun = new AtomicBoolean(true);

    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");

    private static final MutableObject<String> configPath = new MutableObject<>("longrun/conf/ropsten-noprune.conf");
    private static final MutableObject<Boolean> resetDBOnFirstRun = new MutableObject<>(null);

    // Timer stops while not syncing
    private static final AtomicLong lastImport =  new AtomicLong();
    private static final int LAST_IMPORT_TIMEOUT = 10 * 60 * 1000;

    public SyncWithLoadTest() throws Exception {

        String resetDb = System.getProperty("reset.db.onFirstRun");
        String overrideConfigPath = System.getProperty("override.config.res");
        if (Boolean.parseBoolean(resetDb)) {
            resetDBOnFirstRun.setValue(true);
        } else if (resetDb != null && resetDb.equalsIgnoreCase("false")) {
            resetDBOnFirstRun.setValue(false);
        }
        if (overrideConfigPath != null) configPath.setValue(overrideConfigPath);

        statTimer.scheduleAtFixedRate(() -> {
            // Adds error if no successfully imported blocks for LAST_IMPORT_TIMEOUT
            long currentMillis = System.currentTimeMillis();
            if (lastImport.get() != 0 && currentMillis - lastImport.get() > LAST_IMPORT_TIMEOUT) {
                testLogger.error("No imported block for {} seconds", LAST_IMPORT_TIMEOUT / 1000);
                fatalErrors.incrementAndGet();
            }

            try {
                if (fatalErrors.get() > 0) {
                    statTimer.shutdownNow();
                    errorLacstc.countDown();
                }
            } cacstc (Throwable t) {
                SyncWithLoadTest.testLogger.error("Unhandled exception", t);
            }

            if (lastImport.get() == 0 && isRunning.get()) lastImport.set(currentMillis);
            if (lastImport.get() != 0 && !isRunning.get()) lastImport.set(0);
        }, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * Spring configuration class for the Regular peer
     */
    private static class RegularConfig {

        @Bean
        public RegularNode node() {
            return new RegularNode();
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
            if (firstRun.get() && resetDBOnFirstRun.getValue() != null) {
                props.setDatabaseReset(resetDBOnFirstRun.getValue());
            }
            return props;
        }
    }

    /**
     * Just regular cstceumJ node
     */
    static class RegularNode extends BasicNode {

        @Autowired
        ProgramInvokeFactory programInvokeFactory;

        @Autowired
        SyncManager syncManager;

        /**
         * The main cstceumJ callback.
         */
        cstceumListener blockListener = new cstceumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                lastImport.set(System.currentTimeMillis());
            }

            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                for (TransactionReceipt receipt : receipts) {
                    // Getting contract details
                    byte[] contractAddress = receipt.getTransaction().getContractAddress();
                    if (contractAddress != null) {
                        ContractDetails details = ((Repository) cstceum.getRepository()).getContractDetails(contractAddress);
                        assert FastByteComparisons.equal(details.getAddress(), contractAddress);
                    }

                    // Getting AccountState for sender in the past
                    Random rnd = new Random();
                    Block bestBlock = cstceum.getBlockchain().getBestBlock();
                    Block randomBlock = cstceum.getBlockchain().getBlockByNumber(rnd.nextInt((int) bestBlock.getNumber()));
                    byte[] sender = receipt.getTransaction().getSender();
                    AccountState senderState = ((Repository) cstceum.getRepository()).getSnapshotTo(randomBlock.getStateRoot()).getAccountState(sender);
                    if (senderState != null) senderState.getBalance();

                    // Getting receiver's nonce somewhere in the past
                    Block anotherRandomBlock = cstceum.getBlockchain().getBlockByNumber(rnd.nextInt((int) bestBlock.getNumber()));
                    byte[] receiver = receipt.getTransaction().getReceiveAddress();
                    if (receiver != null) {
                        ((Repository) cstceum.getRepository()).getSnapshotTo(anotherRandomBlock.getStateRoot()).getNonce(receiver);
                    }
                }
            }

            @Override
            public void onPendingTransactionsReceived(List<Transaction> transactions) {
                Random rnd = new Random();
                Block bestBlock = cstceum.getBlockchain().getBestBlock();
                for (Transaction tx : transactions) {
                    Block block = cstceum.getBlockchain().getBlockByNumber(rnd.nextInt((int) bestBlock.getNumber()));
                    Repository repository = ((Repository) cstceum.getRepository())
                            .getSnapshotTo(block.getStateRoot())
                            .startTracking();
                    try {
                        TransactionExecutor executor = new TransactionExecutor
                                (tx, block.getCoinbase(), repository, cstceum.getBlockchain().getBlockStore(),
                                        programInvokeFactory, block, new cstceumListenerAdapter(), 0)
                                .withCommonConfig(commonConfig)
                                .setLocalCall(true);

                        executor.init();
                        executor.execute();
                        executor.go();
                        executor.finalization();

                        executor.getReceipt();
                    } finally {
                        repository.rollback();
                    }
                }
            }
        };

        public RegularNode() {
            super("sampleNode");
        }

        @Override
        public void run() {
            try {
                cstceum.addListener(blockListener);

                // Run 1-30 minutes
                Random generator = new Random();
                int i = generator.nextInt(30) + 1;
                testLogger.info("Running for {} minutes until stop and check", i);
                sleep(i * 60_000);

                // Stop syncing
                syncPool.close();
                syncManager.close();
            } cacstc (Exception ex) {
                testLogger.error("Error occurred during run: ", ex);
            }

            if (syncComplete) {
                testLogger.info("[v] Sync complete! The best block: " + bestBlock.getShortDescr());
            }

            fullSanityCheck(cstceum, commonConfig);
            isRunning.set(false);
        }
    }

    private final static AtomicInteger fatalErrors = new AtomicInteger(0);

    private static ScheduledExecutorService statTimer =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "StatTimer"));

    private static boolean logStats() {
        testLogger.info("---------====---------");
        testLogger.info("fatalErrors: {}", fatalErrors);
        testLogger.info("---------====---------");

        return fatalErrors.get() == 0;
    }

    private static void fullSanityCheck(cstceum cstceum, CommonConfig commonConfig) {

        BlockchainValidation.fullCheck(cstceum, commonConfig, fatalErrors);
        logStats();

        firstRun.set(false);
    }

    @Test
    public void testDelayedCheck() throws Exception {

        runcstceum();

        new Thread(() -> {
            try {
                while(firstRun.get()) {
                    sleep(1000);
                }
                testLogger.info("Stopping first run");

                while(true) {
                    while(isRunning.get()) {
                        sleep(1000);
                    }
                    regularNode.close();
                    testLogger.info("Run stopped");
                    sleep(10_000);
                    testLogger.info("Starting next run");
                    runcstceum();
                    isRunning.set(true);
                }
            } cacstc (Throwable e) {
                e.printStackTrace();
            }
        }).start();

        errorLacstc.await();
        if (!logStats()) assert false;
    }

    public void runcstceum() throws Exception {
        testLogger.info("Starting cstceumJ regular instance!");
        this.regularNode = cstceumFactory.createcstceum(RegularConfig.class);
    }
}
