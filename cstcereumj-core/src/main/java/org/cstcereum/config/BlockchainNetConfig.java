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
package org.cstceum.config;

import org.cstceum.core.BlockHeader;

/**
 * Describes a set of configs for a specific blockchain depending on the block number
 * E.g. the main cstceum net has at least FrontierConfig and HomesteadConfig depending on the block
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public interface BlockchainNetConfig {

    /**
     * Get the config for the specific block
     */
    BlockchainConfig getConfigForBlock(long blockNumber);

    /**
     * Returns the constants common for all the blocks in this blockchain
     */
    Constants getCommonConstants();
}
