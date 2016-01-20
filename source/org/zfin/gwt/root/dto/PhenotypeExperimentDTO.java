package org.zfin.gwt.root.dto;

import org.zfin.gwt.root.util.NumberAwareStringComparatorDTO;

/**
 * Data Transfer Object corresponding to PhenotypeExperiment object,
 * a unique combination of Experiment, Figure and Stage range.
 */
public class PhenotypeExperimentDTO extends AbstractFigureStageDTO<PhenotypeStatementDTO> implements Comparable<PhenotypeExperimentDTO> {

    private FishDTO fish;
    private EnvironmentDTO environment;

    public FishDTO getFish() {
        return fish;
    }

    public void setFish(FishDTO fish) {
        this.fish = fish;
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

        if ((figure.getLabel() != null )) {
            if (!figure.getLabel().equals(efs.getFigure().getLabel()) &&
                    (figure.getLabel() != null && efs.getFigure().getLabel() != null)) {
                NumberAwareStringComparatorDTO comparator = new NumberAwareStringComparatorDTO();
                return comparator.compare(figure.getLabel(), efs.getFigure().getLabel());
            }
        }
        if (!fish.equals(efs.getFish()))
            return fish.getNameOrder().compareTo(efs.getFish().getNameOrder());
        if (!environment.equals(efs.getEnvironment()))
            return environment.compareTo(efs.getEnvironment());
        if (!start.getName().equals(efs.getStart().getName()))
            return (int) (start.getStartHours() * 10 - efs.getStart().getStartHours() * 10);
        if (!end.getName().equals(efs.getEnd().getName()))
            return (int) (end.getStartHours() * 10 - efs.getEnd().getStartHours() * 10);
        return 0;
    }


}