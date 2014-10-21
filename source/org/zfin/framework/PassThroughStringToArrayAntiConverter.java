package org.zfin.framework;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

/**
 * By default, Spring is configured to split a single value with commas into multiple values...
 *
 * Which is awful, because sometimes your single value has a comma because it's supposed to.
 *
 * It might be convenient
 */


public class PassThroughStringToArrayAntiConverter implements Converter<String, String[]>{
    @Override
    public String[] convert(String source) {
        String[] array = {source};
        return array;
    }
}