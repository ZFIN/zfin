package org.zfin.people;

import java.util.Set;

/**
 * Company domain model.
 */
public class Company extends Organization {

    private String bio;

    public int compareTo(Organization org) {
        if (org == null)
            return -1;
        return name.compareTo(org.getName());
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean getLab() {
        return false ;
    }

    public boolean getCompany() {
        return true ;
    }
}