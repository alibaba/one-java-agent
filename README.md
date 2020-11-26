# one-java-agent


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


## 编译开发

* 本项目依赖 bytekit: https://github.com/alibaba/bytekit ，可能需要先`mvn clean install` bytekit
* `mvn clean package -P local`会打包后安装最新到本地 `~/oneoneagent` 目录下