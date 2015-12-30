package org.zfin.expression;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.EntityZdbID;

/**
 * Entity class that maps to experiment table.
 */
public class ExperimentCondition implements Comparable<ExperimentCondition>, EntityZdbID {

    private String zdbID;
    private Experiment experiment;
    private ConditionDataType conditionDataType;
    private String comments;

    private static Logger logger = Logger.getLogger(ExperimentCondition.class);

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public ConditionDataType getConditionDataType() {
        return conditionDataType;
    }

    public void setConditionDataType(ConditionDataType conditionDataType) {
        this.conditionDataType = conditionDataType;
    }

    public boolean isChemicalCondition() {
        return (conditionDataType.getGroup().equalsIgnoreCase("chemical"));
    }

    public boolean isHeatShock() {
        return (conditionDataType.getName().equalsIgnoreCase("heat shock"));
    }

    @Override
    public int compareTo(ExperimentCondition o) {
        if (o == null)
            return -1;
        if (conditionDataType.compareTo(o.getConditionDataType()) != 0)
            return conditionDataType.compareTo(o.getConditionDataType());
        else
            return getZdbID().compareTo(o.getZdbID());
    }

    @Override
    public String getAbbreviation() {
        return experiment.getName();
    }

    @Override
    public String getAbbreviationOrder() {
        return experiment.getName();
    }

    @Override
    public String getEntityType() {
        return "Environment";
    }

    @Override
    public String getEntityName() {
        return experiment.getName();
    }
}
