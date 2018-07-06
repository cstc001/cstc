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

import org.cstceum.config.SystemProperties;
import org.cstceum.core.AccountState;
import org.cstceum.jsontestsuite.suite.ContractDetailsImpl;
import org.cstceum.jsontestsuite.suite.model.AccountTck;
import org.cstceum.vm.DataWord;

import java.util.HashMap;
import java.util.Map;

import static org.cstceum.crypto.HashUtil.sha3;
import static org.cstceum.jsontestsuite.suite.Utils.parseData;
import static org.cstceum.util.Utils.unifiedNumericToBigInteger;

public class AccountBuilder {

    public static StateWrap build(AccountTck account) {

        ContractDetailsImpl details = new ContractDetailsImpl();
        details.setCode(parseData(account.getCode()));
        details.setStorage(convertStorage(account.getStorage()));

        AccountState state = new AccountState(SystemProperties.getDefault())
                .withBalanceIncrement(unifiedNumericToBigInteger(account.getBalance()))
                .withNonce(unifiedNumericToBigInteger(account.getNonce()))
                .withStateRoot(details.getStorageHash())
                .withCodeHash(sha3(details.getCode()));

        return new StateWrap(state, details);
    }


    private static Map<DataWord, DataWord> convertStorage(Map<String, String> storageTck) {

        Map<DataWord, DataWord> storage = new HashMap<>();

        for (String keyTck : storageTck.keySet()) {
            String valueTck = storageTck.get(keyTck);

            DataWord key = new DataWord(parseData(keyTck));
            DataWord value = new DataWord(parseData(valueTck));

            storage.put(key, value);
        }

        return storage;
    }


    public static class StateWrap {

        AccountState accountState;
        ContractDetailsImpl contractDetails;

        public StateWrap(AccountState accountState, ContractDetailsImpl contractDetails) {
            this.accountState = accountState;
            this.contractDetails = contractDetails;
        }

        public AccountState getAccountState() {
            return accountState;
        }

        public ContractDetailsImpl getContractDetails() {
            return contractDetails;
        }
    }
}
