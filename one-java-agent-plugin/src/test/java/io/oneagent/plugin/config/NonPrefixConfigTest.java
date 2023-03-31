package io.oneagent.plugin.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import io.oneagent.plugin.config.BinderUtils;
import io.oneagent.plugin.config.PluginConfigImpl;
import io.oneagent.plugin.config.ErrorProperties.IncludeStacktrace;

/**
 * 测试配置类 `@Config` 没有配置 prefix的情况
 * @author hengyunabc 2023-02-16
 *
 */
public class NonPrefixConfigTest extends PropertiesTestBase {

    @Test
    public void test() throws UnknownHostException {
        Properties p = new Properties();
        p.put("name", TEST_PLUGIN_NAME);

        p.put("port", "8080");

        p.put("address", "192.168.1.1");

        p.put("ssl.enabled", "true");
        p.put("ssl.protocol", "TLS");
        p.put("ssl.ciphers", "abc, efg ,hij");

        p.put("error.includeStacktrace", "ALWAYS");

        PluginConfigImpl pluginConfig = new PluginConfigImpl(p, p);

        NonPrefixConfig serverProperties = new NonPrefixConfig();

        BinderUtils.inject(pluginConfig, serverProperties);

        Assert.assertEquals(serverProperties.getPort().intValue(), 8080);

        Assert.assertEquals(serverProperties.getAddress(), InetAddress.getByName("192.168.1.1"));

        Assert.assertEquals(serverProperties.getSsl().getProtocol(), "TLS");
        Assert.assertTrue(serverProperties.getSsl().isEnabled());

        Assert.assertArrayEquals(serverProperties.getSsl().getCiphers(), new String[] { "abc", "efg", "hij" });

        Assert.assertEquals(serverProperties.getError().getIncludeStacktrace(), IncludeStacktrace.ALWAYS);

    }

    @Test
    public void testSystemProperties() {
        Properties p = new Properties();
        p.put("name", TEST_PLUGIN_NAME);
        p.put("system.test.systemKey", "kkk");
        p.put("system.test.nonSystemKey", "xxxx");
        p.put("system.test.systemIngeger", "123");

        System.setProperty(prefix + "system.test.systemKey", "ssss");
        System.setProperty(prefix + "system.test.systemIngeger", "110");

        PluginConfigImpl pluginConfig = new PluginConfigImpl(p, p);

        String property = pluginConfig.getProperty("system.test.systemKey");

        System.err.println(property);

        SystemObject systemObject = new SystemObject();

        BinderUtils.inject(pluginConfig, systemObject);

        Assert.assertEquals(systemObject.getSystemKey(), "ssss");
        Assert.assertEquals(systemObject.getNonSystemKey(), "xxxx");
        Assert.assertEquals(systemObject.getSystemIngeger(), 110);

    }

    @Test
    public void testGlobal_And_SystemProperties() {
        Properties p = new Properties();
        p.put("name", TEST_PLUGIN_NAME);
        p.put("system.test.systemKey", "kkk");
        p.put("system.test.nonSystemKey", "xxxx");
        p.put("system.test.systemIngeger", "123");

        Properties globalProperties = new Properties();
        globalProperties.put(prefix + "system.test.systemIngeger", "111");
        globalProperties.put(prefix + "system.test.nonSystemKey", "valueFromGlobalProperties");

        System.setProperty(prefix + "system.test.systemKey", "yyyyy");
        System.setProperty(prefix + "system.test.systemIngeger", "999");

        PluginConfigImpl pluginConfig = new PluginConfigImpl(globalProperties, p);

        String property = pluginConfig.getProperty("system.test.systemKey");

        System.err.println(property);

        SystemObject systemObject = new SystemObject();

        BinderUtils.inject(pluginConfig, systemObject);

        Assert.assertEquals(systemObject.getSystemKey(), "yyyyy");
        Assert.assertEquals(systemObject.getNonSystemKey(), "valueFromGlobalProperties");
        Assert.assertEquals(systemObject.getSystemIngeger(), 999);

    }
}
