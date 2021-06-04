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
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        fmt.setCalendar(date);
        String dateFormatted = fmt.format(date.getTime());
        jsonGenerator.writeString(dateFormatted);
    }
}
