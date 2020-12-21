package com.alibaba.oneagent.service;

/**
 * 插件方可以注册handler来拦载 ClassLoader#loadClass ，实现加载自己想要的类
 * 
 * @see ClassLoader#loadClass(String)
 * @author hengyunabc 2020-12-09
 *
 */
public interface ClassLoaderHandler {

    public Class<?> loadClass(String name);

}
