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
package org.cstceum.mine;

import org.cstceum.config.NoAutoscan;
import org.cstceum.config.SystemProperties;
import org.cstceum.config.net.MainNetConfig;
import org.cstceum.core.Block;
import org.cstceum.core.BlockSummary;
import org.cstceum.core.Blockchain;
import org.cstceum.core.ImportResult;
import org.cstceum.core.Transaction;
import org.cstceum.crypto.ECKey;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.facade.cstceumImpl;
import org.cstceum.facade.SyncStatus;
import org.cstceum.listener.cstceumListenerAdapter;
import org.cstceum.net.eth.handler.Eth62;
import org.cstceum.net.rlpx.Node;
import org.cstceum.net.server.Channel;
import org.cstceum.util.FastByteComparisons;
import org.cstceum.util.blockchain.cstcUtil;
import org.cstceum.util.blockchain.StandaloneBlockchain;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLacstc;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cstceum.crypto.HashUtil.sha3;
import static org.cstceum.util.FileUtil.recursiveDelete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.spongycastle.util.encoders.Hex.decode;


/**
 * If Miner is started manually, sync status is not changed in any manner,
 * so if miner is started while the peer is in Long Sync mode, miner creates
 * new blocks but ignores any txs, because they are dropped by {@link org.cstceum.core.PendingStateImpl}
 * While automatic detection of long sync works correctly in any live network
 * with big number of peers, automatic detection of Short Sync condition in
 * detached or small private networks looks not doable.
 *
 * To resolve this and any other similar issues manual swicstc to Short Sync mode
 * was added: {@link cstceumImpl#swicstcToShortSync()}
 * This test verifies that manual swicstcing to Short Sync works correctly
 * and solves miner issue.
 */
@Ignore("Long network tests")
public class SyncDoneTest {

    private static Node nodeA;
    private static List<Block> mainB1B10;

    private cstceum cstceumA;
    private cstceum cstceumB;
    private String testDbA;
    private String testDbB;

    private final static int MAX_SECONDS_WAIT = 60;

    @BeforeClass
    public static void setup() throws IOException, URISyntaxException {
        nodeA = new Node("enode://3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6@localhost:30334");

        SysPropConfigA.props.overrideParams(
                "peer.listen.port", "30334",
                "peer.privateKey", "3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c",
                "genesis", "genesis-light-old.json",
                "sync.enabled", "true",
                "mine.fullDataSet", "false"
        );

        SysPropConfigB.props.overrideParams(
                "peer.listen.port", "30335",
                "peer.privateKey", "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec",
                "genesis", "genesis-light-old.json",
                "sync.enabled", "true",
                "mine.fullDataSet", "false"
        );

        mainB1B10 = loadBlocks("sync/main-b1-b10.dmp");
    }

    private static List<Block> loadBlocks(String path) throws URISyntaxException, IOException {

        URL url = ClassLoader.getSystemResource(path);
        File file = new File(url.toURI());
        List<String> strData = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        List<Block> blocks = new ArrayList<>(strData.size());
        for (String rlp : strData) {
            blocks.add(new Block(decode(rlp)));
        }

        return blocks;
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.getDefault().setBlockchainConfig(MainNetConfig.INSTANCE);
    }

    @Before
    public void setupTest() throws InterruptedException {
        testDbA = "test_db_" + new BigInteger(32, new Random());
        testDbB = "test_db_" + new BigInteger(32, new Random());

        SysPropConfigA.props.setDataBaseDir(testDbA);
        SysPropConfigB.props.setDataBaseDir(testDbB);
    }

    @After
    public void cleanupTest() {
        recursiveDelete(testDbA);
        recursiveDelete(testDbB);
        SysPropConfigA.eth62 = null;
    }

    // positive gap, A on main, B on main
    // expected: B downloads missed blocks from A => B on main
    @Test
    public void test1() throws InterruptedException {

        setupPeers();

        // A == B == genesis

        Blockchain blockchainA = (Blockchain) cstceumA.getBlockchain();

        for (Block b : mainB1B10) {
            ImportResult result = blockchainA.tryToConnect(b);
            System.out.println(result.isSuccessful());
        }

        long loadedBlocks = blockchainA.getBestBlock().getNumber();

        // Check that we are synced and on the same block
        assertTrue(loadedBlocks > 0);
        final CountDownLacstc semaphore = new CountDownLacstc(1);
        cstceumB.addListener(new cstceumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks) {
                    semaphore.countDown();
                }
            }
        });
        semaphore.await(MAX_SECONDS_WAIT, SECONDS);
        Assert.assertEquals(0, semaphore.getCount());
        assertEquals(loadedBlocks, cstceumB.getBlockchain().getBestBlock().getNumber());

        cstceumA.getBlockMiner().startMining();

        final CountDownLacstc semaphore2 = new CountDownLacstc(2);
        cstceumB.addListener(new cstceumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks + 2) {
                    semaphore2.countDown();
                    cstceumA.getBlockMiner().stopMining();
                }
            }
        });
        cstceumA.addListener(new cstceumListenerAdapter(){
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks + 2) {
                    semaphore2.countDown();
                }
            }
        });
        semaphore2.await(MAX_SECONDS_WAIT, SECONDS);
        Assert.assertEquals(0, semaphore2.getCount());

        assertFalse(cstceumA.getSyncStatus().getStage().equals(SyncStatus.SyncStage.Complete));
        assertTrue(cstceumB.getSyncStatus().getStage().equals(SyncStatus.SyncStage.Complete)); // Receives NEW_BLOCKs from cstceumA

        // Trying to include txs while miner is on long sync
        // Txs should be dropped as peer is not on short sync
        ECKey sender = ECKey.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
        Transaction tx = Transaction.create(
                Hex.toHexString(ECKey.fromPrivate(sha3("cow".getBytes())).getAddress()),
                cstcUtil.convert(1, cstcUtil.Unit.cstc),
                BigInteger.ZERO,
                BigInteger.valueOf(cstceumA.getGasPrice()),
                new BigInteger("3000000"),
                null
        );
        tx.sign(sender);
        final CountDownLacstc txSemaphore = new CountDownLacstc(1);
        cstceumA.addListener(new cstceumListenerAdapter(){
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (!blockSummary.getBlock().getTransactionsList().isEmpty() &&
                        FastByteComparisons.equal(blockSummary.getBlock().getTransactionsList().get(0).getSender(), sender.getAddress()) &&
                        blockSummary.getReceipts().get(0).isSuccessful()) {
                    txSemaphore.countDown();
                }
            }
        });
        cstceumB.submitTransaction(tx);

        final CountDownLacstc semaphore3 = new CountDownLacstc(2);
        cstceumB.addListener(new cstceumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks + 5) {
                    semaphore3.countDown();
                }
            }
        });
        cstceumA.addListener(new cstceumListenerAdapter(){
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks + 5) {
                    semaphore3.countDown();
                    cstceumA.getBlockMiner().stopMining();
                }
            }
        });
        cstceumA.getBlockMiner().startMining();

        semaphore3.await(MAX_SECONDS_WAIT, SECONDS);
        Assert.assertEquals(0, semaphore3.getCount());

        Assert.assertEquals(loadedBlocks + 5, cstceumA.getBlockchain().getBestBlock().getNumber());
        Assert.assertEquals(loadedBlocks + 5, cstceumB.getBlockchain().getBestBlock().getNumber());
        assertFalse(cstceumA.getSyncStatus().getStage().equals(SyncStatus.SyncStage.Complete));
        // Tx was not included, because miner is on long sync
        assertFalse(txSemaphore.getCount() == 0);

        cstceumA.getBlockMiner().startMining();
        try {
            cstceumA.swicstcToShortSync().get(1, TimeUnit.SECONDS);
        } cacstc (ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        assertTrue(cstceumA.getSyncStatus().getStage().equals(SyncStatus.SyncStage.Complete));
        Transaction tx2 = Transaction.create(
                Hex.toHexString(ECKey.fromPrivate(sha3("cow".getBytes())).getAddress()),
                cstcUtil.convert(1, cstcUtil.Unit.cstc),
                BigInteger.ZERO,
                BigInteger.valueOf(cstceumA.getGasPrice()).add(BigInteger.TEN),
                new BigInteger("3000000"),
                null
        );
        tx2.sign(sender);
        cstceumB.submitTransaction(tx2);

        final CountDownLacstc semaphore4 = new CountDownLacstc(2);
        cstceumB.addListener(new cstceumListenerAdapter() {
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks + 9) {
                    semaphore4.countDown();
                    cstceumA.getBlockMiner().stopMining();
                }
            }
        });
        cstceumA.addListener(new cstceumListenerAdapter(){
            @Override
            public void onBlock(BlockSummary blockSummary) {
                if (blockSummary.getBlock().getNumber() == loadedBlocks + 9) {
                    semaphore4.countDown();
                }
            }
        });

        semaphore4.await(MAX_SECONDS_WAIT, SECONDS);
        Assert.assertEquals(0, semaphore4.getCount());
        Assert.assertEquals(loadedBlocks + 9, cstceumA.getBlockchain().getBestBlock().getNumber());
        Assert.assertEquals(loadedBlocks + 9, cstceumB.getBlockchain().getBestBlock().getNumber());
        assertTrue(cstceumA.getSyncStatus().getStage().equals(SyncStatus.SyncStage.Complete));
        assertTrue(cstceumB.getSyncStatus().getStage().equals(SyncStatus.SyncStage.Complete));
        // Tx is included!
        assertTrue(txSemaphore.getCount() == 0);
    }

    private void setupPeers() throws InterruptedException {

        cstceumA = cstceumFactory.createcstceum(SysPropConfigA.props, SysPropConfigA.class);
        cstceumB = cstceumFactory.createcstceum(SysPropConfigB.props, SysPropConfigB.class);

        final CountDownLacstc semaphore = new CountDownLacstc(1);

        cstceumB.addListener(new cstceumListenerAdapter() {
            @Override
            public void onPeerAddedToSyncPool(Channel peer) {
                semaphore.countDown();
            }
        });

        cstceumB.connect(nodeA);

        semaphore.await(10, SECONDS);
        if(semaphore.getCount() > 0) {
            fail("Failed to set up peers");
        }
    }

    @Configuration
    @NoAutoscan
    public static class SysPropConfigA {
        static SystemProperties props = new SystemProperties();
        static Eth62 eth62 = null;

        @Bean
        public SystemProperties systemProperties() {
            props.setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());
            return props;
        }

        @Bean
        @Scope("prototype")
        public Eth62 eth62() throws IllegalAccessException, InstantiationException {
            if (eth62 != null) return eth62;
            return new Eth62();
        }
    }

    @Configuration
    @NoAutoscan
    public static class SysPropConfigB {
        static SystemProperties props = new SystemProperties();

        @Bean
        public SystemProperties systemProperties() {
            props.setBlockchainConfig(StandaloneBlockchain.getEasyMiningConfig());
            return props;
        }
    }
}
