package org.zfin.expression.presentation;


import java.util.List;

public class ExpressionSearchCriteria {

    String geneField;
    String exactGene;
    List<String> anatomy;

    public String getGeneField() {
        return geneField;
    }

    public void setGeneField(String geneField) {
        this.geneField = geneField;
    }

    public String getExactGene() {
        return exactGene;
    }

    public void setExactGene(String exactGene) {
        this.exactGene = exactGene;
    }

    public List<String> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(List<String> anatomy) {
        this.anatomy = anatomy;
    }
}
