package org.zfin.expression;

import org.apache.log4j.Logger;
import org.zfin.mutant.SequenceTargetingReagent;

/**
 * Entity class that maps to experiment table.
 */
public class ExperimentCondition implements Comparable<ExperimentCondition> {

    private String zdbID;
    private Experiment experiment;
    private SequenceTargetingReagent sequenceTargetingReagent;
    private String value;
    private ExperimentUnit unit;
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

    public SequenceTargetingReagent getSequenceTargetingReagent() {
        return sequenceTargetingReagent;
    }

    public void setSequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        this.sequenceTargetingReagent = sequenceTargetingReagent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ExperimentUnit getUnit() {
        return unit;
    }

    public void setUnit(ExperimentUnit unit) {
        this.unit = unit;
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

    public boolean isSequenceTargetingReagentCondition() {
        boolean strCondition = conditionDataType.getGroup().equalsIgnoreCase("morpholino") || conditionDataType.getGroup().equalsIgnoreCase("CRISPR") || conditionDataType.getGroup().equalsIgnoreCase("TALEN");
        if (strCondition && sequenceTargetingReagent == null) {
            String message = "No Sequence Targeting Reagent found for experiment " + experiment.getName() + " [" + zdbID + "]. ";
            message += "Publication: " + experiment.getPublication().getZdbID();
            logger.error(message);
            return false;
        }
        return strCondition;
    }

    public boolean isChemicalCondition() {
        return (conditionDataType.getGroup().equalsIgnoreCase("chemical"));
    }


    @Override
    public int compareTo(ExperimentCondition o) {
        if (o == null)
            return -1;
        if (conditionDataType.compareTo(o.getConditionDataType()) != 0)
            return conditionDataType.compareTo(o.getConditionDataType());
        else if (isSequenceTargetingReagentCondition() && o.isSequenceTargetingReagentCondition())
            return sequenceTargetingReagent.compareTo(o.getSequenceTargetingReagent());
        else //even if it's the same condition type, we still want consistent order, so use id..
            return getZdbID().compareTo(o.getZdbID());
    }
}
