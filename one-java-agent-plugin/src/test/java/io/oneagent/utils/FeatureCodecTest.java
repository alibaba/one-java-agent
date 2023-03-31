package io.oneagent.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.oneagent.utils.FeatureCodec;

/**
 * 
 * @author hengyunabc 2020-09-17
 *
 */
public class FeatureCodecTest {

    @Test
    public void test() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("vvv", "aaa@0.0.1");
        map.put("array", "aaa@0.0.1,bbb@0.0.2-SNAPSHOT,ccc@0.0.0");

        String string = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toString(map);

        Map<String, String> map2 = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toMap(string);

        Assert.assertEquals(map, map2);

        System.err.println(string);
        System.err.println(map2);

    }

}
