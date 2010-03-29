package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * Domain object that does not map to a database table directly.
 * It is a unique combination of Experiment, Figure, start and end stage.
 */
public class MutantFigureStage {

    private GenotypeExperiment genotypeExperiment;
    private Figure figure;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private Set<Phenotype> phenotypes;
    private Publication publication;

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        if (this.start != null && start != null && !this.start.equals(start))
            throw new RuntimeException("The start state has to be unique!");
        this.start = start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        if (this.end != null && end != null && !this.end.equals(end))
            throw new RuntimeException("The end state has to be unique!");
        this.end = end;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }

    public Set<Phenotype> getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(Set<Phenotype> phenotypes) {
        this.phenotypes = phenotypes;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutantFigureStage that = (MutantFigureStage) o;

        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (genotypeExperiment != null ? !genotypeExperiment.getZdbID().equals(that.genotypeExperiment.getZdbID()) : that.genotypeExperiment != null)
            return false;
        if (figure != null ? !figure.getZdbID().equals(that.figure.getZdbID()) : that.figure != null) return false;
        return !(start != null ? !start.equals(that.start) : that.start != null);

    }

    @Override
    public int hashCode() {
        int result = genotypeExperiment != null ? genotypeExperiment.hashCode() : 0;
        result = 31 * result + (figure != null ? figure.getZdbID().hashCode() : 0);
        result = 31 * result + (start != null ? start.getZdbID().hashCode() : 0);
        result = 31 * result + (end != null ? end.getZdbID().hashCode() : 0);
        return result;
    }

    public void addPhenotype(Phenotype result) {
        if (phenotypes == null)
            phenotypes = new HashSet<Phenotype>(5);
        phenotypes.add(result);
        start = result.getStartStage();
        end = result.getEndStage();
    }

    /**
     * Return phenotypes that match this mutant in
     * 1) start stage
     * 2) end stage
     * 3) publication
     * 4) contain the figure (note: phenotype may be associated to more than one figure).
     *
     * @return set of mutants
     */
    public Set<Phenotype> getMatchingMutantPhenotypes() {
        Set<Phenotype> phenos = genotypeExperiment.getPhenotypes();
        if (phenos == null)
            return null;
        Set<Phenotype> mutantPhenotypes = new HashSet<Phenotype>(5);
        for (Phenotype pheno : phenos) {
            if (pheno.getStartStage().equals(start) && pheno.getEndStage().equals(end))
                if (pheno.getPublication().getZdbID().equals(publication.getZdbID())) {
                    if (pheno.getFigures().contains(figure))
                        mutantPhenotypes.add(pheno);
                }
        }
        return mutantPhenotypes;
    }
}