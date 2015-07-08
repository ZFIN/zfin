package org.zfin.infrastructure;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

public class CustomCalendarEditor extends PropertyEditorSupport {

    private DateFormat format;

    public CustomCalendarEditor(DateFormat format) {
        this.format = format;
    }

    @Override
    public void setAsText(String text) {
        if (!text.isEmpty()) {
            try {
                Date date = format.parse(text);
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                setValue(calendar);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Could not parse date: " + text);
            }
        }
    }

    @Override
    public String getAsText() {
        GregorianCalendar calendar = (GregorianCalendar) getValue();
        return (calendar == null) ? "" : format.format(calendar.getTime());
    }

}
