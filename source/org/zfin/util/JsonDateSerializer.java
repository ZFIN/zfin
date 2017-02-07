package org.zfin.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class JsonDateSerializer extends JsonSerializer<GregorianCalendar> {

    @Override
    public void serialize(GregorianCalendar date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String dateString = date.toZonedDateTime().toString();
        dateString = dateString.substring(0,dateString.indexOf("["));
        jsonGenerator.writeString(dateString);
    }
}
