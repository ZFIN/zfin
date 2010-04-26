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

    /**
     *
     * @param expressionExperiment Antibody label to compare.
     * @return Returns an antibody that prevents merging.
     */
    public ExpressionExperiment getMatchingAntibodyLabeling(ExpressionExperiment expressionExperiment) {
        for(ExpressionExperiment anExpressionExperiment : getAntibodyLabelings()){
            if(false==canMergeAntibodyLabel(anExpressionExperiment,expressionExperiment)){
                return anExpressionExperiment ;
            }
        }
        return null ;
    }

    /**
     * If any of the following is true, can merge:
     * publication, genotypeexperiment, assay are different
     * probe, gene, markerDBLink are different or one or more is null
     *
     * // we don't consider antibody
     *
     * @param eea First expression experiment.
     * @param eeb Second expression experiment.
     * @return Whether or not an antibody label can be merged.
     */
    private boolean canMergeAntibodyLabel(ExpressionExperiment eea,ExpressionExperiment eeb){
        if(!eea.getPublication().equals(eeb.getPublication())) return true ;
        if(!eea.getGenotypeExperiment().equals(eeb.getGenotypeExperiment())) return true ;
        if(!eea.getAssay().equals(eeb.getAssay())) return true ;

        if( eea.getProbe()==null && eeb.getProbe()!=null ) return true ;
        if( eea.getProbe()!=null && eeb.getProbe()==null ) return true ;
        if(eea.getProbe()!=null && eeb.getProbe()!=null &&
                false==eea.getProbe().equals(eeb.getProbe())) return true ;

        if(eea.getGene()==null && eeb.getGene()!=null) return true ;
        if(eea.getGene()!=null && eeb.getGene()==null) return true ;
        if(eea.getGene()!=null && eeb.getGene()!=null &&
                false==eea.getGene().equals(eeb.getGene())) return true ;

        if(eea.getMarkerDBLink()==null && eeb.getMarkerDBLink()!=null) return true ;
        if(eea.getMarkerDBLink()!=null && eeb.getMarkerDBLink()==null) return true ;
        if(eea.getMarkerDBLink()!=null && eeb.getMarkerDBLink()!=null
                && false==eea.getMarkerDBLink().equals(eeb.getMarkerDBLink())) return true ;

        // we don't handle antibody

        return false ;
    }
}
