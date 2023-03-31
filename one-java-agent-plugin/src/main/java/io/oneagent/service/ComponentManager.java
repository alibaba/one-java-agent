package io.oneagent.service;

import java.util.Properties;

/**
 * @author hengyunabc 2020-12-17
 */
public interface ComponentManager {

    /**
     * get the specified  class type of component，result may be null
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getComponent(Class<T> clazz);

    /**
     * init all component from spi services
     */
    void initComponents(Properties properties);

    /**
     * start all component
     */
    void startComponents();

    /**
     * stop component
     */
    void stopComponents();

}
