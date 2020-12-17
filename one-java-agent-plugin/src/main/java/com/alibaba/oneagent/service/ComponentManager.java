package com.alibaba.oneagent.service;

/**
 * 
 * @author hengyunabc 2020-12-17
 *
 */
public interface ComponentManager {

    public <T> T getComponent(Class<T> clazz);

    public void initComponents();

    public void startComponents();

    public void stopComponents();

}
