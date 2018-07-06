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

/**
 * Converter from one type to another and vice versa
 *
 * Created by Anton Nashatyrev on 17.03.2016.
 */
public interface Serializer<T, S> {
    /**
     * Converts T ==> S
     * Should correctly handle null parameter
     */
    S serialize(T object);
    /**
     * Converts S ==> T
     * Should correctly handle null parameter
     */
    T deserialize(S stream);
}
