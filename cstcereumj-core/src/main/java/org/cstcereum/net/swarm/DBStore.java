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
package org.cstceum.net.swarm;

import org.cstceum.datasource.DbSource;

/**
 * ChunkStore backed up with KeyValueDataSource
 *
 * Created by Admin on 18.06.2015.
 */
public class DBStore implements ChunkStore {
    private DbSource<byte[]> db;

    public DBStore(DbSource db) {
        this.db = db;
    }

    @Override
    public void put(Chunk chunk) {
        db.put(chunk.getKey().getBytes(), chunk.getData());
    }

    @Override
    public Chunk get(Key key) {
        byte[] bytes = db.get(key.getBytes());
        return bytes == null ? null : new Chunk(key, bytes);
    }
}
