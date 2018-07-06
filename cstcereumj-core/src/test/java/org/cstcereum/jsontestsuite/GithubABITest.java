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
package org.cstceum.jsontestsuite;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

import static org.cstceum.jsontestsuite.GitHubJSONTestSuite.runABITest;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2017
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GithubABITest {

    String commitSHA = "2d28e1f654bb4b6fe6aca2808b4b5c37e3fccdcc";

    @Test
    public void basicAbiTests() throws IOException {
        runABITest("ABITests/basic_abi_tests.json", commitSHA);
    }
}