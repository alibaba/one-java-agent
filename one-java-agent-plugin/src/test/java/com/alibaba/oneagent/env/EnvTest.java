package com.alibaba.oneagent.env;

import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.alibaba.oneagent.utils.StringUtils;

/**
 * 
 * @author hengyunabc 2021-06-02
 *
 */
public class EnvTest {

    @Test
    public void test() {
        String source = "  \tabc , \t  xxx, yyy zzzz, zzzz   ";
        String[] strings = StringUtils.tokenizeToStringArray(source, ",");
        Assertions.assertThat(strings).containsExactly("abc", "xxx", "yyy zzzz", "zzzz");
    }

    public void test_PropertyResolver() {
        MutablePropertySources propertySources = new MutablePropertySources();
        PropertySourcesPropertyResolver propertyResolver = new PropertySourcesPropertyResolver(propertySources);

        Properties properties = new Properties();
        properties.put("packages", "  com.test.abc, xyz, xxx  ");

        propertySources.addLast(new PropertiesPropertySource("test", properties));

        String[] strings = propertyResolver.getProperty("packages", String[].class, new String[0]);
        Assertions.assertThat(strings).containsExactly("com.test.abc", "xyz", "xxx");
        
        String[] emptyStrings = propertyResolver.getProperty("empty", String[].class, new String[0]);
        Assertions.assertThat(emptyStrings).isEmpty();
    }

}
