package org.zfin.search;

public enum FacetQueryEnum {

    ANY_WILDTYPE(FieldName.GENOTYPE, "Any WT", FieldName.IS_GENOTYPE_WILDTYPE.getName() + ":true"),
    ANY_MUTANT(FieldName.GENOTYPE, "Any Mutant", FieldName.IS_GENOTYPE_WILDTYPE.getName() + ":false"),
    ANY_ZEBRAFISH_GENE(FieldName.ZEBRAFISH_GENE, "Any Zebrafish Gene", FieldName.ZEBRAFISH_GENE.getName() + ":[* TO *]"),
    ANY_REPORTER_GENE(FieldName.REPORTER_GENE, "Any Reporter Gene", FieldName.REPORTER_GENE.getName() + ":[* TO *]"),
    NONE_SEQUENCE_TARGETING_REAGENT(FieldName.SEQUENCE_TARGETING_REAGENT, "Exclude All",
                    "-" + FieldName.SEQUENCE_TARGETING_REAGENT.getName() + ":[* TO *]");

    private String query;
    private String label;
    private FieldName fieldName;

    FacetQueryEnum(FieldName fieldName, String label, String query) {
        this.label = label;
        this.query = query;
        this.fieldName = fieldName;
    }

    public static FacetQueryEnum getFacetQueryEnum(String query) {
        if (query == null) {
            return null;
        }
        for (FacetQueryEnum facetQueryEnum : values()) {
            if (facetQueryEnum.getQuery().equals(query)) {
                return facetQueryEnum;
            }
        }
        return null;
    }

    public String getQuery() {
        return query;
    }

    public String getLabel() {
        return label;
    }

    public FieldName getFieldName() {
        return fieldName;
    }
}
