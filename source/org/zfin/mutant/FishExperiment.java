package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.framework.api.View;

import org.hibernate.annotations.Parameter;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "fish_experiment")
public class FishExperiment implements Comparable<FishExperiment> {

    @Id
    @GeneratedValue(generator = "zdbIdGeneratorForFishExperiment")
    @GenericGenerator(name = "zdbIdGeneratorForFishExperiment",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "GENOX"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "genox_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;

    @Column(name = "genox_is_standard", nullable = false)
    private boolean standard;

    @Column(name = "genox_is_std_or_generic_control", nullable = false)
    private boolean standardOrGenericControl;

    @ManyToOne
    @JoinColumn(name = "genox_exp_zdb_id")
    @JsonView(View.API.class)
    private Experiment experiment;

    @ManyToOne
    @JoinColumn(name = "genox_fish_zdb_id")
    @JsonView(View.API.class)
    private Fish fish;

    @OneToMany(mappedBy = "fishExperiment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PhenotypeExperiment> phenotypeExperiments;

    @OneToMany(mappedBy = "fishExperiment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ExpressionExperiment2> expressionExperiments;

    @OneToMany(mappedBy = "fishExperiment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<GeneGenotypeExperiment> geneGenotypeExperiments;

    @OneToMany(mappedBy = "fishExperiment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DiseaseAnnotationModel> diseaseAnnotationModels;

    @Override
    public int compareTo(FishExperiment o) {
        int fishCompare = fish.compareTo(o.getFish());
        if (fishCompare != 0) {
            return fishCompare;
        }
        return experiment.compareTo(o.getExperiment());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!getClass().isAssignableFrom(o.getClass()) || o.getClass().isAssignableFrom(getClass())) {
            return false;
        }
        FishExperiment that = (FishExperiment) o;
        return Objects.equals(this.getExperiment(), that.getExperiment()) &&
                Objects.equals(this.getFish(), that.getFish());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getExperiment(), this.getFish());
    }

    public boolean isAmelioratedOrExacerbated() {
        Long totalCount = getFish().getFishFunctionalAffectedGeneCount() + getFish().getFishPhenotypicConstructCount();

        if (totalCount >= 2)
            return true;
        if (totalCount == 1 && !isStandardOrGenericControl())
            return true;
        if (totalCount == 0 && getExperiment().getExperimentConditions().size() >= 2)
            return true;

        return false;
    }

    public boolean isTwoChangesInEnvironment() {
        return getExperiment().getExperimentConditions().size() == 2;
    }
}
