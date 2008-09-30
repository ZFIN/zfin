package org.zfin.antibody;

import org.zfin.expression.ExpressionExperiment;
import org.zfin.marker.Marker;

import java.util.Set;

/**
 * Main domain object for antibodies.
 */
public class Antibody extends Marker {

    private String hostSpecies;
    private String immunogenSpecies;
    private String heavyChainIsotype;
    private String lightChainIsotype;
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
}
