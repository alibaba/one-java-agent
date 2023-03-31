package io.oneagent.plugin.classloader;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.oneagent.plugin.classloader.SimpleClassFilter;

public class SimpleClassFilterTest {

    @Test
    public void test() {

        SimpleClassFilter classFilter = new SimpleClassFilter(
                Arrays.asList("com.test", "org.slf4j", "ch.qos.logback."));

        Assertions.assertThat(classFilter.matched("com.test.AAA")).isTrue();
        Assertions.assertThat(classFilter.matched("com.test")).isFalse();
        Assertions.assertThat(classFilter.matched("org.slf4j.Logger")).isTrue();
        Assertions.assertThat(classFilter.matched("org.slf4j.aaa.Logger")).isTrue();
        Assertions.assertThat(classFilter.matched("ch.qos.logback")).isFalse();
        Assertions.assertThat(classFilter.matched("ch.qos.logback.Logger")).isTrue();
    }

}
