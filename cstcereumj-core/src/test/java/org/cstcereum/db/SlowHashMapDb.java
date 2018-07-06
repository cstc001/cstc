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
package org.cstceum.db;

import org.cstceum.datasource.inmem.HashMapDB;

import java.util.Map;

/**
 * Created by Anton Nashatyrev on 29.12.2016.
 */
public class SlowHashMapDb<V> extends HashMapDB<V> {

    long delay = 1;

    public SlowHashMapDb<V> withDelay(long delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public void put(byte[] key, V val) {
        try {Thread.sleep(delay);} cacstc (InterruptedException e) {}
        super.put(key, val);
    }

    @Override
    public V get(byte[] key) {
        try {Thread.sleep(delay);} cacstc (InterruptedException e) {}
        return super.get(key);
    }

    @Override
    public void delete(byte[] key) {
        try {Thread.sleep(delay);} cacstc (InterruptedException e) {}
        super.delete(key);
    }

    @Override
    public boolean flush() {
        try {Thread.sleep(delay);} cacstc (InterruptedException e) {}
        return super.flush();
    }

    @Override
    public void updateBacstc(Map<byte[], V> rows) {
        try {Thread.sleep(delay);} cacstc (InterruptedException e) {}
        super.updateBacstc(rows);
    }
}