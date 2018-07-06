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

import org.cstceum.db.ByteArrayWrapper;
import org.cstceum.util.RLP;
import org.cstceum.util.RLPList;

import static org.cstceum.net.shh.ShhMessageCodes.FILTER;
import static org.cstceum.util.ByteUtil.toHexString;

/**
 * @author by Konstantin Shabalin
 */
public class ShhFilterMessage extends ShhMessage {

    private byte[] bloomFilter;

    private ShhFilterMessage() {
    }

    public ShhFilterMessage(byte[] encoded) {
        super(encoded);
        parse();
    }

    static ShhFilterMessage createFromFilter(byte[] bloomFilter) {
        ShhFilterMessage ret = new ShhFilterMessage();
        ret.bloomFilter = bloomFilter;
        ret.parsed = true;
        return ret;
    }

    private void encode() {
        byte[] protocolVersion = RLP.encodeElement(this.bloomFilter);
        this.encoded = RLP.encodeList(protocolVersion);
    }

    private void parse() {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        this.bloomFilter = paramsList.get(0).getRLPData();
        parsed = true;
    }

    @Override
    public byte[] getEncoded() {
        if (encoded == null) encode();
        return encoded;
    }

    public byte[] getBloomFilter() {
        return bloomFilter;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public ShhMessageCodes getCommand() {
        return FILTER;
    }

    @Override
    public String toString() {
        if (!parsed) parse();
        return "[" + this.getCommand().name() +
            " hash=" + toHexString(bloomFilter) + "]";
    }

}
