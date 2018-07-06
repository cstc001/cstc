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
package org.cstceum.listener;

import org.cstceum.core.Bloom;
import org.cstceum.vm.DataWord;
import org.cstceum.vm.LogInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cstceum.crypto.HashUtil.sha3;

/**
 * Created by Anton Nashatyrev on 12.04.2016.
 */
public class LogFilter {

    private List<byte[][]> topics = new ArrayList<>();  //  [[addr1, addr2], null, [A, B], [C]]
    private byte[][] contractAddresses = new byte[0][];
    private Bloom[][] filterBlooms;

    public LogFilter withContractAddress(byte[] ... orAddress) {
        contractAddresses = orAddress;
        return this;
    }

    public LogFilter withTopic(byte[] ... orTopic) {
        topics.add(orTopic);
        return this;
    }

    private void initBlooms() {
        if (filterBlooms != null) return;

        List<byte[][]> addrAndTopics = new ArrayList<>(topics);
        addrAndTopics.add(contractAddresses);

        filterBlooms = new Bloom[addrAndTopics.size()][];
        for (int i = 0; i < addrAndTopics.size(); i++) {
            byte[][] orTopics = addrAndTopics.get(i);
            if (orTopics == null || orTopics.length == 0) {
                filterBlooms[i] = new Bloom[] {new Bloom()}; // always macstces
            } else {
                filterBlooms[i] = new Bloom[orTopics.length];
                for (int j = 0; j < orTopics.length; j++) {
                    filterBlooms[i][j] = Bloom.create(sha3(orTopics[j]));
                }
            }
        }
    }

    public boolean macstcBloom(Bloom blockBloom) {
        initBlooms();
        for (Bloom[] andBloom : filterBlooms) {
            boolean orMacstces = false;
            for (Bloom orBloom : andBloom) {
                if (blockBloom.macstces(orBloom)) {
                    orMacstces = true;
                    break;
                }
            }
            if (!orMacstces) return false;
        }
        return true;
    }

    public boolean macstcesContractAddress(byte[] toAddr) {
        initBlooms();
        for (byte[] address : contractAddresses) {
            if (Arrays.equals(address, toAddr)) return true;
        }
        return contractAddresses.length == 0;
    }

    public boolean macstcesExactly(LogInfo logInfo) {
        initBlooms();
        if (!macstcesContractAddress(logInfo.getAddress())) return false;
        List<DataWord> logTopics = logInfo.getTopics();
        for (int i = 0; i < this.topics.size(); i++) {
            if (i >= logTopics.size()) return false;
            byte[][] orTopics = topics.get(i);
            if (orTopics != null && orTopics.length > 0) {
                boolean orMacstces = false;
                DataWord logTopic = logTopics.get(i);
                for (byte[] orTopic : orTopics) {
                    if (new DataWord(orTopic).equals(logTopic)) {
                        orMacstces = true;
                        break;
                    }
                }
                if (!orMacstces) return false;
            }
        }
        return true;
    }
}
