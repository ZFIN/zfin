package org.zfin.mutant.presentation;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.Marker;
import org.zfin.profile.Organization;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Zfin Entity that only exists in the warehouse context at this time.
 * It does only have minimal data that is needed for the search result page,
 * and thus does not represent a full Zfin entity.
 */
public class Construct extends ZfinEntity {
    private long constructpkid;
    private int expressionFigureCount;
    private List<Organization> ctSupplier;

    public List<Organization> getCtSupplier() {
        return ctSupplier;
    }

    public void setCtSupplier(List<Organization> ctSupplier) {
        this.ctSupplier = ctSupplier;
    }

    public long getConstructpkid() {
        return constructpkid;
    }

    public void setConstructpkid(long constructpkid) {
        this.constructpkid = constructpkid;
    }
                                                                       private List<Feature> alleles= new ArrayList<Feature>();
    private Set<Marker> ftrGenes= new LinkedHashSet<Marker>();


    public Set<Marker> getFtrGenes() {
        return ftrGenes;
    }

    public void setFtrGenes(Set<Marker> ftrGenes) {
        this.ftrGenes = ftrGenes;
    }

    private boolean imageAvailable;
    private Set<ZfinFigureEntity> expressionFigures;

    public List<Feature> getAlleles() {
        return alleles;
    }

    public void setAlleles(List<Feature> alleles) {
        this.alleles = alleles;
    }

    private List<String> genotypeExperimentIDs;

    public List<String> getGenotypeExperimentIDs() {
        return genotypeExperimentIDs;
    }

    public void setGenotypeExperimentIDs(List<String> genotypeExperimentIDs) {
        this.genotypeExperimentIDs = genotypeExperimentIDs;
    }

    public String getGenotypeExperimentIDsString() {
        return genotypeExperimentIDsString;
    }

    public void setGenotypeExperimentIDsString(String genotypeExperimentIDsString) {
        this.genotypeExperimentIDsString = genotypeExperimentIDsString;
    }

    private String genotypeExperimentIDsString;

    private List<ZfinEntity> features;

    public List<ZfinEntity> getFeatures() {
        return features;
    }

    public void setFeatures(List<ZfinEntity> features) {
        this.features = features;
    }

    public List<ZfinEntity> getGenes() {
        return genes;
    }

    public void setGenes(List<ZfinEntity> genes) {
        this.genes = genes;
    }

    public List<ZfinEntity> getAffectedGenes() {
        return affectedGenes;
    }

    public void setAffectedGenes(List<ZfinEntity> affectedGenes) {
        this.affectedGenes = affectedGenes;
    }

    private List<ZfinEntity> genes;
    private List<ZfinEntity> affectedGenes;

    public int getExpressionFigureCount() {
        return expressionFigureCount;
    }

    public void setExpressionFigureCount(int expressionFigureCount) {
        this.expressionFigureCount = expressionFigureCount;
    }

    public boolean isImageAvailable() {
        return imageAvailable;
    }

    public void setImageAvailable(boolean imageAvailable) {
        this.imageAvailable = imageAvailable;
    }

    public Set<ZfinFigureEntity> getExpressionFigures() {
        return expressionFigures;
    }


    public ZfinFigureEntity getSingleFigure() {
        if (expressionFigures == null || expressionFigures.size() > 1)
            throw new RuntimeException("Did not find exactly one figure for fish " + getID());
        return expressionFigures.iterator().next();
    }

    public void setExpressionFigures(Set<ZfinFigureEntity> expressionFigures) {
        this.expressionFigures = expressionFigures;
        expressionFigureCount = expressionFigures.size();
    }
}