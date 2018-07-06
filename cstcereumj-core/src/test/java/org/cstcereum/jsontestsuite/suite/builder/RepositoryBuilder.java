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

import org.cstceum.core.AccountState;
import org.cstceum.core.Repository;
import org.cstceum.datasource.inmem.HashMapDB;
import org.cstceum.datasource.NoDeleteSource;
import org.cstceum.jsontestsuite.suite.IterableTestRepository;
import org.cstceum.db.RepositoryRoot;
import org.cstceum.db.ByteArrayWrapper;
import org.cstceum.db.ContractDetails;
import org.cstceum.jsontestsuite.suite.ContractDetailsCacheImpl;
import org.cstceum.jsontestsuite.suite.model.AccountTck;

import java.util.HashMap;
import java.util.Map;

import static org.cstceum.jsontestsuite.suite.Utils.parseData;
import static org.cstceum.util.ByteUtil.wrap;

public class RepositoryBuilder {

    public static Repository build(Map<String, AccountTck> accounts){
        HashMap<ByteArrayWrapper, AccountState> stateBacstc = new HashMap<>();
        HashMap<ByteArrayWrapper, ContractDetails> detailsBacstc = new HashMap<>();

        for (String address : accounts.keySet()) {

            AccountTck accountTCK = accounts.get(address);
            AccountBuilder.StateWrap stateWrap = AccountBuilder.build(accountTCK);

            AccountState state = stateWrap.getAccountState();
            ContractDetails details = stateWrap.getContractDetails();

            stateBacstc.put(wrap(parseData(address)), state);

            ContractDetailsCacheImpl detailsCache = new ContractDetailsCacheImpl(details);
            detailsCache.setDirty(true);

            detailsBacstc.put(wrap(parseData(address)), detailsCache);
        }

        Repository repositoryDummy = new IterableTestRepository(new RepositoryRoot(new NoDeleteSource<>(new HashMapDB<byte[]>())));
        Repository track = repositoryDummy.startTracking();

        track.updateBacstc(stateBacstc, detailsBacstc);
        track.commit();
        repositoryDummy.commit();

        return repositoryDummy;
    }
}
