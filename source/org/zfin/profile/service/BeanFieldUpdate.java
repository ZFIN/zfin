package org.zfin.profile.service;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Used by BeanCompareService to communicate which fields have been changed.
 */
public class BeanFieldUpdate implements Serializable{

    private String field;
    private Object from;
    private Object to;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BeanFieldUpdate");
        sb.append("{field='").append(field).append('\'');
        sb.append(", from=").append(from);
        sb.append(", to=").append(to);
        sb.append('}');
        return sb.toString();
    }

    public void setNullToTrueNull() {
        if (to instanceof String && StringUtils.equalsIgnoreCase((String) to, "null")) {
            setTo(null);
        }
    }

    public void setEmptyToNull() {
        if (to instanceof String && StringUtils.equals((String) to, "")) {
            setTo(null);
        }
    }

}
