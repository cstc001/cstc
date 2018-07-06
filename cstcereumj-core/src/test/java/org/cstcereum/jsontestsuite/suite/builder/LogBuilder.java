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
package org.cstceum.jsontestsuite.suite.builder;

import org.cstceum.jsontestsuite.suite.model.LogTck;
import org.cstceum.vm.DataWord;
import org.cstceum.vm.LogInfo;

import java.util.ArrayList;
import java.util.List;

import static org.cstceum.jsontestsuite.suite.Utils.parseData;

public class LogBuilder {

    public static LogInfo build(LogTck logTck){

        byte[] address = parseData(logTck.getAddress());
        byte[] data = parseData(logTck.getData());

        List<DataWord> topics = new ArrayList<>();
        for (String topicTck : logTck.getTopics())
            topics.add(new DataWord(parseData(topicTck)));

        return new LogInfo(address, topics, data);
    }

    public static List<LogInfo> build(List<LogTck> logs){

        List<LogInfo> outLogs = new ArrayList<>();

        for (LogTck log : logs)
            outLogs.add(build(log));

        return outLogs;
    }
}
