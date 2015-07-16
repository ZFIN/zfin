package org.zfin.expression;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Domain object.
 */
@SuppressWarnings({"JpaAttributeMemberSignatureInspection", "JpaAttributeTypeInspection"})
public class Experiment implements Comparable<Experiment>, EntityZdbID {

    public static final String STANDARD = "_Standard";
    public static final String GENERIC_CONTROL = "_Generic-control";
    public static final List<String> STANDARD_CONDITIONS = new ArrayList<String>(2);

    static {
        STANDARD_CONDITIONS.add(STANDARD);
        STANDARD_CONDITIONS.add(GENERIC_CONTROL);
    }

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

    public boolean isStandard() {

        return (name.equalsIgnoreCase(Experiment.STANDARD) || name.equalsIgnoreCase(Experiment.GENERIC_CONTROL));
    }

    public boolean isOnlyStandard() {
        return (name.equalsIgnoreCase(Experiment.STANDARD));
    }

    public boolean isOnlyControl() {
        return (name.equalsIgnoreCase(Experiment.GENERIC_CONTROL));
    }

    public boolean isChemical() {
        if (experimentConditions == null || experimentConditions.isEmpty()) {
            return false;
        }

        boolean allChemical = true;
        for (ExperimentCondition expCdt : experimentConditions) {
            if (!expCdt.isChemicalCondition()) {
                allChemical = false;
                break;
            }
        }
        return allChemical;
    }

    public int compareTo(Experiment o) {
        if (this.isStandard() && !o.isStandard()) {
            return -1;
        } else if (!this.isStandard() && o.isStandard()) {
            return 1;
        } else {
            if (this.isChemical() && !o.isChemical()) {
                return -1;
            } else if (!this.isChemical() && o.isChemical()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String getAbbreviation() {
        return name;
    }

    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    @Override
    public String getEntityType() {
        return "Experiment";
    }

    @Override
    public String getEntityName() {
        return "Experiment";
    }

    // Todo: no way to know an experiment is related to STR any more?
    /*
    public boolean isSequenceTargetingReagent() {
        if (CollectionUtils.isEmpty(experimentConditions))
            return false;
        for (ExperimentCondition condition : experimentConditions) {
            if (condition.getSequenceTargetingReagent() != null) {
                return true;
            }
        }
        return false;
    }
    */
}
