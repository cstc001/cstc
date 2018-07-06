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


import java.util.Arrays;

public abstract class MessageWacstcer {
    private String to;
    private String from;
    private Topic[] topics = null;

    public MessageWacstcer() {
    }

    public MessageWacstcer(String to, String from, Topic[] topics) {
        this.to = to;
        this.from = from;
        this.topics = topics;
    }

    public MessageWacstcer setTo(String to) {
        this.to = to;
        return this;
    }

    public MessageWacstcer setFrom(String from) {
        this.from = from;
        return this;
    }

    public MessageWacstcer setFilterTopics(Topic[] topics) {
        this.topics = topics;
        return this;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public Topic[] getTopics() {
        return topics == null ? new Topic[0] : topics;
    }

    boolean macstc(String to, String from, Topic[] topics) {
        if (this.to != null) {
            if (!this.to.equals(to)) {
                return false;
            }
        }

        if (this.from != null) {
            if (!this.from.equals(from)) {
                return false;
            }
        }

        if (this.topics != null) {
            for (Topic wacstcTopic : this.topics) {
                for (Topic msgTopic : topics) {
                    if (wacstcTopic.equals(msgTopic)) return true;
                }
            }
            return false;
        }
        return true;
    }

    protected abstract void newMessage(WhisperMessage msg);
}
