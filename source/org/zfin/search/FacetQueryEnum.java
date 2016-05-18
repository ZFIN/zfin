package org.zfin.search;

import java.util.Calendar;

public enum FacetQueryEnum {

    DATE_LAST_30_DAYS(FieldName.DATE, "Last 30 Days", "date:[NOW/DAY-30DAYS TO NOW/DAY]"),
    DATE_LAST_90_DAYS(FieldName.DATE, "Last 90 Days", "date:[NOW/DAY-90DAYS TO NOW/DAY]"),
    DATE_THIS_YEAR(FieldName.DATE, Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), "date:[NOW/YEAR+0YEAR TO NOW/YEAR+1YEAR}"),
    DATE_THIS_YEAR_MINUS_1(FieldName.DATE, Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 1), "date:[NOW/YEAR-1YEAR TO NOW/YEAR+0YEAR}"),
    DATE_THIS_YEAR_MINUS_2(FieldName.DATE, Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 2), "date:[NOW/YEAR-2YEAR TO NOW/YEAR-1YEAR}"),
    DATE_THIS_YEAR_MINUS_3(FieldName.DATE, Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 3), "date:[NOW/YEAR-3YEAR TO NOW/YEAR-2YEAR}"),
    DATE_THIS_YEAR_MINUS_4(FieldName.DATE, Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 4), "date:[NOW/YEAR-4YEAR TO NOW/YEAR-3YEAR}"),
    DATE_THIS_YEAR_MINUS_5(FieldName.DATE, Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 5), "date:[NOW/YEAR-5YEAR TO NOW/YEAR-4YEAR}"),
    DATE_MORE_THAN_5_YEARS(FieldName.DATE, "More than 5 years", "date:[* TO NOW/YEAR-5YEAR}"),
    ANY_WILDTYPE(FieldName.GENOTYPE, "Any WT", "is_genotype_wildtype:true"),
    ANY_MUTANT(FieldName.GENOTYPE, "Any Mutant", "is_genotype_wildtype:false"),
    ANY_ZEBRAFISH_GENE(FieldName.ZEBRAFISH_GENE, "Any Zebrafish Gene", "zebrafish_gene:[* TO *]"),
    ANY_REPORTER_GENE(FieldName.REPORTER_GENE, "Any Reporter Gene", "reporter_gene:[* TO *]");

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
