package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResultTermComparator;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.feature.Feature;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.gbrowse.presentation.GBrowseImageSimilarComparator;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.MarkerSupplier;
import org.zfin.publication.Publication;
import org.zfin.sequence.STRMarkerSequence;
import org.zfin.sequence.blast.Database;

import java.util.*;

/**
 */
public class SequenceTargetingReagentBean extends MarkerBean{

    private Logger logger = Logger.getLogger(SequenceTargetingReagentBean.class);

    private Set<Marker> targetGenes ;
    private List<STRMarkerSequence> sequences;
    private List<MarkerSupplier> suppliers;
    private String ncbiBlastUrl;
    private String sequenceAttribution;
    private List<Database> databases;
    private List<Feature> genomicFeatures;
    private List<PhenotypeDisplay> phenotypeDisplays;
    private Set<GBrowseImage> gBrowseImages;
    private List<ExpressionDisplay> expressionDisplays;
    private List<PhenotypeDisplay> allPhenotypeDisplays;

    private boolean phenoMartBeingRegened;

    public Set<Marker> getTargetGenes() {
        return targetGenes;
    }

    public void setTargetGenes(Set<Marker> targetGenes) {
        this.targetGenes = targetGenes;
    }
    /**
     * Most of the time there will only be a single sequence.
     * @return
     */
    public STRMarkerSequence getSequence(){
        if(sequences!=null && sequences.size()>0){
            if(sequences.size()>1){
                logger.error("more than 1 sequence for marker: " + marker);
            }
            return sequences.get(0);
        }
        else{
            return null ;
        }
    }

    public String getNcbiBlastUrl() {
        return ncbiBlastUrl;
    }

    public void setNcbiBlastUrl(String ncbiBlastUrl) {
        this.ncbiBlastUrl = ncbiBlastUrl;
    }

    public List<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    public String getSequenceAttribution() {
        return sequenceAttribution;
    }

    public void setSequenceAttribution(String sequenceAttribution) {
        this.sequenceAttribution = sequenceAttribution;
    }


    public boolean isTALEN() {
        if (this.marker.getType().isMarkerType("TALEN")) {
            return true;
        }

        return false;
    }

    public boolean isCRISPR() {
        if (this.marker.getType().isMarkerType("CRISPR")) {
            return true;
        }

        return false;
    }

    public List<MarkerSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<MarkerSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public List<Feature> getGenomicFeatures() {
        return genomicFeatures;
    }

    public void setGenomicFeatures(List<Feature> genomicFeatures) {
        this.genomicFeatures = genomicFeatures;
    }

    public List<PhenotypeDisplay> getPhenotypeDisplays() {
        return phenotypeDisplays;
    }

    public void setPhenotypeDisplays(List<PhenotypeDisplay> phenotypeDisplays) {
        this.phenotypeDisplays = phenotypeDisplays;
    }

    public List<ExpressionDisplay> getExpressionDisplays() {
        return expressionDisplays;
    }

    public void setExpressionDisplays(List<ExpressionDisplay> expressionDisplays) {
        this.expressionDisplays = expressionDisplays;
    }

    // the b in browse is lowercase in the method name to make JSP happy for some reason
    public Set<GBrowseImage> getGbrowseImages() {
        return gBrowseImages;
    }

    public void addGBrowseImage(GBrowseImage image) {
        if (gBrowseImages == null) {
            gBrowseImages = new TreeSet<>(new GBrowseImageSimilarComparator());
        }
        gBrowseImages.add(image);
    }

    public List<PhenotypeDisplay> getAllPhenotypeDisplays() {
        return allPhenotypeDisplays;
    }

    public void setAllPhenotypeDisplays(List<PhenotypeDisplay> allPhenotypeDisplays) {
        this.allPhenotypeDisplays = allPhenotypeDisplays;
    }

    public boolean isPhenoMartBeingRegened() {
        return phenoMartBeingRegened;
    }

    public void setPhenoMartBeingRegened(boolean phenoMartBeingRegened) {
        this.phenoMartBeingRegened = phenoMartBeingRegened;
    }
}

