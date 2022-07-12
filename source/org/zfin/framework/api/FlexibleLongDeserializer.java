package org.zfin.framework.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class FlexibleLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        String numberString = parser.getText();
        if (numberString.contains(",")) {
            numberString = numberString.replaceAll(",", "");
        }
        return Long.valueOf(numberString);
    }

}