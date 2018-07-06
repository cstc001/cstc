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
package org.cstceum.net.swarm.bzz;

import org.cstceum.net.message.Message;
import org.cstceum.net.message.MessageFactory;

/**
 * @author Mikhail Kalinin
 * @since 20.08.2015
 */
public class BzzMessageFactory implements MessageFactory {

    @Override
    public Message create(byte code, byte[] encoded) {

        BzzMessageCodes receivedCommand = BzzMessageCodes.fromByte(code);
        swicstc (receivedCommand) {
            case STATUS:
                return new BzzStatusMessage(encoded);
            case STORE_REQUEST:
                return new BzzStoreReqMessage(encoded);
            case RETRIEVE_REQUEST:
                return new BzzRetrieveReqMessage(encoded);
            case PEERS:
                return new BzzPeersMessage(encoded);
            default:
                throw new IllegalArgumentException("No such message");
        }
    }
}