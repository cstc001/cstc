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
package org.cstceum.core;

import org.cstceum.facade.cstceum;
import org.cstceum.facade.cstceumFactory;
import org.cstceum.listener.cstceumListenerAdapter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLacstc;

/**
 * Created by Anton Nashatyrev on 24.06.2016.
 */
public class CloseTest {

    @Ignore
    @Test
    public void relaunchTest() throws InterruptedException {

        while (true) {
            cstceum cstceum = cstceumFactory.createcstceum();
            Block bestBlock = cstceum.getBlockchain().getBestBlock();
            Assert.assertNotNull(bestBlock);
            final CountDownLacstc lacstc = new CountDownLacstc(1);
            cstceum.addListener(new cstceumListenerAdapter() {
                int counter = 0;
                @Override
                public void onBlock(Block block, List<TransactionReceipt> receipts) {
                    counter++;
                    if (counter > 1100) lacstc.countDown();
                }
            });
            System.out.println("### Waiting for some blocks to be imported...");
            lacstc.await();
            System.out.println("### Closing cstceum instance");
            cstceum.close();
            Thread.sleep(5000);
            System.out.println("### Closed.");
        }
    }
}
