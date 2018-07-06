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
package org.cstceum.listener;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.cstceum.core.*;
import org.cstceum.db.BlockStore;
import org.cstceum.db.TransactionStore;
import org.cstceum.net.eth.message.StatusMessage;
import org.cstceum.net.message.Message;
import org.cstceum.net.p2p.HelloMessage;
import org.cstceum.net.rlpx.Node;
import org.cstceum.net.server.Channel;
import org.cstceum.util.FastByteComparisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.cstceum.sync.BlockDownloader.MAX_IN_REQUEST;

/**
 * Class capable of replaying stored blocks prior to 'going online' and
 * notifying on newly imported blocks
 *
 * All other cstceumListener events are just forwarded to the supplied listener.
 *
 * For example of usage, look at {@link org.cstceum.samples.EventListenerSample}
 *
 * Created by Anton Nashatyrev on 18.07.2016.
 */
public class BlockReplay extends cstceumListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("events");
    private static final int HALF_BUFFER = MAX_IN_REQUEST;

    BlockStore blockStore;
    TransactionStore transactionStore;

    cstceumListener listener;

    long firstBlock;

    boolean replayComplete = false;
    Block lastReplayedBlock;
    CircularFifoQueue<BlockSummary> onBlockBuffer = new CircularFifoQueue<>(HALF_BUFFER * 2);

    public BlockReplay(BlockStore blockStore, TransactionStore transactionStore, cstceumListener listener, long firstBlock) {
        this.blockStore = blockStore;
        this.transactionStore = transactionStore;
        this.listener = listener;
        this.firstBlock = firstBlock;
    }

    /**
     * Replay blocks asynchronously
     */
    public void replayAsync() {
        new Thread(this::replay).start();
    }

    /**
     * Replay blocks synchronously
     */
    public void replay() {
        long lastBlock = blockStore.getMaxNumber();
        logger.info("Replaying blocks from " + firstBlock + ", current best block: " + lastBlock);
        int cnt = 0;
        long num = firstBlock;
        while(!replayComplete) {
            for (; num <= lastBlock; num++) {
                replayBlock(num);
                cnt++;
                if (cnt % 1000 == 0) {
                    logger.info("Replayed " + cnt + " blocks so far. Current block: " + num);
                }
            }

            synchronized (this) {
                if (onBlockBuffer.size() < onBlockBuffer.maxSize()) {
                    replayComplete = true;
                } else {
                    // So we'll have half of the buffer for new blocks until not synchronized replay finish
                    long newLastBlock = blockStore.getMaxNumber() - HALF_BUFFER;
                    if (lastBlock >= newLastBlock) {
                        replayComplete = true;
                    } else {
                        lastBlock = newLastBlock;
                    }
                }
            }
        }
        logger.info("Replay complete.");
    }

    private void replayBlock(long num) {
        Block block = blockStore.gecstcainBlockByNumber(num);
        lastReplayedBlock = block;
        List<TransactionReceipt> receipts = new ArrayList<>();
        for (Transaction tx : block.getTransactionsList()) {
            TransactionInfo info = transactionStore.get(tx.getHash(), block.getHash());
            TransactionReceipt receipt = info.getReceipt();
            receipt.setTransaction(tx);
            receipts.add(receipt);
        }
        BlockSummary blockSummary = new BlockSummary(block, null, receipts, null);
        blockSummary.setTotalDifficulty(BigInteger.valueOf(num));
        listener.onBlock(blockSummary);
    }

    @Override
    public synchronized void onBlock(BlockSummary blockSummary) {
        if (replayComplete) {
            if (onBlockBuffer.isEmpty()) {
                listener.onBlock(blockSummary);
            } else {
                logger.info("Replaying cached " + onBlockBuffer.size() + " blocks...");
                boolean lastBlockFound = lastReplayedBlock == null || onBlockBuffer.size() < onBlockBuffer.maxSize();
                for (BlockSummary block : onBlockBuffer) {
                    if (!lastBlockFound) {
                        lastBlockFound = FastByteComparisons.equal(block.getBlock().getHash(), lastReplayedBlock.getHash());
                    } else {
                        listener.onBlock(block);
                    }
                }
                onBlockBuffer.clear();
                listener.onBlock(blockSummary);
                logger.info("Cache replay complete. Swicstcing to online mode.");
            }
        } else {
            onBlockBuffer.add(blockSummary);
        }
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt transactionReceipt, PendingTransactionState pendingTransactionState, Block block) {
        listener.onPendingTransactionUpdate(transactionReceipt, pendingTransactionState, block);
    }

    @Override
    public void onPeerDisconnect(String s, long l) {
        listener.onPeerDisconnect(s, l);
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> list) {
        listener.onPendingTransactionsReceived(list);
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
        listener.onPendingStateChanged(pendingState);
    }

    @Override
    public void onSyncDone(SyncState state) {
        listener.onSyncDone(state);
    }

    @Override
    public void onNoConnections() {
        listener.onNoConnections();
    }

    @Override
    public void onVMTraceCreated(String s, String s1) {
        listener.onVMTraceCreated(s, s1);
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary transactionExecutionSummary) {
        listener.onTransactionExecuted(transactionExecutionSummary);
    }

    @Override
    public void onPeerAddedToSyncPool(Channel channel) {
        listener.onPeerAddedToSyncPool(channel);
    }

    @Override
    public void trace(String s) {
        listener.trace(s);
    }

    @Override
    public void onNodeDiscovered(Node node) {
        listener.onNodeDiscovered(node);
    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {
        listener.onHandShakePeer(channel, helloMessage);
    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {
        listener.onEthStatusUpdated(channel, statusMessage);
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
        listener.onRecvMessage(channel, message);
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
        listener.onSendMessage(channel, message);
    }
}
