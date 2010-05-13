package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public enum GoEvidenceQualifier implements IsSerializable {
    CONTRIBUTES_TO("contributes to"),
    NOT("not"),;

    private final String value;

    private GoEvidenceQualifier(String type) {
        this.value = type;
    }

    public String toString() {
        return this.value;
    }

    public static GoEvidenceQualifier getType(String type) {
        for (GoEvidenceQualifier t : values()) {
            if (t.toString().equals(type.trim()))
                return t;
        }
        throw new RuntimeException("No GoFlag named [" + type + "] found.");
    }

}
