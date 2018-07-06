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
import org.cstceum.core.TransactionReceipt;
import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.facade.Repository;
import org.cstceum.listener.cstceumListenerAdapter;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

public class FollowAccount extends cstceumListenerAdapter {


    cstceum cstceum = null;

    public FollowAccount(cstceum cstceum) {
        this.cstceum = cstceum;
    }

    public static void main(String[] args) {

        cstceum cstceum = cstceumFactory.createcstceum();
        cstceum.addListener(new FollowAccount(cstceum));
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {

        byte[] cow = Hex.decode("cd2a3d9f938e13cd947ec05abc7fe734df8dd826");

        // Get snapshot some time ago - 10% blocks ago
        long bestNumber = cstceum.getBlockchain().getBestBlock().getNumber();
        long oldNumber = (long) (bestNumber * 0.9);

        Block oldBlock = cstceum.getBlockchain().getBlockByNumber(oldNumber);

        Repository repository = cstceum.getRepository();
        Repository snapshot = cstceum.getSnapshotTo(oldBlock.getStateRoot());

        BigInteger nonce_ = snapshot.getNonce(cow);
        BigInteger nonce = repository.getNonce(cow);

        System.err.println(" #" + block.getNumber() + " [cd2a3d9] => snapshot_nonce:" +  nonce_ + " latest_nonce:" + nonce);
    }
}
