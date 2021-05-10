package org.zfin.expression.presentation;

import org.zfin.expression.ExpressionStageAnatomy;
import org.zfin.marker.Gene;

import java.util.List;

public class ExpressionSummaryBean {
    private Gene gene;
    private List<ExpressionStageAnatomy> xsaList;


    public Gene getGene() {
        if (gene == null) {
            gene = new Gene();
        }
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public List<ExpressionStageAnatomy> getXsaList() {
        return xsaList;
    }

    public void setXsaList(List<ExpressionStageAnatomy> xsaList) {
        this.xsaList = xsaList;
    }
}
