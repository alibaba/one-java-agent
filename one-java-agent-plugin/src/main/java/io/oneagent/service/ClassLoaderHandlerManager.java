package io.oneagent.service;

/**
 * @author hengyunabc 2020-12-09
 */
public interface ClassLoaderHandlerManager {
    /**
     * add ClassLoaderHandler
     *
     * @param handler
     */
    void addHandler(ClassLoaderHandler handler);

    /**
     * remove ClassLoaderHandler
     *
     * @param handler
     */
    void removeHandler(ClassLoaderHandler handler);

    /**
     * get all ClassLoaderHandler
     *
     * @return
     */
    ClassLoaderHandler[] handlers();

    /**
     * load class name in order of ClassLoaderHandler
     *
     * @param name
     * @return
     */
    Class<?> loadClass(String name);
}
