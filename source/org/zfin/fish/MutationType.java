package org.zfin.fish;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration of mutation types supported in ZFIN.
 * This includes Morpholino which currently only in the warehouse
 * is an accepted mutation type.
 */
public enum MutationType {
    COMPLEX("Complex"),
    DEFICENCY("Deficiency"),
    INSERTION("Insertion"),
    MORPHOLINO("Morpholino"),
    POINT_MUTATION("Point Mutation"),
    SMALL_DELETION("Small Deletion"),
    INVERSION("Inversion"),
    TRANSLOCATION("Translocation"),
    TRANSGENIC_INSERTION("Transgenic Insertion"),
    UNKNOWN("Unknown"),
    // Todo: db column needs to be increased to hold the full name
    UNSPECIFIED_TRANSGENIC_INSERTION("Unspecified Transgenic Insertion"),
    NON_ALLELIC_TRANSGENIC_INSERTION("Transgenic Insertion, non-allelic"),
    UNSPECIFIED("Unspecified");

    private String name;

    private MutationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MutationType getMutationType(String name) {
        if (StringUtils.isEmpty(name))
            return null;
        for (MutationType type : values())
            if (type.getName().equals(name))
                return type;
        throw new RuntimeException("No Mutation Type Display with name " + name + " found!");
    }
}
