package org.zfin.mapping;

import org.zfin.feature.Feature;
import org.zfin.ontology.GenericTerm;

/**
 * Feature Location .
 */
public class FeatureLocation  {
    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    private String zdbID;
    private Feature feature;
    private Integer sfclStart;
    private Integer sfclEnd;
    private String sfclChromosome;
    private String sfclAssembly;
    private GenericTerm sfclEvidence;



    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }



    public Integer getSfclStart() {
        return sfclStart;
    }

    public void setSfclStart(Integer sfclStart) {
        this.sfclStart = sfclStart;
    }

    public Integer getSfclEnd() {
        return sfclEnd;
    }

    public void setSfclEnd(Integer sfclEnd) {
        this.sfclEnd = sfclEnd;
    }

    public String getSfclChromosome() {
        return sfclChromosome;
    }

    public void setSfclChromosome(String sfclChromosome) {
        this.sfclChromosome = sfclChromosome;
    }

    public String getSfclAssembly() {
        return sfclAssembly;
    }

    public void setSfclAssembly(String sfclAssembly) {
        this.sfclAssembly = sfclAssembly;
    }

    public GenericTerm getSfclEvidence() {
        return sfclEvidence;
    }

    public void setSfclEvidence(GenericTerm sfclEvidence) {
        this.sfclEvidence = sfclEvidence;
    }
}
