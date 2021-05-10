package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum AttributionType implements IsSerializable {

    MARKER,
    FEATURE,
    GENOTYPE,
    FISH,
    ;

    private static final String STARS = "**";

    public String toString() {
        return STARS + name();
    }

    public static boolean isHeader(String value) {
        return (value == null || value.startsWith(STARS));
    }

}
