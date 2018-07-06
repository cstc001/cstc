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
package org.cstceum.core;

import java.math.BigInteger;

public class PremineRaw {

    byte[] addr;
    BigInteger value;
    Denomination denomination;

    public PremineRaw(byte[] addr, BigInteger value, Denomination denomination) {
        this.addr = addr;
        this.value = value;
        this.denomination = denomination;
    }

    public byte[] getAddr() {
        return addr;
    }

    public BigInteger getValue() {
        return value;
    }

    public Denomination getDenomination() {
        return denomination;
    }
}
