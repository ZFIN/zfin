package org.zfin.marker;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.zfin.framework.NamedData;

@Entity
@Table(name = "marker")
public class Gene implements NamedData, Comparable<Gene> {
    public final static String CSS_CLASS = "genedom";

    @Id
    @Column(name = "mrkr_zdb_id")
    private String zdbID;

    @Column(name = "mrkr_abbrev")
    private String symbol;

    @Column(name = "mrkr_name")
    private String name;


    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return getSymbol();
    }

    public String getNameHtml() {
        return "<span class=\"" + CSS_CLASS + "\">" + getName() + "</span>";

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int compareTo(Gene anotherGene) {
        return symbol.compareTo(anotherGene.getSymbol());
    }
}
