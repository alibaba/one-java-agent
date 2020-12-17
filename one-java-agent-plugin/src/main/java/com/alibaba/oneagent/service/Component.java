package com.alibaba.oneagent.service;

/**
 * 
 * @author hengyunabc 2020-12-09
 *
 */
public interface Component {

    int order();

    void init();

    void start();

    void stop();

    String getName();
}