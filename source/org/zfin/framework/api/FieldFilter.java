package org.zfin.framework.api;

import java.util.StringJoiner;

public enum FieldFilter {
    SEQUENCE_ACCESSION("sequence.accession"),
    SEQUENCE_TYPE("sequence.type"),
    RELATIONSHIP_TYPE("relationship.type")
    ;
    private String name;

    FieldFilter(String name) {
        this.name = name;
    }

    public static FieldFilter getFieldFilterByName(String name) {
        if (name == null)
            return null;
        for (FieldFilter sort : values()) {
            if (sort.name.equals(name))
                return sort;
        }
        return null;
    }

    public static String getAllValues() {
        StringJoiner values = new StringJoiner(",");
        for (FieldFilter sorting : values())
            values.add(sorting.name);
        return values.toString();
    }

    public String getName() {
        return name;
    }

}
