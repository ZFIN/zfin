package org.zfin.framework.api;

import java.util.StringJoiner;

public enum FieldFilter {
    ANTIBODY_NAME("filter.antibodyName"),
    PUBLICATION_TYPE("publication.type"),
    PUBLICATION_ID("publication.id"),
    PUBLICATION_AUTHOR("publication.shortAuthor"),
    FISH_NAME("fish.name"),
    FISH_TYPE("fish.type"),
    SEQUENCE_ACCESSION("sequence.accession"),
    CLONAL_TYPE("filter.clonalType"),
    ISOTYPE("filter.asisotypeay"),
    HOST("filter.host"),
    ASSAY("filter.assay"),
    ANTIGEN_GENE("filter.antigenGenes"),
    TARGET_GENE("filter.targetGene"),
    SEQUENCE_TYPE("sequence.type"),
    FILTER_TERM_NAME("termName"),
    RELATIONSHIP_TYPE("relationship.type"),
    GENE_ABBREVIATION("filter.geneAbbreviation"),
    NAME("name"),
    TARGET_NAME("targetName"),
    ENTITY_ID("entityId"),
    STR_NAME("strName"),
    REGULATORY_REGION("regulatoryRegion"),
    CODING_SEQUENCE("codingSequence"),
    SPECIES("species"),
    ZDB_ENTITY_TYPE("zdb.entity.type"),
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
