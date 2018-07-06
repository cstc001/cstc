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

import com.google.common.util.concurrent.ListenableFuture;
import org.cstceum.core.Block;
import org.cstceum.core.BlockHeader;

import java.util.Collection;

/**
 * Mine algorithm interface
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public interface MinerIfc {

    /**
     * Starts mining the block. On successful mining the Block is update with necessary nonce and hash.
     * @return MiningResult Future object. The mining can be canceled via this Future. The Future is complete
     * when the block successfully mined.
     */
    ListenableFuture<MiningResult> mine(Block block);

    /**
     * Validates the Proof of Work for the block
     */
    boolean validate(BlockHeader blockHeader);

    /**
     * Passes {@link MinerListener}'s to miner
     */
    void setListeners(Collection<MinerListener> listeners);

    final class MiningResult {

        public final long nonce;

        public final byte[] digest;

        /**
         * Mined block
         */
        public final Block block;

        public MiningResult(long nonce, byte[] digest, Block block) {
            this.nonce = nonce;
            this.digest = digest;
            this.block = block;
        }
    }
}
