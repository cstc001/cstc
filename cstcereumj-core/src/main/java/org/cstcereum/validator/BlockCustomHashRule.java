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
package org.cstceum.validator;

import org.cstceum.core.BlockHeader;
import org.cstceum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Stan Reshetnyk on 26.12.16.
 */
public class BlockCustomHashRule extends BlockHeaderRule {

    public final byte[] blockHash;

    public BlockCustomHashRule(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        if (!FastByteComparisons.equal(header.getHash(), blockHash)) {
            return fault("Block " + header.getNumber() + " hash constraint violated. Expected:" +
                    Hex.toHexString(blockHash) + ", got: " + Hex.toHexString(header.getHash()));
        }
        return Success;
    }
}
