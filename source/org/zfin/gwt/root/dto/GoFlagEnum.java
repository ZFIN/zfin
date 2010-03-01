package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public enum GoFlagEnum implements IsSerializable{
    CONTRIBUTES_TO("contributes to"),
    NOT("not"),
    ;

    private final String value;

    private GoFlagEnum(String type) {
        this.value = type;
    }

    public String toString() {
        return this.value;
    }

    public static GoFlagEnum getType(String type) {
        for (GoFlagEnum t : values()) {
            if (t.toString().equals(type.trim()))
                return t;
        }
        throw new RuntimeException("No GoFlag named [" + type + "] found.");
    }

}
