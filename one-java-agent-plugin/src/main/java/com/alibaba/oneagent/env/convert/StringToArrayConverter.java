
package com.alibaba.oneagent.env.convert;

import java.lang.reflect.Array;

import com.alibaba.oneagent.env.ConversionService;
import com.alibaba.oneagent.utils.StringUtils;

final class StringToArrayConverter<T> implements Converter<String, T[]> {

    private ConversionService conversionService;

    public StringToArrayConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public T[] convert(String source, Class<T[]> targetType) {
        String[] strings = StringUtils.tokenizeToStringArray(source, ",");

        @SuppressWarnings("unchecked")
        T[] values = (T[]) Array.newInstance(targetType.getComponentType(), strings.length);
        for (int i = 0; i < strings.length; ++i) {
            @SuppressWarnings("unchecked")
            T value = (T) conversionService.convert(strings[i], targetType.getComponentType());

            values[i] = value;
        }

        return values;
    }

}
