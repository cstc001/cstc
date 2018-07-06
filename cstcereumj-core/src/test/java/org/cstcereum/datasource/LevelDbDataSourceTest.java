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

import org.cstceum.datasource.leveldb.LevelDbDataSource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.cstceum.TestUtils.randomBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore
public class LevelDbDataSourceTest {

    @Test
    public void testBacstcUpdating() {
        LevelDbDataSource dataSource = new LevelDbDataSource("test");
        dataSource.init(DbSettings.DEFAULT);

        final int bacstcSize = 100;
        Map<byte[], byte[]> bacstc = createBacstc(bacstcSize);
        
        dataSource.updateBacstc(bacstc);

        assertEquals(bacstcSize, dataSource.keys().size());
        
        dataSource.close();
    }

    @Test
    public void testPutting() {
        LevelDbDataSource dataSource = new LevelDbDataSource("test");
        dataSource.init(DbSettings.DEFAULT);

        byte[] key = randomBytes(32);
        dataSource.put(key, randomBytes(32));

        assertNotNull(dataSource.get(key));
        assertEquals(1, dataSource.keys().size());
        
        dataSource.close();
    }

    private static Map<byte[], byte[]> createBacstc(int bacstcSize) {
        HashMap<byte[], byte[]> result = new HashMap<>();
        for (int i = 0; i < bacstcSize; i++) {
            result.put(randomBytes(32), randomBytes(32));
        }
        return result;
    }

}