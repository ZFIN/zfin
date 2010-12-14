package org.zfin.feature.presentation;

import org.zfin.people.Lab;

/**
 * A wrapper around a lab that gets displayed in line designation.
 */
public class LabEntry implements Comparable<LabEntry> {

    private Lab lab ;
    private boolean currentLineDesignation ;

    public LabEntry(Lab lab,boolean currentLineDesignation){
        this.lab = lab;
        this.currentLineDesignation = currentLineDesignation ;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public boolean isCurrentLineDesignation() {
        return currentLineDesignation;
    }

    public void setCurrentLineDesignation(boolean currentLineDesignation) {
        this.currentLineDesignation = currentLineDesignation;
    }

    @Override
    public int compareTo(LabEntry labEntry) {
        if(isCurrentLineDesignation()!=labEntry.isCurrentLineDesignation()){
            return (isCurrentLineDesignation() ? -1 : 1) ;
        }
        else{
            return getLab().getName().compareToIgnoreCase(labEntry.getLab().getName()) ;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) return false ;
        if(o instanceof LabEntry){
            LabEntry otherLab = (LabEntry) o ;
            return lab.equals(otherLab.getLab());
        }
        return false ;
    }
}
