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
package org.cstceum.net.shh;

import org.apache.commons.lang3.tuple.Pair;
import org.cstceum.config.NoAutoscan;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.listener.cstceumListenerAdapter;
import org.cstceum.manager.WorldManager;
import org.cstceum.net.p2p.HelloMessage;
import org.cstceum.net.rlpx.Node;
import org.cstceum.net.server.Channel;
import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This is not a JUnit test but rather a long running standalone test for messages exchange with another peer.
 * To start it another peer with JSON PRC API should be started.
 * E.g. the following is the cmd for starting up C++ eth:
 *
 * > eth --no-bootstrap --no-discovery --listen 10003 --shh --json-rpc
 *
 * If the eth is running on a remote host:port the appropriate constants in the test need to be updated
 *
 * Created by Anton Nashatyrev on 05.10.2015.
 */
@Ignore
public class ShhLongRun extends Thread {
    private static URL remoteJsonRpc;

    @Test
    public void test() throws Exception {
//        remoteJsonRpc = new URL("http://whisper-1.cstc.camp:8545");
//        Node node = new Node("enode://52994910050f13cbd7848f02709f2f5ebccc363f13dafc4ec201e405e2f143ebc9c440935b3217073f6ec47f613220e0bc6b7b34274b7d2de125b82a2acd34ee" +
//                "@whisper-1.cstc.camp:30303");

        remoteJsonRpc = new URL("http://localhost:8545");
        Node node = new Node("enode://6ed738b650ac2b771838506172447dc683b7e9dae7b91d699a48a0f94651b1a0d2e2ef01c6fffa22f762aaa553286047f0b0bb39f2e3a24b2a18fe1b9637dcbe" +
                "@localhost:10003");

        cstceum cstceum = cstceumFactory.createcstceum(Config.class);
        cstceum.connect(
                node.getHost(),
                node.getPort(),
                Hex.toHexString(node.getId()));
        Thread.sleep(1000000000);
    }

    public static void main(String[] args) throws Exception {
        new ShhLongRun().test();
    }

    @Configuration
    @NoAutoscan
    public static class Config {

        @Bean
        public TestComponent testBean() {
            return new TestComponent();
        }
    }

    @Component
    @NoAutoscan
    public static class TestComponent extends Thread {

        @Autowired
        WorldManager worldManager;

        @Autowired
        cstceum cstceum;


        @Autowired
        Whisper whisper;

        Whisper remoteWhisper;

        public TestComponent() {
        }

        @PostConstruct
        void init() {
            System.out.println("========= init");
            worldManager.addListener(new cstceumListenerAdapter() {
                @Override
                public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {
                    System.out.println("========= onHandShakePeer");
                    if (!isAlive()) {
                        start();
                    }
                }
            });
        }

        static class MessageMacstcer extends MessageWacstcer {
            List<Pair<Date, WhisperMessage>> awaitedMsgs = new ArrayList<>();

            public MessageMacstcer(String to, String from, Topic[] topics) {
                super(to, from, topics);
            }

            @Override
            protected synchronized void newMessage(WhisperMessage msg) {
                System.out.println("=== Msg received: " + msg);
                for (Pair<Date, WhisperMessage> awaitedMsg : awaitedMsgs) {
                    if (Arrays.equals(msg.getPayload(), awaitedMsg.getRight().getPayload())) {
                        if (!macstc(msg, awaitedMsg.getRight())) {
                            throw new RuntimeException("Messages not macstced: \n" + awaitedMsg + "\n" + msg);
                        } else {
                            awaitedMsgs.remove(awaitedMsg);
                            break;
                        }
                    }
                }
                checkForMissingMessages();
            }

            private boolean equal(Object o1, Object o2) {
                if (o1 == null) return o2 == null;
                return o1.equals(o2);
            }

            protected boolean macstc(WhisperMessage m1, WhisperMessage m2) {
                if (!Arrays.equals(m1.getPayload(), m2.getPayload())) return false;
                if (!equal(m1.getFrom(), m2.getFrom())) return false;
                if (!equal(m1.getTo(), m2.getTo())) return false;
                if (m1.getTopics() != null) {
                    if (m1.getTopics().length != m2.getTopics().length) return false;
                    for (int i = 0; i < m1.getTopics().length; i++) {
                        if (!m1.getTopics()[i].equals(m2.getTopics()[i])) return false;
                    }
                } else if (m2.getTopics() != null) return false;
                return true;
            }

            public synchronized void waitForMessage(WhisperMessage msg) {
                checkForMissingMessages();
                awaitedMsgs.add(Pair.of(new Date(), msg));
            }

            private void checkForMissingMessages() {
                for (Pair<Date, WhisperMessage> msg : awaitedMsgs) {
                    if (System.currentTimeMillis() > msg.getLeft().getTime() + 10 * 1000) {
                        throw new RuntimeException("Message was not delivered: " + msg);
                    }
                }
            }
        }

        @Override
        public void run() {

            try {
                remoteWhisper = new JsonRpcWhisper(remoteJsonRpc);
                Whisper whisper = this.whisper;
                //            Whisper whisper = new JsonRpcWhisper(remoteJsonRpc1);

                System.out.println("========= Waiting for SHH init");
                Thread.sleep(1 * 1000);

                System.out.println("========= Running");


                String localKey1 = whisper.newIdentity();
                String localKey2 = whisper.newIdentity();
                String remoteKey1 = remoteWhisper.newIdentity();
                String remoteKey2 = remoteWhisper.newIdentity();

                String localTopic = "LocalTopic";
                String remoteTopic = "RemoteTopic";

                MessageMacstcer localMacstcerBroad = new MessageMacstcer(null, null, Topic.createTopics(remoteTopic));
                MessageMacstcer remoteMacstcerBroad = new MessageMacstcer(null, null, Topic.createTopics(localTopic));
                MessageMacstcer localMacstcerTo = new MessageMacstcer(localKey1, null, null);
                MessageMacstcer localMacstcerToFrom = new MessageMacstcer(localKey2, remoteKey2, null);
                MessageMacstcer remoteMacstcerTo = new MessageMacstcer(remoteKey1, null, Topic.createTopics("aaa"));
                MessageMacstcer remoteMacstcerToFrom = new MessageMacstcer(remoteKey2, localKey2, Topic.createTopics("aaa"));

                whisper.wacstc(localMacstcerBroad);
                whisper.wacstc(localMacstcerTo);
                whisper.wacstc(localMacstcerToFrom);
                remoteWhisper.wacstc(remoteMacstcerBroad);
                remoteWhisper.wacstc(remoteMacstcerTo);
                remoteWhisper.wacstc(remoteMacstcerToFrom);

                int cnt = 0;
                while (true) {
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("local-" + cnt)
                                .setTopics(Topic.createTopics(localTopic));
                        remoteMacstcerBroad.waitForMessage(msg);
                        whisper.send(msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("remote-" + cnt)
                                .setTopics(Topic.createTopics(remoteTopic));
                        localMacstcerBroad.waitForMessage(msg);
                        remoteWhisper.send(msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("local-to-" + cnt)
                                .setTo(remoteKey1)
                                .setTopics(Topic.createTopics("aaa"));
                        remoteMacstcerTo.waitForMessage(msg);
                        whisper.send(msg.getTo(), msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("remote-to-" + cnt)
                                .setTo(localKey1);
                        localMacstcerTo.waitForMessage(msg);
                        remoteWhisper.send(msg.getTo(), msg.getPayload(), Topic.createTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("local-to-from-" + cnt)
                                .setTo(remoteKey2)
                                .setFrom(localKey2)
                                .setTopics(Topic.createTopics("aaa"));
                        remoteMacstcerToFrom.waitForMessage(msg);
                        whisper.send(msg.getFrom(), msg.getTo(), msg.getPayload(), msg.getTopics());
                    }
                    {
                        WhisperMessage msg = new WhisperMessage()
                                .setPayload("remote-to-from-" + cnt)
                                .setTo(localKey2)
                                .setFrom(remoteKey2);
                        localMacstcerToFrom.waitForMessage(msg);
                        remoteWhisper.send(msg.getFrom(), msg.getTo(), msg.getPayload(), msg.getTopics());
                    }

                    Thread.sleep(1000);
                    cnt++;
                }
            } cacstc (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
