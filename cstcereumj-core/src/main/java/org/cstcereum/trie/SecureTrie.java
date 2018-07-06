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
package org.cstceum.trie;

import org.cstceum.datasource.Source;
import org.cstceum.util.Value;

import static org.cstceum.crypto.HashUtil.sha3;
import static org.cstceum.util.ByteUtil.EMPTY_BYTE_ARRAY;

public class SecureTrie extends TrieImpl {

    public SecureTrie(byte[] root) {
        super(root);
    }

    public SecureTrie(Source<byte[], byte[]> cache) {
        super(cache, null);
    }

    public SecureTrie(Source<byte[], byte[]> cache, byte[] root) {
        super(cache, root);
    }

    @Override
    public byte[] get(byte[] key) {
        return super.get(sha3(key));
    }

    @Override
    public void put(byte[] key, byte[] value) {
        super.put(sha3(key), value);
    }

    @Override
    public void delete(byte[] key) {
        put(key, EMPTY_BYTE_ARRAY);
    }
}
