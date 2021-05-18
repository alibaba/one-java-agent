package com.alibaba.oneagent.service;

/**
 * @author hengyunabc 2020-12-09
 */
public interface Component {
    /**
     * get component order
     *
     * @return
     */
    int order();

    /**
     * init component before start method
     */
    void init();

    /**
     * start component after init method
     */
    void start();

    /**
     * stop component
     */
    void stop();

    /**
     * get component name
     *
     * @return
     */
    String getName();
}