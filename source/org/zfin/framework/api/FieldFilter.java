package org.zfin.framework.api;

import java.util.StringJoiner;

public enum FieldFilter {
    SEQUENCE_ACCESSION("sequence.accession"),
    SEQUENCE_TYPE("sequence.type"),
    FILTER_TERM_NAME("termName"),
    RELATIONSHIP_TYPE("relationship.type"),
    GENE_ABBREVIATION("filter.geneAbbreviation"),
    NAME("name"),
    FISH_NAME("fishName"),
    DISEASE_NAME("disease"),
    TARGET_NAME("targetName"),
    ENTITY_ID("entityId"),
    STR_NAME("strName"),
    REGULATORY_REGION("regulatoryRegion"),
    CODING_SEQUENCE("codingSequence"),
    SPECIES("species"),
    CONDITION_NAME("conditionName"),
    PHENOTYPE("phenotype"),
    ANTIBODY_NAME("antibodyName"),
    CITATION("citation"),
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
