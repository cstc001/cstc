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

import org.cstceum.core.*;
import org.cstceum.net.eth.message.StatusMessage;
import org.cstceum.net.message.Message;
import org.cstceum.net.p2p.HelloMessage;
import org.cstceum.net.rlpx.Node;
import org.cstceum.net.server.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Roman Mandeleil
 * @since 12.11.2014
 */
@Component(value = "cstceumListener")
public class CompositecstceumListener implements cstceumListener {

    private static abstract class RunnableInfo implements Runnable {
        private cstceumListener listener;
        private String info;

        public RunnableInfo(cstceumListener listener, String info) {
            this.listener = listener;
            this.info = info;
        }

        @Override
        public String toString() {
            return "RunnableInfo: " + info + " [listener: " + listener.getClass() + "]";
        }
    }

    @Autowired
    EventDispacstcThread eventDispacstcThread = EventDispacstcThread.getDefault();
    
    List<cstceumListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(cstceumListener listener) {
        listeners.add(listener);
    }
    public void removeListener(cstceumListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void trace(final String output) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "trace") {
                @Override
                public void run() {
                    listener.trace(output);
                }
            });
        }
    }

    @Override
    public void onBlock(final BlockSummary blockSummary) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onBlock") {
                @Override
                public void run() {
                    listener.onBlock(blockSummary);
                }
            });
        }
    }

    @Override
    public void onRecvMessage(final Channel channel, final Message message) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onRecvMessage") {
                @Override
                public void run() {
                    listener.onRecvMessage(channel, message);
                }
            });
        }
    }

    @Override
    public void onSendMessage(final Channel channel, final Message message) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onSendMessage") {
                @Override
                public void run() {
                    listener.onSendMessage(channel, message);
                }
            });
        }
    }

    @Override
    public void onPeerDisconnect(final String host, final long port) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onPeerDisconnect") {
                @Override
                public void run() {
                    listener.onPeerDisconnect(host, port);
                }
            });
        }
    }

    @Override
    public void onPendingTransactionsReceived(final List<Transaction> transactions) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onPendingTransactionsReceived") {
                @Override
                public void run() {
                    listener.onPendingTransactionsReceived(transactions);
                }
            });
        }
    }

    @Override
    public void onPendingStateChanged(final PendingState pendingState) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onPendingStateChanged") {
                @Override
                public void run() {
                    listener.onPendingStateChanged(pendingState);
                }
            });
        }
    }

    @Override
    public void onSyncDone(final SyncState state) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onSyncDone") {
                @Override
                public void run() {
                    listener.onSyncDone(state);
                }
            });
        }
    }

    @Override
    public void onNoConnections() {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onNoConnections") {
                @Override
                public void run() {
                    listener.onNoConnections();
                }
            });
        }
    }

    @Override
    public void onHandShakePeer(final Channel channel, final HelloMessage helloMessage) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onHandShakePeer") {
                @Override
                public void run() {
                    listener.onHandShakePeer(channel, helloMessage);
                }
            });
        }
    }

    @Override
    public void onVMTraceCreated(final String transactionHash, final String trace) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onVMTraceCreated") {
                @Override
                public void run() {
                    listener.onVMTraceCreated(transactionHash, trace);
                }
            });
        }
    }

    @Override
    public void onNodeDiscovered(final Node node) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onNodeDiscovered") {
                @Override
                public void run() {
                    listener.onNodeDiscovered(node);
                }
            });
        }
    }

    @Override
    public void onEthStatusUpdated(final Channel channel, final StatusMessage status) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onEthStatusUpdated") {
                @Override
                public void run() {
                    listener.onEthStatusUpdated(channel, status);
                }
            });
        }
    }

    @Override
    public void onTransactionExecuted(final TransactionExecutionSummary summary) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onTransactionExecuted") {
                @Override
                public void run() {
                    listener.onTransactionExecuted(summary);
                }
            });
        }
    }

    @Override
    public void onPeerAddedToSyncPool(final Channel peer) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onPeerAddedToSyncPool") {
                @Override
                public void run() {
                    listener.onPeerAddedToSyncPool(peer);
                }
            });
        }
    }

    @Override
    public void onPendingTransactionUpdate(final TransactionReceipt txReceipt, final PendingTransactionState state,
                                           final Block block) {
        for (final cstceumListener listener : listeners) {
            eventDispacstcThread.invokeLater(new RunnableInfo(listener, "onPendingTransactionUpdate") {
                @Override
                public void run() {
                    listener.onPendingTransactionUpdate(txReceipt, state, block);
                }
            });
        }
    }
}