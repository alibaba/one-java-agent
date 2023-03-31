package io.oneagent.service;

/**
 * 插件方可以注册handler来拦载 ClassLoader#loadClass ，实现加载自己想要的类
 *
 * @author hengyunabc 2020-12-09
 * @see ClassLoader#loadClass(String)
 */
public interface ClassLoaderHandler {

    /**
     * plugin parties can register handlers to intercept ClassLoader#loadClass to load the class they want
     *
     * @param name
     * @return
     */
    Class<?> loadClass(String name);
}
