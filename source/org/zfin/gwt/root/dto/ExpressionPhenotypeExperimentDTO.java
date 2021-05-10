package org.zfin.gwt.root.dto;

import org.zfin.gwt.root.util.NumberAwareStringComparatorDTO;

/**
 * Data Transfer Object corresponding to PhenotypeExperiment object,
 * a unique combination of Experiment, Figure and Stage range.
 */
public class ExpressionPhenotypeExperimentDTO extends AbstractFigureStageDTO<ExpressionPhenotypeStatementDTO> implements Comparable<ExpressionPhenotypeExperimentDTO> {

    private FishDTO fish;
    private ExperimentDTO experiment;

    public FishDTO getFish() {
        return fish;
    }

    public void setFish(FishDTO fish) {
        this.fish = fish;
    }

    public ExperimentDTO getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentDTO experiment) {
        this.experiment = experiment;
    }

    public int compareTo(ExpressionPhenotypeExperimentDTO efs) {
        if (efs == null)
            return -1;
        if (figure.getLabel() == null && efs.getFigure().getLabel() != null)
            return -1;
        if (figure.getLabel() != null && efs.getFigure().getLabel() == null)
            return 1;

        if ((figure.getLabel() != null && efs.getFigure().getLabel() != null)) {
            if (!figure.getLabel().equals(efs.getFigure().getLabel()) &&
                    (figure.getLabel() != null && efs.getFigure().getLabel() != null)) {
                NumberAwareStringComparatorDTO comparator = new NumberAwareStringComparatorDTO();
                return comparator.compare(figure.getLabel(), efs.getFigure().getLabel());
            }
        }
        if (!fish.equals(efs.getFish()))
            return fish.compareTo(efs.getFish());
        if (!experiment.equals(efs.getExperiment()))
            return experiment.compareTo(efs.getExperiment());
        if (!start.getName().equals(efs.getStart().getName()))
            return (int) (start.getStartHours() * 10 - efs.getStart().getStartHours() * 10);
        if (!end.getName().equals(efs.getEnd().getName()))
            return (int) (end.getStartHours() * 10 - efs.getEnd().getStartHours() * 10);
        return 0;
    }


}