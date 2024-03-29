# one-java-agent
![JavaCI](https://github.com/alibaba/one-java-agent/workflows/JavaCI/badge.svg)
[![maven](https://img.shields.io/maven-central/v/com.alibaba/one-java-agent.svg)](https://search.maven.org/search?q=g:com.alibaba%20AND%20a:one-java-agent*)
![license](https://img.shields.io/github/license/alibaba/one-java-agent.svg)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/alibaba/one-java-agent.svg)](http://isitmaintained.com/project/alibaba/one-java-agent "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/alibaba/one-java-agent.svg)](http://isitmaintained.com/project/alibaba/one-java-agent "Percentage of issues still open")

## 目标

1. 提供插件化支持，统一管理众多的Java Agent
2. 插件支持install/unstall，需要插件方实现接口
3. 支持传统的java agent，即已经开发好的java agent


## 插件系统

插件如果希望感知生命周期，可以实现 `PluginActivator`接口：

```java
public interface PluginActivator {
    // 让插件本身判断是否要启动
    boolean enabled(PluginContext context);

    public void init(PluginContext context) throws Exception;

    /**
     * Before calling this method, the {@link PluginState} is
     * {@link PluginState#STARTING}, after calling, the {@link PluginState} is
     * {@link PluginState#ACTIVE}
     *
     * @param context
     */
    public void start(PluginContext context) throws Exception;

    /**
     * Before calling this method, the {@link PluginState} is
     * {@link PluginState#STOPPING}, after calling, the {@link PluginState} is
     * {@link PluginState#RESOLVED}
     *
     * @param context
     */
    public void stop(PluginContext context) throws Exception;
}
```



## 传统的java agent

插件目录下面放一个`plugin.properties`，并且放上原生的agent jar文件。

例如：

```
type=traditional
name=demo-agent
version=1.0.0
agentJarPath=demo-agent.jar
```

则 one java agent会启动这个`demo-agent`。

## 配置注入

One Java Agent很方便可以注入配置到插件里。

1. 配置到插件的`plugin.properties`文件里
2. 通过`-D`参数配置，比如插件`aaa`，则可以配置为`-Doneagent.plugin.aaa.key1=value1`

然后可以通过`PluginContext#getProperty("key1")`来获取值。

## 迁移

* 从一个传统的Java Agent如何迁移到one java agent的形式？
* 如何同时支持传统的 `-javaagent` 方式和 one java agent形式？

比如一个传统的Agent，它会有一个包含 premain 函数的类：

```java
public class MyAgent {

    public static void premain(String args, Instrumentation inst) {
        // do something
    }
}
```

把上面的Agent做同时支持非常简单，先把原来的初始化逻辑抽取为`init`函数，把原来的初始化逻辑移到里面：

```java
public class MyAgent {

    public static void premain(String args, Instrumentation inst) {
        init(args, inst);
    }

    public static void init(String args, Instrumentation inst) {
        // do something
    }
}
```

然后按上面的文档，编写一个`MyActivator`，在`init`函数里调用原来的`MyAgent.init(args, instrumentation);`函数

```java
public class MyActivator implements PluginActivator {
...
    @Override
    public void init(PluginContext context) throws Exception {
        Instrumentation instrumentation = context.getInstrumentation();
        String args = context.getProperty("args");
        MyAgent.init(args, instrumentation);
    }
...
}
```

在MyActivator init函数里，args可以通过`配置注入`一节注入，或者通过自定义的方式来获取。

这样子，Agent就可以同时支持传统方式和One Java Agent方式启动。


## 插件之间类共享

参考`fastjson-demo-plugin`，它在`plugin.properties`里配置了`exportPackages=com.alibaba.fastjson`。

当其它插件想引用共享的fastjson时，需要在`plugin.properties`里配置：

```
importPackages=com.alibaba.fastjson
```

## 插件注册自定义 ClassLoaderHandler

当插件增强应用ClassLoader里加载的类时，会出现一个问题，当调用插件自己的类时，会加载不到。因此提供一个`ClassLoaderHandler`机制，插件方可以自行注册处理自己package下的类加载。

参考`dubbo-test-plugin`里：

* /dubbo-test-plugin/src/main/java/com/test/dubbo/DubboPluginClassLoaderHandler.java
* /dubbo-test-instrument/src/main/java/org/apache/dubbo/monitor/support/MonitorFilter.java

在`MonitorFilter`里调用了在 plugin里加载的`com.test.dubbo.RpcUtils`。


## 配置define 工具类

在增强代码之后，如果把逻辑全部写到`@Instrument`里：

* 增强代码会太复杂
* 有重复的逻辑需要重用
* `@Instrument`里插入的代码缺少行号

那么可以定义一些工具类，在运行时动态 define 到应用的 ClassLoader 里。

参考： `dubbo-test-instrument/src/main/resources/instrument.properties`里`define`配置。


## 编译开发

* 本项目依赖 bytekit: https://github.com/alibaba/bytekit ，可能需要先`mvn clean install` bytekit
* 执行测试： `mvn clean package -DskipTests && mvn test`
* `mvn clean package -P local -DskipTests`会打包后安装最新到本地 `~/oneoneagent` 目录下