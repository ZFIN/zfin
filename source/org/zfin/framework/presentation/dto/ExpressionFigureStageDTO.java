package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object corresponding to a unique combination of
 * Experiment, Figure and Stage range.
 */
public class ExpressionFigureStageDTO implements IsSerializable, Comparable<ExpressionFigureStageDTO> {

    private ExperimentDTO experiment;
    private String figureID;
    private String figureLabel;
    private String figureOrderingLabel;
    private StageDTO start;
    private StageDTO end;
    private String expressedIn;
    private boolean patoExists;
    private List<ExpressedTermDTO> expressedTerms = new ArrayList<ExpressedTermDTO>();

    public String getFigureID() {
        return figureID;
    }

    public void setFigureID(String figureID) {
        this.figureID = figureID;
    }

    public String getFigureLabel() {
        return figureLabel;
    }

    public void setFigureLabel(String figureLabel) {
        this.figureLabel = figureLabel;
    }

    public String getExperimentName() {
        return experiment.getEnvironmentDisplayValue();
    }

    /**
     * Return the stage range in the format:
     * [start stage] - [end stage]
     * if start and end stage are the same only return the start stage name.
     *
     * @return stage range.
     */
    public String getStageRange() {
        if (start.getZdbID().equals(end.getZdbID()))
            return start.getName();
        return start.getName() + " - " + end.getName();
    }

    public String getAnatomyTerms() {
        return null;
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

    public List<ExpressedTermDTO> getExpressedTerms() {
        return expressedTerms;
    }

    public void setExpressedTerms(List<ExpressedTermDTO> expressedTerms) {
        this.expressedTerms = expressedTerms;
    }

    public StageDTO getStart() {
        return start;
    }

    public void setStart(StageDTO start) {
        this.start = start;
    }

    public StageDTO getEnd() {
        return end;
    }

    public void setEnd(StageDTO end) {
        this.end = end;
    }

    public String getFigureOrderingLabel() {
        return figureOrderingLabel;
    }

    public void setFigureOrderingLabel(String figureOrderingLabel) {
        this.figureOrderingLabel = figureOrderingLabel;
    }

    public void addExpressedTerm(ExpressedTermDTO term) {
        expressedTerms.add(term);
    }

    public String getUniqueID() {
        StringBuilder sb = new StringBuilder();
        sb.append(experiment.getExperimentZdbID());
        sb.append(":");
        sb.append(figureID);
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
        figureID = ids[1];
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
        if (!figureOrderingLabel.equals(efs.getFigureOrderingLabel()))
            return figureOrderingLabel.compareTo(efs.getFigureOrderingLabel());
        if (!experiment.equals(efs.getExperiment()))
            return experiment.compareTo(efs.getExperiment());
        if (!start.getName().equals(efs.getStart().getName()))
            return (int) (start.getStartHours() * 10 - efs.getStart().getStartHours() * 10);
        if (!end.getName().equals(efs.getEnd().getName()))
            return (int) (end.getStartHours() * 10 - efs.getEnd().getStartHours() * 10);
        return 0;
    }
}