package org.zfin.expression;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.publication.Publication;

import java.util.Set;

/**
 * Domain object.
 */
@SuppressWarnings({"JpaAttributeMemberSignatureInspection", "JpaAttributeTypeInspection"})
public class Experiment implements Comparable<Experiment>, EntityZdbID {

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

    public boolean isHeatShock() {
        if (experimentConditions == null || experimentConditions.isEmpty()) {
            return false;
        }

        boolean allHeatShock = true;
        for (ExperimentCondition expCdt : experimentConditions) {
            if (!expCdt.isHeatShock()) {
                allHeatShock = false;
                break;
            }
        }
        return allHeatShock;
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
                return this.getName().compareToIgnoreCase(o.getName());
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

    public String getConditionKey() {
        String groupingKey = "";
        for (ExperimentCondition condition : experimentConditions) {
            groupingKey += condition.getConditionDataType().getGroup() + ":" + condition.getConditionDataType().getName() + "&&";
        }
        if (CollectionUtils.isNotEmpty(experimentConditions))
            groupingKey = groupingKey.substring(0, groupingKey.length() - 2);
        return groupingKey;
    }

}
