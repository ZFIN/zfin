package org.zfin.feature.presentation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class AllelesForPrefixBean {

    private List<FeatureLabEntry> featureLabEntries = null ;
    private Collection<LabEntry> labs = null ;
    private boolean hasNonCurrentLabs = false ;

    public List<FeatureLabEntry> getFeatureLabEntries() {
        return featureLabEntries;
    }

    public void setFeatureLabEntries(List<FeatureLabEntry> featureLabEntries) {
        this.featureLabEntries = featureLabEntries;
    }

    public Collection<LabEntry> getLabs() {
        return labs;
    }

    public void setLabs(Collection<LabEntry> labs) {
        this.labs = labs;
    }

    public boolean isHasNonCurrentLabs() {
        return hasNonCurrentLabs;
    }

    public void setHasNonCurrentLabs(boolean hasNonCurrentLabs) {
        this.hasNonCurrentLabs = hasNonCurrentLabs;
    }

    public Collection<LabEntry> getNonCurrentLabs(){
        Set<LabEntry> nonCurrentLabs = new TreeSet<LabEntry> ();
        for(LabEntry labEntry : labs){
            if(!labEntry.isCurrentLineDesignation()){
                nonCurrentLabs.add(labEntry) ;
            }
        }
        
        return nonCurrentLabs ;
    }
}
