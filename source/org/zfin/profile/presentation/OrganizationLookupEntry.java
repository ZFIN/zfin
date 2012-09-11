package org.zfin.profile.presentation;

import java.io.Serializable;

/**
 */
public class OrganizationLookupEntry implements Serializable, Comparable<OrganizationLookupEntry> {

    private String id;
    private String name ;
    private String type ;

    public String getLabel(){
        return name + " ["+type+"]" ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
