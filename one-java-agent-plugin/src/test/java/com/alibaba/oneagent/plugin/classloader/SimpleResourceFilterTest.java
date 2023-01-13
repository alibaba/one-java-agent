package com.alibaba.oneagent.plugin.classloader;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SimpleResourceFilterTest {

    @Test
    public void test() {

        SimpleResourceFilter filter = new SimpleResourceFilter(
                Arrays.asList("com.test", "org.slf4j", "ch.qos.logback."),
                Arrays.asList("test.xml", "aaa.properties", "conf/test.txt"));

        Assertions.assertThat(filter.matched("com/test")).isFalse();
        Assertions.assertThat(filter.matched("com/test/AAA.class")).isTrue();

        Assertions.assertThat(filter.matched("org/slf4j/Logger.class")).isTrue();

        Assertions.assertThat(filter.matched("ch/qos/logback.class")).isFalse();

        Assertions.assertThat(filter.matched("ch/qos/logback/Manager.class")).isTrue();

        Assertions.assertThat(filter.matched("test1.xml")).isFalse();
        Assertions.assertThat(filter.matched("test.xml")).isTrue();
        Assertions.assertThat(filter.matched("aaa.properties")).isTrue();

        Assertions.assertThat(filter.matched("conf/aaa.properties")).isFalse();

        Assertions.assertThat(filter.matched("conf/test.txt")).isTrue();

        Assertions.assertThat(filter.matched("conf/test.xml")).isFalse();
    }
}
