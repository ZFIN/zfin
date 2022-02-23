package org.zfin.framework.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class FlexibleIntegerDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String numberString = parser.getText();
        if (numberString.contains(",")) {
            numberString = numberString.replaceAll(",", "");
        }
        return Integer.valueOf(numberString);
    }

}