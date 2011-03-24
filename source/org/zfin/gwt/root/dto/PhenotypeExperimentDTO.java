package org.zfin.gwt.root.dto;

import org.zfin.gwt.root.util.NumberAwareStringComparatorDTO;

/**
 * Data Transfer Object corresponding to PhenotypeExperiment object,
 * a unique combination of Experiment, Figure and Stage range.
 */
public class PhenotypeExperimentDTO extends AbstractFigureStageDTO<PhenotypeStatementDTO> implements Comparable<PhenotypeExperimentDTO>{

    private GenotypeDTO genotype;
    private EnvironmentDTO environment;

    public GenotypeDTO getGenotype() {
        return genotype;
    }

    public void setGenotype(GenotypeDTO genotype) {
        this.genotype = genotype;
    }

    public EnvironmentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentDTO environment) {
        this.environment = environment;
    }

    public int compareTo(PhenotypeExperimentDTO efs) {
        if (efs == null)
            return -1;
        if (figure.getLabel() == null && efs.getFigure().getLabel() != null)
            return -1;
        if (figure.getLabel() != null && efs.getFigure().getLabel() == null)
            return 1;

        if ((figure.getLabel() != null && efs.getFigure().getLabel() != null)) {
            if (!figure.getLabel().equals(efs.getFigure().getLabel()) &&
                    (figure.getLabel() != null && efs.getFigure().getLabel() != null)){
                NumberAwareStringComparatorDTO comparator = new NumberAwareStringComparatorDTO();
                return comparator.compare(figure.getLabel(),efs.getFigure().getLabel());
            }
        }
        if (!genotype.equals(efs.getGenotype()))
            return genotype.compareTo(efs.getGenotype());
        if (!environment.equals(efs.getEnvironment()))
            return environment.compareTo(efs.getEnvironment());
        if (!start.getName().equals(efs.getStart().getName()))
            return (int) (start.getStartHours() * 10 - efs.getStart().getStartHours() * 10);
        if (!end.getName().equals(efs.getEnd().getName()))
            return (int) (end.getStartHours() * 10 - efs.getEnd().getStartHours() * 10);
        return 0;
    }

    
}