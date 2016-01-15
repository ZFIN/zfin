package org.zfin.search;

import java.util.Calendar;

/**
 * Created by kschaper on 9/10/14.
 */
public enum FacetQueryEnum {

    DATE_LAST_30_DAYS("Last 30 Days", "date:[NOW/DAY-30DAYS TO NOW/DAY]"),
    DATE_LAST_90_DAYS("Last 90 Days", "date:[NOW/DAY-90DAYS TO NOW/DAY]"),
    DATE_THIS_YEAR(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), "date:[NOW/YEAR+0YEAR TO NOW/YEAR+1YEAR]"),
    DATE_THIS_YEAR_MINUS_1(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 1), "date:[NOW/YEAR-1YEAR TO NOW/YEAR+0YEAR]"),
    DATE_THIS_YEAR_MINUS_2(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 2), "date:[NOW/YEAR-2YEAR TO NOW/YEAR-1YEAR]"),
    DATE_THIS_YEAR_MINUS_3(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 3), "date:[NOW/YEAR-3YEAR TO NOW/YEAR-2YEAR]"),
    DATE_THIS_YEAR_MINUS_4(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 4), "date:[NOW/YEAR-4YEAR TO NOW/YEAR-3YEAR]"),
    DATE_THIS_YEAR_MINUS_5(Integer.toString(Calendar.getInstance().get(Calendar.YEAR) - 5), "date:[NOW/YEAR-5YEAR TO NOW/YEAR-4YEAR]"),
    DATE_MORE_THAN_5_YEARS("More than 5 years", "date:[* TO NOW/YEAR-5YEAR]"),
    ANY_WILDTYPE("Any WT", "is_genotype_wildtype:true"),
    ANY_MUTANT("Any Mutant", "is_genotype_wildtype:false"),
    ANY_ZEBRAFISH_GENE("Any Zebrafish Gene", "zebrafish_gene:[* TO *]"),
    ANY_REPORTER_GENE("Any Reporter Gene", "reporter_gene:[* TO *]");

    private String query;
    private String label;

    FacetQueryEnum(String label, String query) {
        this.label = label;
        this.query = query;
    }

    public static FacetQueryEnum getFacetQueryEnum(String query) {
        if (query == null)
            return null;
        for (FacetQueryEnum facetQueryEnum : values()) {
            if (facetQueryEnum.getQuery().equals(query))
                return facetQueryEnum;
        }
        return null;
    }

    public String getQuery() {
        return query;
    }

    public String getLabel() {
        return label;
    }


}
