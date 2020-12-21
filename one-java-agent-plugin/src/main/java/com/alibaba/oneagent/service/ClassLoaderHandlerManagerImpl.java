package com.alibaba.oneagent.service;

/**
 * 
 * @author hengyunabc 2020-12-09
 *
 */
public class ClassLoaderHandlerManagerImpl implements ClassLoaderHandlerManager, Component {
    private volatile ClassLoaderHandler[] handlers = new ClassLoaderHandler[0];

    @Override
    synchronized public void addHandler(ClassLoaderHandler handler) {
        ClassLoaderHandler[] tmp = new ClassLoaderHandler[handlers.length + 1];
        for (int i = 0; i < handlers.length; ++i) {
            tmp[i] = handlers[i];
        }
        tmp[handlers.length] = handler;

        handlers = tmp;
    }

    @Override
    synchronized public void removeHandler(ClassLoaderHandler handler) {
        int index = -1;
        for (int i = 0; i < handlers.length; ++i) {
            if (handler.equals(handlers[i])) {
                index = i;
            }
        }
        if (index >= 0) {
            ClassLoaderHandler[] tmp = new ClassLoaderHandler[handlers.length - 1];
            int i = 0;
            int j = 0;
            for (; i < handlers.length; ++i) {
                if (!handler.equals(handlers[i])) {
                    tmp[j] = handlers[i];
                    ++j;
                }
            }
            handlers = tmp;
        }
    }

    @Override
    public ClassLoaderHandler[] handlers() {
        return handlers;
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        this.handlers = new ClassLoaderHandler[0];
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Class<?> loadClass(String name) {
        ClassLoaderHandler[] localHandlers = handlers;
        try {
            for (ClassLoaderHandler handler : localHandlers) {

                Class<?> loadClass = handler.loadClass(name);
                if (loadClass != null) {
                    return loadClass;
                }
            }
        } catch (Throwable e) {
            // TODO: logger? 会有死锁风险不？
        }

        return null;
    }

}
