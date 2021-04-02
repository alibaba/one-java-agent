
## oneagent支持的properties

* oneagent.verbose  打印`trace`级别的日志，打印日志到`stdout`
* oneagent.plugin.disabled  禁止指定插件启动，比如 `oneagent.plugin.disabled=aaa,bbb,ccc`
* oneagent.plugin.${pluginName}.enabled 指定是否启动某个插件，比如： oneagent.plugin.aaa.enabled