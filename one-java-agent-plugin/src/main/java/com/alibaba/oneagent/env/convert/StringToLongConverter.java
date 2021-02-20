
package com.alibaba.oneagent.env.convert;

final class StringToLongConverter implements Converter<String, Long> {
    @Override
    public Long convert(String source, Class<Long> targetType) {
        return Long.parseLong(source);
    }
}
