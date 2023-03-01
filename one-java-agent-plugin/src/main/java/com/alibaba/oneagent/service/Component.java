package com.alibaba.oneagent.service;

import java.util.Properties;

/**
 * @author hengyunabc 2020-12-09
 */
public interface Component {
    static final int DEFAULT_ORDER = 100;

    /**
     * get component order
     *
     * @return
     */
    default int order() {
        return DEFAULT_ORDER;
    };

    /**
     * init component before start method
     */
    default void init(Properties properties) {
    };

    /**
     * start component after init method
     */
    default void start() {
    };

    /**
     * stop component
     */
    default void stop() {
    };

    /**
     * get component name
     *
     * @return
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}