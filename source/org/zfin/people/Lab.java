package org.zfin.people;

import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Lab domain model.
 */
public class Lab extends Organization {

    private String bio;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int compareTo(Organization org) {
        if (org == null)
            return -1;
        return name.compareTo(org.getName());
    }
}
