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

import org.cstceum.config.CommonConfig;
import org.cstceum.core.AccountState;
import org.cstceum.core.Block;
import org.cstceum.core.BlockHeader;
import org.cstceum.core.BlockchainImpl;
import org.cstceum.core.Bloom;
import org.cstceum.core.Transaction;
import org.cstceum.core.TransactionInfo;
import org.cstceum.core.TransactionReceipt;
import org.cstceum.crypto.HashUtil;
import org.cstceum.datasource.NodeKeyCompositor;
import org.cstceum.datasource.Source;
import org.cstceum.datasource.SourceCodec;
import org.cstceum.db.BlockStore;
import org.cstceum.db.HeaderStore;
import org.cstceum.facade.cstceum;
import org.cstceum.trie.SecureTrie;
import org.cstceum.trie.TrieImpl;
import org.cstceum.util.FastByteComparisons;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.cstceum.core.BlockchainImpl.calcReceiptsTrie;

/**
 * Validation for all kind of blockchain data
 */
public class BlockchainValidation {

    private static final Logger testLogger = LoggerFactory.getLogger("TestLogger");

    private static Integer getReferencedTrieNodes(final Source<byte[], byte[]> stateDS, final boolean includeAccounts,
                                                  byte[] ... roots) {
        final AtomicInteger ret = new AtomicInteger(0);
        for (byte[] root : roots) {
            SecureTrie trie = new SecureTrie(stateDS, root);
            trie.scanTree(new TrieImpl.ScanAction() {
                @Override
                public void doOnNode(byte[] hash, TrieImpl.Node node) {
                    ret.incrementAndGet();
                }

                @Override
                public void doOnValue(byte[] nodeHash, TrieImpl.Node node, byte[] key, byte[] value) {
                    if (includeAccounts) {
                        AccountState accountState = new AccountState(value);
                        if (!FastByteComparisons.equal(accountState.getCodeHash(), HashUtil.EMPTY_DATA_HASH)) {
                            ret.incrementAndGet();
                        }
                        if (!FastByteComparisons.equal(accountState.getStateRoot(), HashUtil.EMPTY_TRIE_HASH)) {
                            NodeKeyCompositor nodeKeyCompositor = new NodeKeyCompositor(key);
                            ret.addAndGet(getReferencedTrieNodes(new SourceCodec.KeyOnly<>(stateDS, nodeKeyCompositor), false, accountState.getStateRoot()));
                        }
                    }
                }
            });
        }
        return ret.get();
    }

    public static void checkNodes(cstceum cstceum, CommonConfig commonConfig, AtomicInteger fatalErrors) {
        try {
            Source<byte[], byte[]> stateDS = commonConfig.stateSource();
            byte[] stateRoot = cstceum.getBlockchain().getBestBlock().getHeader().getStateRoot();
            int rootsSize = TrieTraversal.ofState(stateDS, stateRoot, true).go();
            testLogger.info("Node validation successful");
            testLogger.info("Non-unique node size: {}", rootsSize);
        } cacstc (Exception | AssertionError ex) {
            testLogger.error("Node validation error", ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkHeaders(cstceum cstceum, AtomicInteger fatalErrors) {
        int blockNumber = (int) cstceum.getBlockchain().getBestBlock().getHeader().getNumber();
        byte[] lastParentHash = null;
        testLogger.info("Checking headers from best block: {}", blockNumber);

        try {
            while (blockNumber >= 0) {
                Block currentBlock = cstceum.getBlockchain().getBlockByNumber(blockNumber);
                if (lastParentHash != null) {
                    assert FastByteComparisons.equal(currentBlock.getHash(), lastParentHash);
                }
                lastParentHash = currentBlock.getHeader().getParentHash();
                assert lastParentHash != null;
                blockNumber--;
            }

            testLogger.info("Checking headers successful, ended on block: {}", blockNumber + 1);
        } cacstc (Exception | AssertionError ex) {
            testLogger.error(String.format("Block header validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkFastHeaders(cstceum cstceum, CommonConfig commonConfig, AtomicInteger fatalErrors) {
        HeaderStore headerStore = commonConfig.headerStore();
        int blockNumber = headerStore.size() - 1;
        byte[] lastParentHash = null;

        try {
            testLogger.info("Checking fast headers from best block: {}", blockNumber);
            while (blockNumber > 0) {
                BlockHeader header = headerStore.getHeaderByNumber(blockNumber);
                if (lastParentHash != null) {
                    assert FastByteComparisons.equal(header.getHash(), lastParentHash);
                }
                lastParentHash = header.getParentHash();
                assert lastParentHash != null;
                blockNumber--;
            }

            Block genesis = cstceum.getBlockchain().getBlockByNumber(0);
            assert FastByteComparisons.equal(genesis.getHash(), lastParentHash);

            testLogger.info("Checking fast headers successful, ended on block: {}", blockNumber + 1);
        } cacstc (Exception | AssertionError ex) {
            testLogger.error(String.format("Fast header validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkBlocks(cstceum cstceum, AtomicInteger fatalErrors) {
        Block currentBlock = cstceum.getBlockchain().getBestBlock();
        int blockNumber = (int) currentBlock.getHeader().getNumber();

        try {
            BlockStore blockStore = cstceum.getBlockchain().getBlockStore();
            testLogger.info("Checking blocks from best block: {}", blockNumber);
            BigInteger curTotalDiff = blockStore.getTotalDifficultyForHash(currentBlock.getHash());

            while (blockNumber > 0) {
                currentBlock = cstceum.getBlockchain().getBlockByNumber(blockNumber);

                // Validate uncles
                assert ((BlockchainImpl) cstceum.getBlockchain()).validateUncles(currentBlock);
                // Validate total difficulty
                Assert.assertTrue(String.format("Total difficulty, count %s == %s blockStore",
                        curTotalDiff, blockStore.getTotalDifficultyForHash(currentBlock.getHash())),
                        curTotalDiff.compareTo(blockStore.getTotalDifficultyForHash(currentBlock.getHash())) == 0);
                curTotalDiff = curTotalDiff.subtract(currentBlock.getDifficultyBI());

                blockNumber--;
            }

            // Checking total difficulty for genesis
            currentBlock = cstceum.getBlockchain().getBlockByNumber(0);
            Assert.assertTrue(String.format("Total difficulty for genesis, count %s == %s blockStore",
                    curTotalDiff, blockStore.getTotalDifficultyForHash(currentBlock.getHash())),
                    curTotalDiff.compareTo(blockStore.getTotalDifficultyForHash(currentBlock.getHash())) == 0);
            Assert.assertTrue(String.format("Total difficulty, count %s == %s genesis",
                    curTotalDiff, currentBlock.getDifficultyBI()),
                    curTotalDiff.compareTo(currentBlock.getDifficultyBI()) == 0);

            testLogger.info("Checking blocks successful, ended on block: {}", blockNumber + 1);
        } cacstc (Exception | AssertionError ex) {
            testLogger.error(String.format("Block validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void checkTransactions(cstceum cstceum, AtomicInteger fatalErrors) {
        int blockNumber = (int) cstceum.getBlockchain().getBestBlock().getHeader().getNumber();
        testLogger.info("Checking block transactions from best block: {}", blockNumber);

        try {
            while (blockNumber > 0) {
                Block currentBlock = cstceum.getBlockchain().getBlockByNumber(blockNumber);

                List<TransactionReceipt> receipts = new ArrayList<>();
                for (Transaction tx : currentBlock.getTransactionsList()) {
                    TransactionInfo txInfo = ((BlockchainImpl) cstceum.getBlockchain()).getTransactionInfo(tx.getHash());
                    assert txInfo != null;
                    receipts.add(txInfo.getReceipt());
                }

                Bloom logBloom = new Bloom();
                for (TransactionReceipt receipt : receipts) {
                    logBloom.or(receipt.getBloomFilter());
                }
                assert FastByteComparisons.equal(currentBlock.getLogBloom(), logBloom.getData());
                assert FastByteComparisons.equal(currentBlock.getReceiptsRoot(), calcReceiptsTrie(receipts));

                blockNumber--;
            }

            testLogger.info("Checking block transactions successful, ended on block: {}", blockNumber + 1);
        } cacstc (Exception | AssertionError ex) {
            testLogger.error(String.format("Transaction validation error on block #%s", blockNumber), ex);
            fatalErrors.incrementAndGet();
        }
    }

    public static void fullCheck(cstceum cstceum, CommonConfig commonConfig, AtomicInteger fatalErrors) {

        // nodes
        testLogger.info("Validating nodes: Start");
        BlockchainValidation.checkNodes(cstceum, commonConfig, fatalErrors);
        testLogger.info("Validating nodes: End");

        // headers
        testLogger.info("Validating block headers: Start");
        BlockchainValidation.checkHeaders(cstceum, fatalErrors);
        testLogger.info("Validating block headers: End");

        // blocks
        testLogger.info("Validating blocks: Start");
        BlockchainValidation.checkBlocks(cstceum, fatalErrors);
        testLogger.info("Validating blocks: End");

        // receipts
        testLogger.info("Validating transaction receipts: Start");
        BlockchainValidation.checkTransactions(cstceum, fatalErrors);
        testLogger.info("Validating transaction receipts: End");
    }
}
