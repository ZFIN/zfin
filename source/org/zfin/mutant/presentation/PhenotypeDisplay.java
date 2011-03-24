package org.zfin.mutant.presentation;

import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.publication.Publication;

import java.util.SortedMap;
import java.util.SortedSet;


public class PhenotypeDisplay implements Comparable<PhenotypeDisplay> {
    private PhenotypeStatement phenoStatement;

    private SortedMap<Publication, SortedSet<Figure>> figuresPerPub;

    public PhenotypeDisplay(PhenotypeStatement phenoStatement)  {
        this.phenoStatement = phenoStatement;
    }

    public Experiment getExperiment() {
        return phenoStatement.getPhenotypeExperiment().getGenotypeExperiment().getExperiment();
    }

    public int compareTo(PhenotypeDisplay o) {
        if (phenoStatement.equals(o.getPhenoStatement())) {
            return getExperiment().compareTo(o.getExperiment());
        }  else {
            if (phenoStatement.getEntity().compareTo(o.getPhenoStatement().getEntity()) == 0) {
                if (phenoStatement.getQuality().compareTo(o.getPhenoStatement().getQuality()) == 0)
                    return phenoStatement.getTag().compareTo(o.getPhenoStatement().getTag());
                else
                    return phenoStatement.getQuality().compareTo(o.getPhenoStatement().getQuality());
            } else {
                return phenoStatement.getEntity().compareTo(o.getPhenoStatement().getEntity());
            }
        }
    }


    public PhenotypeStatement getPhenoStatement() {
        return phenoStatement;
    }

    public void setPhenoStatement(PhenotypeStatement phenoStatement) {
        this.phenoStatement = phenoStatement;
    }

    public SortedMap<Publication, SortedSet<Figure>> getFiguresPerPub() {
        return figuresPerPub;
    }

    public void setFiguresPerPub(SortedMap<Publication, SortedSet<Figure>> figuresPerPub) {
        this.figuresPerPub = figuresPerPub;
    }
}