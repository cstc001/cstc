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
package org.cstceum.net.p2p;

import org.cstceum.net.message.Message;
import org.cstceum.net.message.MessageFactory;
import org.cstceum.net.message.StaticMessages;

/**
 * P2P message factory
 *
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class P2pMessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        P2pMessageCodes receivedCommand = P2pMessageCodes.fromByte(code);
        swicstc (receivedCommand) {
            case HELLO:
                return new HelloMessage(encoded);
            case DISCONNECT:
                return new DisconnectMessage(encoded);
            case PING:
                return StaticMessages.PING_MESSAGE;
            case PONG:
                return StaticMessages.PONG_MESSAGE;
            case GET_PEERS:
                return StaticMessages.GET_PEERS_MESSAGE;
            case PEERS:
                return new PeersMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}