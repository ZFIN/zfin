package org.zfin.antibody;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.ExpressionExperiment;
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

    public String getHostSpecies() {
        return hostSpecies;
    }

    public void setHostSpecies(String hostSpecies) {
        this.hostSpecies = hostSpecies;
    }

    public String getImmunogenSpecies() {
        return immunogenSpecies;
    }

    public void setImmunogenSpecies(String immunogenSpecies) {
        this.immunogenSpecies = immunogenSpecies;
    }

    public Set<ExpressionExperiment> getAntibodyLabelings() {
        return antibodyLabelings;
    }

    public void setAntibodyLabelings(Set<ExpressionExperiment> antibodyLabelings) {
        this.antibodyLabelings = antibodyLabelings;
    }

    public Set<AntibodyExternalNote> getExternalNotes() {
        return externalNotes;
    }

    public void setExternalNotes(Set<AntibodyExternalNote> externalNotes) {
        this.externalNotes = externalNotes;
    }

    public String getClonalType() {
        return clonalType;
    }

    public void setClonalType(String clonalType) {
        this.clonalType = clonalType;
    }

    public String getHeavyChainIsotype() {
        return heavyChainIsotype;
    }

    public void setHeavyChainIsotype(String heavyChainIsotype) {
        this.heavyChainIsotype = heavyChainIsotype;
    }

    public String getLightChainIsotype() {
        return lightChainIsotype;
    }

    public void setLightChainIsotype(String lightChainIsotype) {
        this.lightChainIsotype = lightChainIsotype;
    }

    /**
     * @param expressionExperiment Antibody label to compare.
     * @return Returns an antibody that prevents merging.
     */
    public ExpressionExperiment getMatchingAntibodyLabeling(ExpressionExperiment expressionExperiment) {
        for (ExpressionExperiment anExpressionExperiment : getAntibodyLabelings()) {
            if (false == canMergeAntibodyLabel(anExpressionExperiment, expressionExperiment)) {
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
        if (!eea.getPublication().equals(eeb.getPublication())) return true;
        if (!eea.getFishExperiment().equals(eeb.getFishExperiment())) return true;
        if (!eea.getAssay().equals(eeb.getAssay())) return true;

        if (eea.getProbe() == null && eeb.getProbe() != null) return true;
        if (eea.getProbe() != null && eeb.getProbe() == null) return true;
        if (eea.getProbe() != null && eeb.getProbe() != null &&
                false == eea.getProbe().equals(eeb.getProbe())) return true;

        if (eea.getGene() == null && eeb.getGene() != null) return true;
        if (eea.getGene() != null && eeb.getGene() == null) return true;
        if (eea.getGene() != null && eeb.getGene() != null &&
                false == eea.getGene().equals(eeb.getGene())) return true;

        if (eea.getMarkerDBLink() == null && eeb.getMarkerDBLink() != null) return true;
        if (eea.getMarkerDBLink() != null && eeb.getMarkerDBLink() == null) return true;
        if (eea.getMarkerDBLink() != null && eeb.getMarkerDBLink() != null
                && false == eea.getMarkerDBLink().equals(eeb.getMarkerDBLink())) return true;

        // we don't handle antibody

        return false;
    }

    @Override
    public String getEntityName() {
        return getName();
    }

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
