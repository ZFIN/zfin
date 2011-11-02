package org.zfin.feature.presentation;

import org.zfin.people.Lab;
import org.zfin.people.Organization;

/**
 * A wrapper around a lab that gets displayed in line designation.
 */
public class LabEntry implements Comparable<LabEntry> {

    private Organization organization ;
    private boolean currentLineDesignation ;

    public LabEntry(Organization organization,boolean currentLineDesignation){
        this.organization = organization;
        this.currentLineDesignation = currentLineDesignation ;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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
            return getOrganization().getName().compareToIgnoreCase(labEntry.getOrganization().getName()) ;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) return false ;
        if(o instanceof LabEntry){
            LabEntry otherLab = (LabEntry) o ;
            return organization.equals(otherLab.getOrganization());
        }
        return false ;
    }
}
