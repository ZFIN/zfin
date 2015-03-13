package org.zfin.profile.presentation;

import org.zfin.framework.presentation.LookupEntry;

import java.io.Serializable;

/**
 */
public class OrganizationLookupEntry extends LookupEntry implements Serializable, Comparable<OrganizationLookupEntry> {

    private String type ;

    public String getLabel(){
        return name + " ["+type+"]" ;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(OrganizationLookupEntry org) {
        if (org == null)
            return -1;
        return name.compareTo(org.getName());
    }
}
