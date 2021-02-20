package com.alibaba.oneagent.plugin.config;

import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * 
 * @author hengyunabc 2021-02-19
 *
 */
public class PluginConfigImplTest {

    @Test
    public void test() {

        Properties globalProperties = new Properties();
        Properties pluginProperties = new Properties();
        pluginProperties.put("name", "test");
        pluginProperties.put("enabled", "false");

        PluginConfigImpl impl = new PluginConfigImpl(globalProperties, pluginProperties);

        boolean enabled = impl.isEnabled();

        Assertions.assertThat(enabled).isFalse();
    }

    @Test
    public void testGlobalConfig() {

        Properties globalProperties = new Properties();
        globalProperties.put(PluginConfigImpl.PLUGIN_CONFIG_PREFIX + "test" + ".enabled", "false");
        Properties pluginProperties = new Properties();
        pluginProperties.put("name", "test");

        PluginConfigImpl impl = new PluginConfigImpl(globalProperties, pluginProperties);

        boolean enabled = impl.isEnabled();

        Assertions.assertThat(enabled).isFalse();
    }
    
    @Test
    public void testDisabledConfig() {

        Properties globalProperties = new Properties();
        globalProperties.put(PluginConfigImpl.PLUGIN_DISABLED, "abc, test ,  xxx");
        Properties pluginProperties = new Properties();
        pluginProperties.put("name", "test");

        PluginConfigImpl impl = new PluginConfigImpl(globalProperties, pluginProperties);

        boolean enabled = impl.isEnabled();

        Assertions.assertThat(enabled).isFalse();
    }

}
