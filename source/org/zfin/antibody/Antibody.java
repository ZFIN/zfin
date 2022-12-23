package org.zfin.antibody;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main domain object for antibodies.
 */
@Setter
@Getter
public class Antibody extends Marker {

    @JsonView(View.AntibodyDetailsAPI.class)
    private String hostSpecies;
    @JsonView(View.AntibodyDetailsAPI.class)
    private String immunogenSpecies;
    @JsonView(View.AntibodyDetailsAPI.class)
    private String heavyChainIsotype;
    @JsonView(View.AntibodyDetailsAPI.class)
    private String lightChainIsotype;
    @JsonView(View.AntibodyDetailsAPI.class)
    private String clonalType;
    private Set<ExpressionExperiment> antibodyLabelings;
    private Set<AntibodyExternalNote> externalNotes;
    @JsonView(View.AntibodyDetailsAPI.class)
    private List<Marker> antigenGenes;

    @JsonView(View.AntibodyDetailsAPI.class)
    @Transient
    private String abregistryID;

    /**
     * @param expressionExperiment Antibody label to compare.
     * @return Returns an antibody that prevents merging.
     */
    public ExpressionExperiment getMatchingAntibodyLabeling(ExpressionExperiment expressionExperiment) {
        for (ExpressionExperiment anExpressionExperiment : getAntibodyLabelings()) {
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
    private boolean canMergeAntibodyLabel(ExpressionExperiment eea, ExpressionExperiment eeb) {
        //if any of these conditions are true, return true, otherwise, false
        return List.of(
            !eea.getPublication().equals(eeb.getPublication()),
            !eea.getFishExperiment().equals(eeb.getFishExperiment()),
            !eea.getAssay().equals(eeb.getAssay()),

            eea.getProbe() == null && eeb.getProbe() != null,
            eea.getProbe() != null && eeb.getProbe() == null,
            eea.getProbe() != null && eeb.getProbe() != null && !eea.getProbe().equals(eeb.getProbe()),

            eea.getGene() == null && eeb.getGene() != null,
            eea.getGene() != null && eeb.getGene() == null,
            eea.getGene() != null && eeb.getGene() != null && !eea.getGene().equals(eeb.getGene()),

            eea.getMarkerDBLink() == null && eeb.getMarkerDBLink() != null,
            eea.getMarkerDBLink() != null && eeb.getMarkerDBLink() == null,
            eea.getMarkerDBLink() != null && eeb.getMarkerDBLink() != null && !eea.getMarkerDBLink().equals(eeb.getMarkerDBLink())
        ).contains(true);
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
                .filter(experiment -> CollectionUtils.isNotEmpty(experiment.getExpressionResults()))
                .map(experiment -> experiment.getAssay().getAbbreviation())
                .filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());

    }
}
