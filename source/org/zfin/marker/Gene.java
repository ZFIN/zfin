package org.zfin.marker ;

import org.zfin.properties.ZfinProperties;
import org.zfin.framework.NamedData;

public class Gene implements NamedData, Comparable<Gene> {
    public final static String CSS_CLASS = "genedom";

    private String zdbID;
    private String symbol;
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
