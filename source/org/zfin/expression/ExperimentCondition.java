package org.zfin.expression;

import org.apache.log4j.Logger;
import org.zfin.mutant.Morpholino;

/**
 * Entity class that maps to experiment table.
 */
public class ExperimentCondition implements Comparable<ExperimentCondition> {

    private String zdbID;
    private Experiment experiment;
    private Morpholino morpholino;
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

    public Morpholino getMorpholino() {
        return morpholino;
    }

    public void setMorpholino(Morpholino morpholino) {
        this.morpholino = morpholino;
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

    public boolean isMoCondition() {
        boolean moCondition = conditionDataType.getGroup().equalsIgnoreCase("morpholino");
        if (moCondition && morpholino == null) {
            String message = "No Morpholino found for experiment " + experiment.getName() + " [" + zdbID + "]. ";
            message += "Publication: " + experiment.getPublication().getZdbID();
            logger.error(message);
            return false;
        }
        return moCondition;
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
        else if (isMoCondition() && o.isMoCondition())
            return morpholino.compareTo(o.getMorpholino());
        else //even if it's the same condition type, we still want consistent order, so use id..
            return getZdbID().compareTo(o.getZdbID());
    }
}
