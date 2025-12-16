package org.zfin.framework.api;

import java.util.StringJoiner;

public enum FieldFilter {
    PUBLICATION_TYPE("publication.type"),
    PUBLICATION_ID("publication.id"),
    PUBLICATION_AUTHOR("publication.shortAuthor"),
    FISH_TYPE("fish.type"),
    SEQUENCE_ACCESSION("sequence.accession"),
    SEQUENCE_TYPE("sequence.type"),
    FILTER_TERM_NAME("termName"),
    FILTER_EVIDENCE("evidence"),
    FILTER_REF("ref"),
    RELATIONSHIP_TYPE("relationship.type"),
    GENE_ABBREVIATION("filter.geneAbbreviation"),
    NAME("name"),
    FISH_NAME("fish"),
    ASSAY("assay"),
    DISEASE_NAME("disease"),
    TARGET_NAME("targetName"),
    ENTITY_ID("entityId"),
    STR_NAME("reagent"),
    STR_TYPE("reagentType"),
    REGULATORY_REGION("regulatoryRegion"),
    CODING_SEQUENCE("codingSequence"),
    SPECIES("species"),
    CONDITION_NAME("conditionName"),
    PHENOTYPE("phenotype"),
    CLONAL_TYPE("clonalType"),
    ISOTYPE("isotype"),
    HOST("hostOrganism"),
    ZDB_ENTITY_TYPE("zdb.entity.type"),
    SUPER_TYPE("superType"),
    FOREIGN_DB("foreignDB"),
    DB_LINK_INFO("dbInfo"),
    TARGET_GENE("targetGene"),
    ANTIGEN_GENE("filter.antigenGenes"),
    ANTIBODY_NAME("antibodyName"),
    ANTIBODY("antibody"),
    QUALIFIER("qualifier"),
    CITATION("citation"),
    ANATOMY("anatomy"),

    STAGE("stage"),
    PROBE("probe"),
    EXPERIMENT("environment"), TYPE("type"), STATUS("status"), STATUS_EMPTY("empty"), TRANSCRIPT_ID("ID"), PLASMID("plasmid"), STRAIN("strain"), DISPLAY_GROUP("displayGroup"), NON_REF_STRAIN("nonRefStrain");

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
