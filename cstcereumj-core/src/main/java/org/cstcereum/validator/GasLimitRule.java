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
package org.cstceum.validator;

import org.cstceum.config.SystemProperties;
import org.cstceum.config.Constants;
import org.cstceum.core.BlockHeader;

import java.math.BigInteger;

/**
 * Checks {@link BlockHeader#gasLimit} against {@link Constants#getMIN_GAS_LIMIT}. <br>
 *
 * This check is NOT run in Frontier
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public class GasLimitRule extends BlockHeaderRule {

    private final int MIN_GAS_LIMIT;

    public GasLimitRule(SystemProperties config) {
        MIN_GAS_LIMIT = config.getBlockchainConfig().
                getCommonConstants().getMIN_GAS_LIMIT();
    }

    @Override
    public ValidationResult validate(BlockHeader header) {

        if (new BigInteger(1, header.getGasLimit()).compareTo(BigInteger.valueOf(MIN_GAS_LIMIT)) < 0) {
            return fault("header.getGasLimit() < MIN_GAS_LIMIT");
        }

        return Success;
    }
}
