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
package org.cstceum.facade;

import org.cstceum.config.DefaultConfig;
import org.cstceum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;


/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class cstceumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");

    public static cstceum createcstceum() {
        return createcstceum((Class) null);
    }

    public static cstceum createcstceum(Class userSpringConfig) {
        return userSpringConfig == null ? createcstceum(new Class[] {DefaultConfig.class}) :
                createcstceum(DefaultConfig.class, userSpringConfig);
    }

    /**
     * @deprecated The config parameter is not used anymore. The configuration is passed
     * via 'systemProperties' bean either from the DefaultConfig or from supplied userSpringConfig
     * @param config  Not used
     * @param userSpringConfig   User Spring configuration class
     * @return  Fully initialized cstceum instance
     */
    public static cstceum createcstceum(SystemProperties config, Class userSpringConfig) {

        return userSpringConfig == null ? createcstceum(new Class[] {DefaultConfig.class}) :
                createcstceum(DefaultConfig.class, userSpringConfig);
    }

    public static cstceum createcstceum(Class ... springConfigs) {
        logger.info("Starting cstceumJ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(springConfigs);

        return context.getBean(cstceum.class);
    }
}
