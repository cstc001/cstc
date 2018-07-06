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
package org.cstceum.net;

import org.cstceum.net.message.ReasonCode;
import org.cstceum.net.p2p.DisconnectMessage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

public class DisconnectMessageTest {

    private static final Logger logger = LoggerFactory.getLogger("test");

    /* DISCONNECT_MESSAGE */

    @Test /* DisconnectMessage 1 - Requested */
    public void test_1() {

        byte[] payload = Hex.decode("C100");
        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);

        logger.trace("{}" + disconnectMessage);
        assertEquals(disconnectMessage.getReason(), ReasonCode.REQUESTED);
    }

    @Test /* DisconnectMessage 2 - TCP Error */
    public void test_2() {

        byte[] payload = Hex.decode("C101");
        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);

        logger.trace("{}" + disconnectMessage);
        assertEquals(disconnectMessage.getReason(), ReasonCode.TCP_ERROR);
    }

    @Test /* DisconnectMessage 2 - from constructor */
    public void test_3() {

        DisconnectMessage disconnectMessage = new DisconnectMessage(ReasonCode.NULL_IDENTITY);

        logger.trace("{}" + disconnectMessage);

        String expected = "c107";
        assertEquals(expected, Hex.toHexString(disconnectMessage.getEncoded()));

        assertEquals(ReasonCode.NULL_IDENTITY, disconnectMessage.getReason());
    }

    @Test //handling boundary-high
    public void test_4() {

        byte[] payload = Hex.decode("C180");

        DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
        logger.trace("{}" + disconnectMessage);

        assertEquals(disconnectMessage.getReason(), ReasonCode.UNKNOWN); //high numbers are zeroed
    }

    @Test //handling boundary-low minus 1 (error)
    public void test_6() {

        String disconnectMessageRaw = "C19999";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        try {
            DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
            disconnectMessage.toString(); //throws exception
            assertTrue("Valid raw encoding for disconnectMessage", false);
        } cacstc (RuntimeException e) {
            assertTrue("Invalid raw encoding for disconnectMessage", true);
        }
    }

    @Test //handling boundary-high plus 1 (error)
    public void test_7() {

        String disconnectMessageRaw = "C28081";
        byte[] payload = Hex.decode(disconnectMessageRaw);

        try {
            DisconnectMessage disconnectMessage = new DisconnectMessage(payload);
            disconnectMessage.toString(); //throws exception
            assertTrue("Valid raw encoding for disconnectMessage", false);
        } cacstc (RuntimeException e) {
            assertTrue("Invalid raw encoding for disconnectMessage", true);
        }
    }
}

