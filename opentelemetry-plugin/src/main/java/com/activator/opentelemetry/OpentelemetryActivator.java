package com.activator.opentelemetry;

import io.oneagent.plugin.PluginActivator;
import io.oneagent.plugin.PluginContext;
import io.oneagent.service.ClassLoaderHandlerManager;

public class OpentelemetryActivator implements PluginActivator {
    private String name = this.getClass().getSimpleName();

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());

        ClassLoaderHandlerManager loaderHandlerManager = context.getComponentManager()
                .getComponent(ClassLoaderHandlerManager.class);
        loaderHandlerManager.addHandler(new OpentelemetryPluginClassLoaderHandler());

        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        System.out.println("init " + this.getClass().getName());
    }

    @Override
    public void start(PluginContext context) throws Exception {
        System.out.println("start " + this.getClass().getName());
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        System.out.println("stop " + this.getClass().getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
