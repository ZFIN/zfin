package org.zfin.infrastructure;

/**
 * This class maps to a fast search table in the db for lookup of
 * marker names and aliases and other entities.
 */
public abstract class AllNamesFastSearch {

    private int id;
    private String name;
    private String nameLowerCase;
    private String precedence;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLowerCase() {
        return nameLowerCase;
    }

    public void setNameLowerCase(String nameLowerCase) {
        this.nameLowerCase = nameLowerCase;
    }

    public String getPrecedence() {
        return precedence;
    }

    public void setPrecedence(String precedence) {
        this.precedence = precedence;
    }

    public static Precedence[] getGenePrecedences() {
        return new Precedence[]{Precedence.GENE_ALIAS, Precedence.GENE_SYMBOL, Precedence.CURRENT_NAME, Precedence.CURRENT_SYMBOL};
    }

    public enum Precedence {
        ACCESSION_NUMBER("Accession number"),
        CURRENT_NAME("Current name"),
        CURRENT_SYMBOL("Current symbol"),
        GENE_ALIAS("Gene alias"),
        gene_name("Gene name"),
        GENE_SYMBOL("Gene symbol"),
        PREVIOUS_NAME("Previous name");

        private String name;

        Precedence(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}