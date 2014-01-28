package org.zfin.profile.service;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Used by BeanCompareService to communicate which fields have been changed.
 */
public class BeanFieldUpdate implements Serializable{

    String field ;
    Object from ;
    Object to ;
    Class fieldType ;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getFrom() {
        return from;
    }

    public void setFrom(Object from) {
        this.from = from;
    }

    public Object getTo() {
        return to;
    }

    public void setTo(Object to) {
        this.to = to;
    }

    public Class getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BeanFieldUpdate");
        sb.append("{field='").append(field).append('\'');
        sb.append(", from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", fieldType=").append(fieldType.toString());
        sb.append('}');
        return sb.toString();
    }

    public void setNullToTrueNull() {
        if (fieldType == String.class && StringUtils.equalsIgnoreCase((String)to, "null"))
            setTo(null);
    }

    public void setEmptyToNull() {
        if (fieldType == String.class && StringUtils.equals((String)to, ""))
            setTo(null);
    }

}
