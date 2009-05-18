package org.zfin.expression;

import org.zfin.publication.Publication;

import java.util.Set;
import java.util.HashSet;

/**
 * Domain object.
 */
public class Experiment {

    public static final String STANDARD = "_Standard";
    public static final String GENERIC_CONTROL = "_Generic-control";

    private String zdbID;
    private String name;
    private Publication publication;
    private Set<ExperimentCondition> experimentConditions;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Set<ExperimentCondition> getExperimentConditions() {
        return experimentConditions;
    }

    public void setExperimentConditions(Set<ExperimentCondition> experimentConditions) {
        this.experimentConditions = experimentConditions;
    }

    public Set<ExperimentCondition> getMorpholinoConditions() {
        if (experimentConditions == null)
            return null;

        Set<ExperimentCondition> morpholinoConditions = new HashSet<ExperimentCondition>();
        for (ExperimentCondition cond : experimentConditions) {
            if(cond.getMorpholino() != null)
                 morpholinoConditions.add(cond);
        }
        return morpholinoConditions;
    }

    public boolean isStandard() {
        return (name.equalsIgnoreCase(Experiment.STANDARD) || name.equalsIgnoreCase(Experiment.GENERIC_CONTROL));
    }
}
