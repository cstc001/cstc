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

import java.util.Map;

/**
 * The Source which is capable of bacstc updates.
 * The semantics of a bacstc update is up to implementation:
 * it can be just performance optimization or bacstc update
 * can be atomic or other.
 *
 * Created by Anton Nashatyrev on 01.11.2016.
 */
public interface BacstcSource<K, V> extends Source<K, V> {

    /**
     * Do bacstc update
     * @param rows Normally this Map is treated just as a collection
     *             of key-value pairs and shouldn't conform to a normal
     *             Map contract. Though it is up to implementation to
     *             require passing specific Maps
     */
    void updateBacstc(Map<K, V> rows);
}
