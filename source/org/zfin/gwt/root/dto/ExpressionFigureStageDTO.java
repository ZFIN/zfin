package org.zfin.gwt.root.dto;

/**
 * Data Transfer Object corresponding to a unique combination of
 * Experiment, Figure and Stage range.
 */
public class ExpressionFigureStageDTO extends AbstractFigureStageDTO<ExpressedTermDTO> implements Comparable<ExpressionFigureStageDTO> {

    private ExperimentDTO experiment;
    private String expressedIn;
    private boolean patoExists;

    public String getExperimentName() {
        return experiment.getEnvironment().getName();
    }

    public ExperimentDTO getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentDTO experiment) {
        this.experiment = experiment;
    }

    public String getExpressedIn() {
        return expressedIn;
    }

    public void setExpressedIn(String expressedIn) {
        this.expressedIn = expressedIn;
    }

    public boolean isPatoExists() {
        return patoExists;
    }

    public void setPatoExists(boolean patoExists) {
        this.patoExists = patoExists;
    }

    public String getUniqueID() {
        StringBuilder sb = new StringBuilder(experiment.getExperimentZdbID());
        sb.append(":");
        sb.append(figure.getZdbID());
        sb.append(":");
        sb.append(start.getZdbID());
        sb.append(":");
        sb.append(end.getZdbID());
        return sb.toString();
    }

    /**
     * This is a compound ID of the format:
     * experimentID:figureID:startStageID:endStageID.
     *
     * @param uniqueID concatenated unique ID.
     */
    public void setUniqueID(String uniqueID) {
        String[] ids = uniqueID.split(":");
        if (ids.length != 4)
            throw new RuntimeException("unique id '" + uniqueID + "'not in the format experimentID:figureID:startStageID:endStageID");
        if (experiment == null)
            experiment = new ExperimentDTO();
        experiment.setExperimentZdbID(ids[0]);
        if (figure == null)
            figure = new FigureDTO();
        figure.setZdbID(ids[1]);
        if (start == null)
            start = new StageDTO();
        if (end == null)
            end = new StageDTO();
        start.setZdbID(ids[2]);
        end.setZdbID(ids[3]);
    }

    public int compareTo(ExpressionFigureStageDTO efs) {
        if (efs == null)
            return -1;
        if (!figure.getOrderingLabel().equals(efs.figure.getOrderingLabel()))
            return figure.getOrderingLabel().compareTo(efs.figure.getOrderingLabel());
        if (!experiment.equals(efs.getExperiment()))
            return experiment.compareTo(efs.getExperiment());
        if (!start.getNameLong().equals(efs.getStart().getNameLong()))
            return (int) (start.getStartHours() * 10 - efs.getStart().getStartHours() * 10);
        if (!end.getNameLong().equals(efs.getEnd().getNameLong()))
            return (int) (end.getStartHours() * 10 - efs.getEnd().getStartHours() * 10);
        return 0;
    }
}