package com.alibaba.oneagent.plugin.share;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * 
 * @author hengyunabc 2021-06-03
 *
 */
public class SharedServiceImplTest {

    @Test
    public void test_findResourcePackage() {
        Assertions.assertThat(SharedServiceImpl.findResourcePackage("com/abc/xxx.class")).isEqualTo("com/abc/");
        Assertions.assertThat(SharedServiceImpl.findResourcePackage("com/xxx.class")).isEqualTo("com/");
        Assertions.assertThat(SharedServiceImpl.findResourcePackage("xxx.class")).isNull();
    }

    @Test
    public void test_findClassPackage() {
        Assertions.assertThat(SharedServiceImpl.findClassPackage("com.abc.Hello")).isEqualTo("com.abc.");
        Assertions.assertThat(SharedServiceImpl.findClassPackage("com.abc")).isEqualTo("com.");
        Assertions.assertThat(SharedServiceImpl.findClassPackage("com")).isNull();
    }
}
