package io.oneagent.plugin.config;

import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.oneagent.plugin.config.PluginConfigImpl;

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
        pluginProperties.put("exportPackages", "  com.test\t, hello, abc.xyz ");

        PluginConfigImpl impl = new PluginConfigImpl(globalProperties, pluginProperties);

        boolean enabled = impl.isEnabled();

        Assertions.assertThat(enabled).isFalse();
        Assertions.assertThat(impl.exportPackages()).containsExactly("com.test", "hello", "abc.xyz");
        Assertions.assertThat(impl.importPackages()).isEmpty();

        Assertions.assertThat(impl.getPluginActivator()).isEqualTo("io.oneagent.plugin.DefaultPluginActivator");
    }

    @Test
    public void testGlobalConfig() {

        Properties globalProperties = new Properties();
        globalProperties.put("version", "golbal-version");
        globalProperties.put(PluginConfigImpl.PLUGIN_CONFIG_PREFIX + "test" + ".enabled", "false");
        Properties pluginProperties = new Properties();
        pluginProperties.put("name", "test");
        pluginProperties.put("version", "plugin-version");

        PluginConfigImpl impl = new PluginConfigImpl(globalProperties, pluginProperties);

        System.err.println(impl.getVersion());

        boolean enabled = impl.isEnabled();

        Assertions.assertThat(enabled).isFalse();

        Assertions.assertThat(impl.getVersion()).isEqualTo("plugin-version");
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
