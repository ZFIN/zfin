package org.zfin.fish.presentation;

import org.zfin.expression.*;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.*;

// TODO: looks like a lot of copied code in GenotypeBean, AbstractFishViewBean, and GenotypeExperimentBean. Can some be refactored out?

public class AbstractFishViewBean {
    protected Genotype genotype;
    private List<GenotypeFigure> genotypeFigures;
    private List<PhenotypeStatementWarehouse> phenoStatements;
    private List<ExpressionStatement> expressionStatements;
    private List<PhenotypeDisplay> phenoDisplays;
    private List<ExpressionDisplay> expressionDisplays;
    private int totalNumberOfPublications;

    public List<GenotypeFigure> getGenotypeFigures() {
        return genotypeFigures;
    }

    public void setGenotypeFigures(List<GenotypeFigure> genotypeFigures) {
        this.genotypeFigures = genotypeFigures;
    }

    public List<PhenotypeStatementWarehouse> getPhenoStatements() {
        return phenoStatements;
    }

    public void setPhenoStatements(List<PhenotypeStatementWarehouse> phenoStatements) {
        this.phenoStatements = phenoStatements;
    }

    public List<ExpressionStatement> getExpressionStatements() {
        return expressionStatements;
    }

    public void setExpressionStatements(List<ExpressionStatement> expressionStatements) {
        this.expressionStatements = expressionStatements;
    }

    public void setExpressionDisplays(List<ExpressionDisplay> expressionDisplays) {
        this.expressionDisplays = expressionDisplays;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public List<PhenotypeDisplay> getPhenoDisplays() {
        if (phenoStatements == null) {
            return null;
        }

        return PhenotypeService.getPhenotypeDisplays(phenoStatements,"condition", "phenotypeStatement");
    }

    public void setPhenoDisplays(List<PhenotypeDisplay> phenoDisplays) {
        this.phenoDisplays = phenoDisplays;
    }

    public int getNumberOfPhenoDisplays() {
        if (phenoStatements == null || phenoStatements.size() == 0) {
            return 0;
        } else {
            if (phenoDisplays == null) {
                phenoDisplays = PhenotypeService.getPhenotypeDisplays(phenoStatements, "condition", "phenotypeStatement");
            }

            if (phenoDisplays == null) {
                return 0;
            } else {
                return phenoDisplays.size();
            }
        }
    }

    public int getTotalNumberOfPublications() {
        return totalNumberOfPublications;
    }

    public void setTotalNumberOfPublications(int totalNumberOfPublications) {
        this.totalNumberOfPublications = totalNumberOfPublications;
    }

}


