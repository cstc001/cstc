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
package org.cstceum.datasource;

import com.typesafe.config.ConfigFactory;
import org.cstceum.config.NoAutoscan;
import org.cstceum.config.SystemProperties;
import org.cstceum.core.Repository;
import org.cstceum.crypto.HashUtil;
import org.cstceum.db.DbFlushManager;
import org.cstceum.db.StateSource;
import org.cstceum.db.prune.Pruner;
import org.cstceum.db.prune.Segment;
import org.cstceum.vm.DataWord;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mikhail Kalinin
 * @since 05.12.2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@NoAutoscan
@Ignore
public class TrieNodeSourceIntegrationTest {

    @Autowired @Qualifier("trieNodeSource")
    Source<byte[], byte[]> trieNodeSource;

    @Autowired
    StateSource stateSource;

    @Autowired @Qualifier("defaultRepository")
    Repository repository;

    @Autowired
    DbFlushManager dbFlushManager;

    @Configuration
    @ComponentScan(basePackages = "org.cstceum")
    @NoAutoscan
    static class ContextConfiguration {

        private final String config =
                // no need for discovery in that small network
                "peer.discovery.enabled = false \n" +
                "sync.enabled = false \n" +
                "genesis = genesis-low-difficulty.json \n";

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    @AfterClass
    public static void cleanup() {
        SystemProperties.resetToDefault();
    }


    @Test
    public void testContractStorage() throws ExecutionException, InterruptedException {

        byte[] addr1 = Hex.decode("5c543e7ae0a1104f78406c340e9c64fd9fce5170");
        byte[] addr2 = Hex.decode("8bccc9ba2e5706e24a36dda02ca2a846e39a7bbf");

        byte[] k1 = HashUtil.sha3("key1".getBytes()), v1 = "value1".getBytes();

        assertNull(trieNodeSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055")));

        repository.addStorageRow(addr1, new DataWord(k1), new DataWord(v1));
        repository.addStorageRow(addr2, new DataWord(k1), new DataWord(v1));
        flushChanges();

        // Logically we must get a node with key 4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055
        // and reference counter equal 2, cause both addresses have that node in their storage.

        // Technically we get two nodes with keys produced by NodeKeyCompositor:
        // 4b7fc4d98630bae2133ad002f743124f01bb4fbefc0a1a699cd4c20899a2877f
        // 4b7fc4d98630bae2133ad002f743124fafe15fa1326e1c0fcd4b67a53eee9537
        // values they hold are equal, logically it is the same value

        assertNull(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055")));

        assertEquals("eaa1202cc45dfd615ef5099974383a7c13606186f629a558453bdb2985d005ad174e73878676616c756531",
                Hex.toHexString(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f01bb4fbefc0a1a699cd4c20899a2877f"))));
        assertEquals("eaa1202cc45dfd615ef5099974383a7c13606186f629a558453bdb2985d005ad174e73878676616c756531",
                Hex.toHexString(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124fafe15fa1326e1c0fcd4b67a53eee9537"))));

        // trieNodeSource must return that value supplied with origin key 4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055
        // it does a prefix search by only first 16 bytes of the key
        assertEquals("eaa1202cc45dfd615ef5099974383a7c13606186f629a558453bdb2985d005ad174e73878676616c756531",
                Hex.toHexString(trieNodeSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055"))));

        // one of that storage rows is gonna be removed
        repository.addStorageRow(addr1, new DataWord(k1), DataWord.ZERO);
        flushChanges();

        // state doesn't contain a copy of node that belongs to addr1
        assertNull(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f01bb4fbefc0a1a699cd4c20899a2877f")));
        // but still able to pick addr2 value
        assertEquals("eaa1202cc45dfd615ef5099974383a7c13606186f629a558453bdb2985d005ad174e73878676616c756531",
                Hex.toHexString(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124fafe15fa1326e1c0fcd4b67a53eee9537"))));
        // trieNodeSource still able to pick a value by origin key
        assertEquals("eaa1202cc45dfd615ef5099974383a7c13606186f629a558453bdb2985d005ad174e73878676616c756531",
                Hex.toHexString(trieNodeSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055"))));

        // remove a copy of value stick to addr2
        repository.addStorageRow(addr2, new DataWord(k1), DataWord.ZERO);
        flushChanges();

        // no source can resolve any of those keys
        assertNull(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f01bb4fbefc0a1a699cd4c20899a2877f")));
        assertNull(stateSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124fafe15fa1326e1c0fcd4b67a53eee9537")));
        assertNull(trieNodeSource.get(Hex.decode("4b7fc4d98630bae2133ad002f743124f2ef5d8167f094af0c2b82d3476604055")));
    }

    @Test
    public void testContractCode() throws ExecutionException, InterruptedException {

        byte[] addr1 = Hex.decode("5c543e7ae0a1104f78406c340e9c64fd9fce5170");
        byte[] addr2 = Hex.decode("8bccc9ba2e5706e24a36dda02ca2a846e39a7bbf");

        byte[] code = "contract Foo {}".getBytes();
        byte[] codeHash = HashUtil.sha3(code);

        repository.saveCode(addr1, code);
        flushChanges();

        assertNull(stateSource.get(codeHash));
        assertEquals(Hex.toHexString(code),
                Hex.toHexString(stateSource.get(Hex.decode("0827ccfec1b70192ffadbc46e945a9af01bb4fbefc0a1a699cd4c20899a2877f"))));
        assertNull(stateSource.get(Hex.decode("0827ccfec1b70192ffadbc46e945a9afafe15fa1326e1c0fcd4b67a53eee9537")));
        assertEquals(Hex.toHexString(code), Hex.toHexString(trieNodeSource.get(codeHash)));

        repository.saveCode(addr2, code);
        flushChanges();

        assertNull(stateSource.get(codeHash));
        assertEquals(Hex.toHexString(code),
                Hex.toHexString(stateSource.get(Hex.decode("0827ccfec1b70192ffadbc46e945a9af01bb4fbefc0a1a699cd4c20899a2877f"))));
        assertEquals(Hex.toHexString(code),
                Hex.toHexString(stateSource.get(Hex.decode("0827ccfec1b70192ffadbc46e945a9afafe15fa1326e1c0fcd4b67a53eee9537"))));
        assertEquals(Hex.toHexString(code), Hex.toHexString(trieNodeSource.get(codeHash)));
    }

    private void flushChanges() throws ExecutionException, InterruptedException {
        repository.commit();
        stateSource.getJournalSource().commitUpdates(HashUtil.EMPTY_DATA_HASH);

        Pruner pruner = new Pruner(stateSource.getJournalSource().getJournal(), stateSource.getNoJournalSource());
        pruner.init(HashUtil.EMPTY_DATA_HASH);
        Segment segment = new Segment(0, HashUtil.EMPTY_DATA_HASH, HashUtil.EMPTY_DATA_HASH);
        segment.startTracking()
                .addMain(1, HashUtil.EMPTY_DATA_HASH, HashUtil.EMPTY_DATA_HASH)
                .commit();
        pruner.prune(segment);

        dbFlushManager.flush().get();
    }
}
