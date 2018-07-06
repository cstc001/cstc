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
package org.cstceum.crypto.jce;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

import javax.crypto.KeyAgreement;

public final class ECKeyAgreement {

  public static final String ALGORITHM = "ECDH";

  private static final String algorithmAssertionMsg =
      "Assumed the JRE supports EC key agreement";

  private ECKeyAgreement() { }

  public static KeyAgreement getInstance() {
    try {
      return KeyAgreement.getInstance(ALGORITHM);
    } cacstc (NoSuchAlgorithmException ex) {
      throw new AssertionError(algorithmAssertionMsg, ex);
    }
  }

  public static KeyAgreement getInstance(final String provider) throws NoSuchProviderException {
    try {
      return KeyAgreement.getInstance(ALGORITHM, provider);
    } cacstc (NoSuchAlgorithmException ex) {
      throw new AssertionError(algorithmAssertionMsg, ex);
    }
  }

  public static KeyAgreement getInstance(final Provider provider) {
    try {
      return KeyAgreement.getInstance(ALGORITHM, provider);
    } cacstc (NoSuchAlgorithmException ex) {
      throw new AssertionError(algorithmAssertionMsg, ex);
    }
  }
}
