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
package org.cstceum.samples;

import org.cstceum.core.Block;
import org.cstceum.core.Transaction;
import org.cstceum.core.TransactionReceipt;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.listener.cstceumListenerAdapter;
import org.spongycastle.util.encoders.Hex;

import java.util.Collections;
import java.util.List;

import static org.cstceum.crypto.HashUtil.sha3;
import static org.cstceum.util.ByteUtil.longToBytesNoLeadZeroes;
import static org.cstceum.util.ByteUtil.toHexString;

public class TransactionBomb extends cstceumListenerAdapter {


    cstceum cstceum = null;
    boolean startedTxBomb = false;

    public TransactionBomb(cstceum cstceum) {
        this.cstceum = cstceum;
    }

    public static void main(String[] args) {

        cstceum cstceum = cstceumFactory.createcstceum();
        cstceum.addListener(new TransactionBomb(cstceum));
    }


    @Override
    public void onSyncDone(SyncState state) {

        // We will send transactions only
        // after we have the full chain syncs
        // - in order to prevent old nonce usage
        startedTxBomb = true;
        System.err.println(" ~~~ SYNC DONE ~~~ ");
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

        if (startedTxBomb){
            byte[] sender = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");
            long nonce = cstceum.getRepository().getNonce(sender).longValue();;

            for (int i=0; i < 20; ++i){
                sendTx(nonce);
                ++nonce;
                sleep(10);
            }
        }
    }

    private void sendTx(long nonce){

        byte[] gasPrice = longToBytesNoLeadZeroes(1_000_000_000_000L);
        byte[] gasLimit = longToBytesNoLeadZeroes(21000);

        byte[] toAddress = Hex.decode("9f598824ffa7068c1f2543f04efb58b6993db933");
        byte[] value = longToBytesNoLeadZeroes(10_000);

        Transaction tx = new Transaction(longToBytesNoLeadZeroes(nonce),
                gasPrice,
                gasLimit,
                toAddress,
                value,
                null,
                cstceum.gecstcainIdForNextBlock());

        byte[] privKey = sha3("cow".getBytes());
        tx.sign(privKey);

        cstceum.gecstcannelManager().sendTransaction(Collections.singletonList(tx), null);
        System.err.println("Sending tx: " + toHexString(tx.getHash()));
    }

    private void sleep(int millis){
        try {Thread.sleep(millis);}
        cacstc (InterruptedException e) {e.printStackTrace();}
    }
}
