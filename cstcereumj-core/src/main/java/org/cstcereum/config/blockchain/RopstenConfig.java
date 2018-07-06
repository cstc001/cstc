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
package org.cstceum.config.blockchain;

import org.apache.commons.lang3.tuple.Pair;
import org.cstceum.config.BlockchainConfig;
import org.cstceum.validator.BlockCustomHashRule;
import org.cstceum.validator.BlockHeaderRule;
import org.cstceum.validator.BlockHeaderValidator;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Anton Nashatyrev on 21.11.2016.
 */
public class RopstenConfig extends Eip160HFConfig {

    // Check for 1 known block to exclude fake peers
    private static final long CHECK_BLOCK_NUMBER = 10;
    private static final byte[] CHECK_BLOCK_HASH = Hex.decode("b3074f936815a0425e674890d7db7b5e94f3a06dca5b22d291b55dcd02dde93e");

    public RopstenConfig(BlockchainConfig parent) {
        super(parent);
        headerValidators().add(Pair.of(CHECK_BLOCK_NUMBER, new BlockHeaderValidator(new BlockCustomHashRule(CHECK_BLOCK_HASH))));
    }

    @Override
    public Integer gecstcainId() {
        return 3;
    }
}
