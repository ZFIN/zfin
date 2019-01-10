package org.zfin.expression;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.*;

@SuppressWarnings({"JpaAttributeMemberSignatureInspection", "JpaAttributeTypeInspection"})
@Entity
@Table(name = "experiment")
public class Experiment implements Comparable<Experiment>, EntityZdbID {

    public static final String STANDARD = "_Standard";
    public static final String GENERIC_CONTROL = "_Generic-control";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "EXP"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "exp_zdb_id")
    private String zdbID;
    @Column(name = "exp_name")
    private String name;
    @ManyToOne
    @JoinColumn(name = "exp_source_zdb_id")
    private Publication publication;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "experiment")
    /*@SortNatural
    private Set<ExperimentCondition> experimentConditions;*/
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

    public boolean isStandard() {
        return (name.equalsIgnoreCase(Experiment.STANDARD) || name.equalsIgnoreCase(Experiment.GENERIC_CONTROL));
    }

    public boolean isOnlyStandard() {
        return (name.equalsIgnoreCase(Experiment.STANDARD));
    }

    public boolean isOnlyControl() {
        return (name.equalsIgnoreCase(Experiment.GENERIC_CONTROL));
    }

    public Set<ExperimentCondition> getExperimentConditions() {
        return experimentConditions;
    }

    public void setExperimentConditions(SortedSet<ExperimentCondition> experimentConditions) {
        this.experimentConditions = experimentConditions;
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
            groupingKey += condition.getZecoTerm().getTermName() + "&&";
        }
        if (CollectionUtils.isNotEmpty(experimentConditions))
            groupingKey = groupingKey.substring(0, groupingKey.length() - 2);
        return groupingKey;
    }

    public void addExperimentCondition(ExperimentCondition condition) {
       /* if (experimentConditions == null)
            experimentConditions = new HashSet<>();
        experimentConditions.add(condition);*/
        if (experimentConditions == null)
            experimentConditions = new TreeSet<>();
        experimentConditions.add(condition);
    }

    public String getDisplayAllConditions() {
        String displayConditions = "";
        Iterator iterator = experimentConditions.iterator();
        if (iterator.hasNext()) {
            ExperimentCondition firstExperimentCondition = (ExperimentCondition) iterator.next();
            displayConditions = firstExperimentCondition.getDisplayName();
            while (iterator.hasNext()) {
                displayConditions += ", ";
                ExperimentCondition experimentCondition = (ExperimentCondition) iterator.next();
                displayConditions += experimentCondition.getDisplayName();
            }
        }
        return displayConditions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experiment that = (Experiment) o;
        return new HashSet<>(experimentConditions).equals(new HashSet<>(that.experimentConditions));
    }

    @Override
    public int hashCode() {
        if (experimentConditions == null)
            return 11;
        return experimentConditions.stream().mapToInt(ExperimentCondition::hashCode).sum();
    }
}
