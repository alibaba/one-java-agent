package com.hello;

import org.apache.dubbo.rpc.Invocation;

/**
 * 
 * <pre>
 * 在instrument.properties 里通过define字段指定的类，会在运行时define到应用的ClassLoader里。
 * 可以在增强的代码里直接使用
 * </pre>
 */
public class DubboUtils {

    public static void test(Invocation invocation) {
        System.err.println("DubboUtils: " + invocation.getServiceName());
    }

}
