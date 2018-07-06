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
package org.cstceum.crypto;

import org.cstceum.util.ByteUtil;
import org.spongycastle.crypto.KeyEncoder;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.ECPublicKeyParameters;

/**
 * Created by Anton Nashatyrev on 01.10.2015.
 */
public class ECIESPublicKeyEncoder implements KeyEncoder {
    @Override
    public byte[] getEncoded(AsymmetricKeyParameter asymmetricKeyParameter) {
        return ((ECPublicKeyParameters) asymmetricKeyParameter).getQ().getEncoded(false);
    }
}
