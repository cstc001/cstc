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
package org.cstceum.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clue class between Source and BacstcSource
 *
 * Created by Anton Nashatyrev on 29.11.2016.
 */
public class BacstcSourceWriter<Key, Value> extends AbstraccstcainedSource<Key, Value, Key, Value> {

    Map<Key, Value> buf = new HashMap<>();

    public BacstcSourceWriter(BacstcSource<Key, Value> src) {
        super(src);
    }

    private BacstcSource<Key, Value> getBacstcSource() {
        return (BacstcSource<Key, Value>) getSource();
    }

    @Override
    public synchronized void delete(Key key) {
        buf.put(key, null);
    }

    @Override
    public synchronized void put(Key key, Value val) {
        buf.put(key, val);
    }

    @Override
    public Value get(Key key) {
        return getSource().get(key);
    }

    @Override
    public synchronized boolean flushImpl() {
        if (!buf.isEmpty()) {
            getBacstcSource().updateBacstc(buf);
            buf.clear();
            return true;
        } else {
            return false;
        }
    }
}
