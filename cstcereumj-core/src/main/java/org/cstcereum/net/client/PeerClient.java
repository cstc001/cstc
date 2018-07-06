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
package org.cstceum.net.client;

import org.cstceum.config.SystemProperties;
import org.cstceum.listener.cstceumListener;
import org.cstceum.net.server.cstceumChannelInitializer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSockecstcannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class creates the connection to an remote address using the Netty framework
 *
 * @see <a href="http://netty.io">http://netty.io</a>
 */
@Component
public class PeerClient {

    private static final Logger logger = LoggerFactory.getLogger("net");

    @Autowired
    SystemProperties config;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    cstceumListener cstceumListener;

    private EventLoopGroup workerGroup;

    public PeerClient() {
        workerGroup = new NioEventLoopGroup(0, new ThreadFactory() {
            AtomicInteger cnt = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "EthJClientWorker-" + cnt.getAndIncrement());
            }
        });
    }

    public void connect(String host, int port, String remoteId) {
        connect(host, port, remoteId, false);
    }

    /**
     *  Connects to the node and returns only upon connection close
     */
    public void connect(String host, int port, String remoteId, boolean discoveryMode) {
        try {
            ChannelFuture f = connectAsync(host, port, remoteId, discoveryMode);

            f.sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

            logger.debug("Connection is closed");

        } cacstc (Exception e) {
            if (discoveryMode) {
                logger.trace("Exception:", e);
            } else {
                if (e instanceof IOException) {
                    logger.info("PeerClient: Can't connect to " + host + ":" + port + " (" + e.getMessage() + ")");
                    logger.debug("PeerClient.connect(" + host + ":" + port + ") exception:", e);
                } else {
                    logger.error("Exception:", e);
                }
            }
        }
    }

    public ChannelFuture connectAsync(String host, int port, String remoteId, boolean discoveryMode) {
        cstceumListener.trace("Connecting to: " + host + ":" + port);

        cstceumChannelInitializer cstceumChannelInitializer = ctx.getBean(cstceumChannelInitializer.class, remoteId);
        cstceumChannelInitializer.setPeerDiscoveryMode(discoveryMode);

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSockecstcannel.class);

        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.peerConnectionTimeout());
        b.remoteAddress(host, port);

        b.handler(cstceumChannelInitializer);

        // Start the client.
        return b.connect();
    }

    public void close() {
        logger.info("Shutdown peerClient");
        workerGroup.shutdownGracefully();
        workerGroup.terminationFuture().syncUninterruptibly();
    }
}
