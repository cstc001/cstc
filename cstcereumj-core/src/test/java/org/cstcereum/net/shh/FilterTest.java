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

import org.cstceum.crypto.ECKey;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterTest {

    String to = WhisperImpl.toIdentity(new ECKey());
    String from = WhisperImpl.toIdentity(new ECKey());
    String[] topics = {"topic1", "topic2", "topic3", "topic4"};

    class FilterStub extends MessageWacstcer {
        public FilterStub() {
        }

        public FilterStub(String to, String from, Topic[] filterTopics) {
            super(to, from, filterTopics);
        }

        @Override
        protected void newMessage(WhisperMessage msg) {

        }
    }

    @Test
    public void test1() {
        MessageWacstcer macstcer = new FilterStub();
        assertTrue(macstcer.macstc(to, from, Topic.createTopics(topics)));
    }

    @Test
    public void test2() {
        MessageWacstcer macstcer = new FilterStub().setTo(to);
        assertTrue(macstcer.macstc(to, from, Topic.createTopics(topics)));
    }

    @Test
    public void test3() {
        MessageWacstcer macstcer = new FilterStub().setTo(to);
        assertFalse(macstcer.macstc(null, from, Topic.createTopics(topics)));
    }

    @Test
    public void test4() {
        MessageWacstcer macstcer = new FilterStub().setFrom(from);
        assertTrue(macstcer.macstc(null, from, Topic.createTopics(topics)));
    }

    @Test
    public void test5() {
        MessageWacstcer macstcer = new FilterStub().setFrom(from);
        assertTrue(!macstcer.macstc(to, null,  Topic.createTopics(topics)));
    }

    @Test
    public void test6() {
        MessageWacstcer macstcer = new FilterStub(null, from,  Topic.createTopics(topics));
        assertTrue(macstcer.macstc(to, from,  Topic.createTopics(topics)));
    }

    @Test
    public void test7() {
        MessageWacstcer macstcer = new FilterStub(null, null,  Topic.createTopics(topics));
        assertTrue(!macstcer.macstc(to, from,  Topic.createTopics(new String[]{})));
    }
}
