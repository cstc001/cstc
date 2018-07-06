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
package org.cstceum.net;

import com.typesafe.config.ConfigFactory;
import org.cstceum.config.NoAutoscan;
import org.cstceum.config.SystemProperties;
import org.cstceum.core.*;
import org.cstceum.crypto.ECKey;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.listener.cstceumListenerAdapter;
import org.cstceum.mine.Ethash;
import org.cstceum.net.eth.handler.Eth62;
import org.cstceum.net.eth.message.*;
import org.cstceum.net.server.Channel;
import org.cstceum.util.RLP;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLacstc;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.cstceum.crypto.HashUtil.sha3;

/**
 * Created by Anton Nashatyrev on 13.10.2015.
 */
@Ignore
public class TwoPeerTest {

    @Configuration
    @NoAutoscan
    public static class SysPropConfig1 {
        static Eth62 testHandler = null;
        @Bean
        @Scope("prototype")
        public Eth62 eth62() {
            return testHandler;
//            return new Eth62();
        }

        static SystemProperties props = new SystemProperties();
        @Bean
        public SystemProperties systemProperties() {
            return props;
        }
    }
    @Configuration
    @NoAutoscan
    public static class SysPropConfig2 {
        static SystemProperties props= new SystemProperties();
        @Bean
        public SystemProperties systemProperties() {
            return props;
        }

    }

    public Block createNextBlock(Block parent, String stateRoot, String extraData) {
        Block b = new Block(parent.getHash(), sha3(RLP.encodeList()), parent.getCoinbase(), parent.getLogBloom(),
                parent.getDifficulty(), parent.getNumber() + 1, parent.getGasLimit(), parent.getGasUsed(),
                System.currentTimeMillis() / 1000, new byte[0], new byte[0], new byte[0],
                parent.getReceiptsRoot(), parent.getTxTrieRoot(),
                Hex.decode(stateRoot),
//                    Hex.decode("7c22bebbe3e6cf5af810bef35ad7a7b8172e0a247eaeb44f63fffbce87285a7a"),
                Collections.<Transaction>emptyList(), Collections.<BlockHeader>emptyList());
        //        b.getHeader().setDifficulty(b.getHeader().calcDifficulty(bestBlock.getHeader()).toByteArray());
        if (extraData != null) {
            b.getHeader().setExtraData(extraData.getBytes());
        }
        return b;
    }

    public Block addNextBlock(BlockchainImpl blockchain1, Block parent, String extraData) {
        Block b = createNextBlock(parent, "00", extraData);
        System.out.println("Adding block.");
//        blockchain1.add(b, new Miner() {
//            @Override
//            public long mine(BlockHeader header) {
//                return Ethash.getForBlock(header.getNumber()).mineLight(header);
//            }
//
//            @Override
//            public boolean validate(BlockHeader header) {
//                return true;
//            }
//        });
        return b;
    }

    @Test
    public void testTest() throws FileNotFoundException, InterruptedException {
        SysPropConfig1.props.overrideParams(
                "peer.listen.port", "30334",
                "peer.privateKey", "3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c",
                // nodeId: 3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6
                "genesis", "genesis-light.json",
                "database.dir", "testDB-1");

        SysPropConfig2.props.overrideParams(ConfigFactory.parseString(
                "peer.listen.port = 30335 \n" +
                        "peer.privateKey = 6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec \n" +
                        "peer.active = [{ url = \"enode://3973cb86d7bef9c96e5d589601d788370f9e24670dcba0480c0b3b1b0647d13d0f0fffed115dd2d4b5ca1929287839dcd4e77bdc724302b44ae48622a8766ee6@localhost:30334\" }] \n" +
                        "sync.enabled = true \n" +
                        "genesis = genesis-light.json \n" +
                        "database.dir = testDB-2 \n"));

        final List<Block> alternativeFork = new ArrayList<>();
        SysPropConfig1.testHandler = new Eth62() {
            @Override
            protected void processGetBlockHeaders(GetBlockHeadersMessage msg) {
                if (msg.getBlockHash() != null) {
                    System.out.println("=== (1)");
                    for (int i = 0; i < alternativeFork.size(); i++) {
                        if (Arrays.equals(msg.getBlockHash(), alternativeFork.get(i).getHash())) {
                            System.out.println("=== (2)");
                            int endIdx = max(0, i - msg.getSkipBlocks());
                            int startIdx = max(0, i - msg.getMaxHeaders());
                            if (!msg.isReverse()) {
                                startIdx = min(alternativeFork.size() - 1, i + msg.getSkipBlocks());
                                endIdx = min(alternativeFork.size() - 1, i + msg.getMaxHeaders());
                            }

                            List<BlockHeader> headers = new ArrayList<>();
                            for (int j = startIdx; j <= endIdx; j++) {
                                headers.add(alternativeFork.get(j).getHeader());
                            }

                            if (msg.isReverse()) {
                                Collections.reverse(headers);
                            }

                            sendMessage(new BlockHeadersMessage(headers));

                            return;
                        }
                    }

                }
                super.processGetBlockHeaders(msg);
            }

            @Override
            protected void processGetBlockBodies(GetBlockBodiesMessage msg) {
                List<byte[]> bodies = new ArrayList<>(msg.getBlockHashes().size());

                for (byte[] hash : msg.getBlockHashes()) {
                    Block block = null;
                    for (Block b : alternativeFork) {
                        if (Arrays.equals(b.getHash(), hash)) {
                            block = b;
                            break;
                        }
                    }
                    if (block == null) {
                        block = blockchain.getBlockByHash(hash);
                    }
                    bodies.add(block.getEncodedBody());
                }

                sendMessage(new BlockBodiesMessage(bodies));
            }
        };

        cstceum cstceum1 = cstceumFactory.createcstceum(SysPropConfig1.props, SysPropConfig1.class);
        BlockchainImpl blockchain = (BlockchainImpl) cstceum1.getBlockchain();
        Block bGen = blockchain.getBestBlock();
        Block b1 = addNextBlock(blockchain, bGen, "chain A");
        Block b2 = addNextBlock(blockchain, b1, null);
        Block b3 = addNextBlock(blockchain, b2, null);
        Block b4 = addNextBlock(blockchain, b3, null);

        List<BlockHeader> listOfHeadersStartFrom = blockchain.getListOfHeadersStartFrom(new BlockIdentifier(null, 3), 0, 100, true);

//        Block b1b = addNextBlock(blockchain, bGen, "chain B");
        Block b1b = createNextBlock(bGen, "7c22bebbe3e6cf5af810bef35ad7a7b8172e0a247eaeb44f63fffbce87285a7a", "chain B");
        Ethash.getForBlock(SystemProperties.getDefault(), b1b.getNumber()).mineLight(b1b);
        Block b2b = createNextBlock(b1b, Hex.toHexString(b2.getStateRoot()), "chain B");
        Ethash.getForBlock(SystemProperties.getDefault(), b2b.getNumber()).mineLight(b2b);

        alternativeFork.add(bGen);
        alternativeFork.add(b1b);
        alternativeFork.add(b2b);

//        byte[] root = ((RepositoryImpl) cstceum.getRepository()).getRoot();
//        ((RepositoryImpl) cstceum.getRepository()).syncToRoot(root);
//        byte[] root1 = ((RepositoryImpl) cstceum.getRepository()).getRoot();
//        Block b2b = addNextBlock(blockchain, b1, "chain B");

        System.out.println("Blocks added");

        cstceum cstceum2 = cstceumFactory.createcstceum(SysPropConfig2.props, SysPropConfig2.class);

        final CountDownLacstc semaphore = new CountDownLacstc(1);

        final Channel[] channel1 = new Channel[1];
        cstceum1.addListener(new cstceumListenerAdapter() {
            @Override
            public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {
                channel1[0] = channel;
                System.out.println("==== Got the Channel: " + channel);
            }
        });
        cstceum2.addListener(new cstceumListenerAdapter() {
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                if (block.getNumber() == 4) {
                    semaphore.countDown();
                }
            }

        });

        System.out.println("======= Waiting for block #4");
        semaphore.await(60, TimeUnit.SECONDS);
        if(semaphore.getCount() > 0) {
            throw new RuntimeException("4 blocks were not imported.");
        }

        System.out.println("======= Sending forked block without parent...");
//        ((EthHandler) channel1[0].getEthHandler()).sendNewBlock(b2b);

//        Block b = b4;
//        for (int i = 0; i < 10; i++) {
//            Thread.sleep(3000);
//            System.out.println("=====  Adding next block...");
//            b = addNextBlock(blockchain, b, null);
//        }

        Thread.sleep(10000000);


        cstceum1.close();
        cstceum2.close();

        System.out.println("Passed.");

    }

    public static void main(String[] args) throws Exception {
        ECKey k = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        System.out.println(Hex.toHexString(k.getPrivKeyBytes()));
        System.out.println(Hex.toHexString(k.getAddress()));
        System.out.println(Hex.toHexString(k.getNodeId()));
    }
}
