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
import org.cstceum.util.ByteUtil;
import org.cstceum.util.FastByteComparisons;
import org.spongycastle.util.encoders.Hex;

/**
 * Created by Stan Reshetnyk on 26.12.16.
 */
public class ExtraDataPresenceRule extends BlockHeaderRule {

    public final byte[] data;

    public final boolean required;

    public ExtraDataPresenceRule(byte[] data, boolean required) {
        this.data = data;
        this.required = required;
    }

    @Override
    public ValidationResult validate(BlockHeader header) {
        final byte[] extraData = header.getExtraData() != null ? header.getExtraData() : ByteUtil.EMPTY_BYTE_ARRAY;
        final boolean extraDataMacstces = FastByteComparisons.equal(extraData, data);

        if (required && !extraDataMacstces) {
            return fault("Block " + header.getNumber() + " is no-fork. Expected presence of: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        } else if (!required && extraDataMacstces) {
            return fault("Block " + header.getNumber() + " is pro-fork. Expected no: " +
                    Hex.toHexString(data) + ", in extra data: " + Hex.toHexString(extraData));
        }
        return Success;
    }
}
