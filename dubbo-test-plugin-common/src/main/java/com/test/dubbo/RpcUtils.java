package com.test.dubbo;

/**
 * RpcUtils的ClassLoader是
 * {@linkplain io.oneagent.plugin.classloader.PluginClassLoader}，演示在被增强的Dubbo代码里加载其它类。
 * 
 * @author hengyunabc 2021-08-26
 *
 */
public class RpcUtils {
    /**
     * 入参必须是 PluginClassLoader 能加载到的类，因此直接传入Object
     * @param obj
     */
    public static void print(Object obj) {
        System.out.println("RpcUtils print:" + obj);
    }

}
