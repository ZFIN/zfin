package org.zfin.profile.service;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Used by BeanCompareService to communicate which fields have been changed.
 */
@Setter
@Getter
public class BeanFieldUpdate implements Cloneable, Serializable {

    private String field;
    private Object from;
    private Object to;

    @Override
    public String toString() {
        return "BeanFieldUpdate{field='%s', from=%s, to=%s}".formatted(field, from, to);
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

    @Override
    public BeanFieldUpdate clone() {
        try {
            return (BeanFieldUpdate) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}
