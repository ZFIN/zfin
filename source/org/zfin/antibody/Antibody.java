package org.zfin.antibody;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.SortNatural;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main domain object for antibodies.
 */
@Entity
@Table(name = "antibody")
@PrimaryKeyJoinColumn(name = "atb_zdb_id")
@Setter
@Getter
public class Antibody extends Marker {

    @Column(name = "atb_host_organism")
    @JsonView(View.AntibodyDetailsAPI.class)
    private String hostSpecies;

    @Column(name = "atb_immun_organism")
    @JsonView(View.AntibodyDetailsAPI.class)
    private String immunogenSpecies;

    @Column(name = "atb_hviso_name")
    @JsonView(View.AntibodyDetailsAPI.class)
    private String heavyChainIsotype;

    @Column(name = "atb_ltiso_name")
    @JsonView(View.AntibodyDetailsAPI.class)
    private String lightChainIsotype;

    @Column(name = "atb_type")
    @JsonView(View.AntibodyDetailsAPI.class)
    private String clonalType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatex_atb_zdb_id")
    private Set<ExpressionExperiment2> antibodyLabelings;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "extnote_data_zdb_id")
    @SortNatural
    private Set<AntibodyExternalNote> externalNotes;

    @Transient
    @JsonView(View.AntibodyDetailsAPI.class)
    private List<Marker> antigenGenes;

    @JsonView(View.AntibodyDetailsAPI.class)
    @Transient
    private String abregistryIDs;

    /**
     * @param expressionExperiment Antibody label to compare.
     * @return Returns an antibody that prevents merging.
     */
    public ExpressionExperiment2 getMatchingAntibodyLabeling(ExpressionExperiment2 expressionExperiment) {
        for (ExpressionExperiment2 anExpressionExperiment : getAntibodyLabelings()) {
            if (!canMergeAntibodyLabel(anExpressionExperiment, expressionExperiment)) {
                return anExpressionExperiment;
            }
        }
        return null;
    }

    /**
     * If any of the following is true, can merge:
     * publication, genotypeexperiment, assay are different
     * probe, gene, markerDBLink are different or one or more is null
     * <p>
     * // we don't consider antibody
     *
     * @param eea First expression experiment.
     * @param eeb Second expression experiment.
     * @return Whether or not an antibody label can be merged.
     */
    private boolean canMergeAntibodyLabel(ExpressionExperiment2 eea, ExpressionExperiment2 eeb) {
        if (!eea.getPublication().equals(eeb.getPublication())) return true;
        if (!eea.getFishExperiment().equals(eeb.getFishExperiment())) return true;
        if (!eea.getAssay().equals(eeb.getAssay())) return true;

        if (eea.getProbe() == null && eeb.getProbe() != null) return true;
        if (eea.getProbe() != null && eeb.getProbe() == null) return true;
        if (eea.getProbe() != null && eeb.getProbe() != null &&
            !eea.getProbe().equals(eeb.getProbe())) return true;

        if (eea.getGene() == null && eeb.getGene() != null) return true;
        if (eea.getGene() != null && eeb.getGene() == null) return true;
        if (eea.getGene() != null && eeb.getGene() != null &&
            !eea.getGene().equals(eeb.getGene())) return true;

        if (eea.getMarkerDBLink() == null && eeb.getMarkerDBLink() != null) return true;
        if (eea.getMarkerDBLink() != null && eeb.getMarkerDBLink() == null) return true;
        if (eea.getMarkerDBLink() != null && eeb.getMarkerDBLink() != null
            && !eea.getMarkerDBLink().equals(eeb.getMarkerDBLink())) return true;

        return false;
    }

    @Override
    public String getEntityName() {
        return getName();
    }

    @JsonView(View.AntibodyDetailsAPI.class)
    public List<String> getDistinctAssayNames() {
        if (antibodyLabelings == null) {
            return new ArrayList<>();
        }

        return antibodyLabelings.stream()
            .filter(experiment -> CollectionUtils.isNotEmpty(experiment.getFigureStageSet()))
            .map(experiment -> experiment.getAssay().getAbbreviation())
            .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());

    }
}
