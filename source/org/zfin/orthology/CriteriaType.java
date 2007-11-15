package org.zfin.orthology;

/**
 * Typesafe enumerated class that represents a species used in the orthology domain.
 */
public class CriteriaType {

    public static final CriteriaType GENE_SYMBOL = new CriteriaType("symbol");
    public static final CriteriaType CHROMOSOME = new CriteriaType("chromosome");

    private String name;

    private CriteriaType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
