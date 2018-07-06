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
package org.cstceum.solidity;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import org.cstceum.solidity.Abi.Entry;
import org.cstceum.solidity.Abi.Entry.Type;
import org.junit.Test;

import java.io.IOException;

public class AbiTest {

    @Test
    public void simpleTest() throws IOException {
        String contractAbi = "[{"
                + "\"name\":\"simpleFunction\","
                + "\"constant\":true,"
                + "\"payable\":true,"
                + "\"type\":\"function\","
                + "\"inputs\": [{\"name\":\"_in\", \"type\":\"bytes32\"}],"
                + "\"outputs\":[{\"name\":\"_out\",\"type\":\"bytes32\"}]}]";

        Abi abi = Abi.fromJson(contractAbi);
        assertEquals(abi.size(), 1);

        Entry onlyFunc = abi.get(0);
        assertEquals(onlyFunc.type, Type.function);
        assertEquals(onlyFunc.inputs.size(), 1);
        assertEquals(onlyFunc.outputs.size(), 1);
        assertTrue(onlyFunc.payable);
        assertTrue(onlyFunc.constant);
    }

    @Test
    public void simpleLegacyTest() throws IOException {
        String contractAbi = "[{"
                + "\"name\":\"simpleFunction\","
                + "\"constant\":true,"
                + "\"type\":\"function\","
                + "\"inputs\": [{\"name\":\"_in\", \"type\":\"bytes32\"}],"
                + "\"outputs\":[{\"name\":\"_out\",\"type\":\"bytes32\"}]}]";

        Abi abi = Abi.fromJson(contractAbi);
        assertEquals(abi.size(), 1);

        Entry onlyFunc = abi.get(0);
        assertEquals(onlyFunc.type, Type.function);
        assertEquals(onlyFunc.inputs.size(), 1);
        assertEquals(onlyFunc.outputs.size(), 1);
        assertTrue(onlyFunc.constant);
    }
}
