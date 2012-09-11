package org.zfin.profile;

import java.sql.Blob;

/**
 * Company domain model.
 */
public class Company extends Organization {

    private String bio;
    private Blob snapshot;

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
        return false;
    }

    public boolean getCompany() {
        return true;
    }

    @Override
    public String getType() {
        return "company";
    }

   public Blob getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Blob snapshot) {
        this.snapshot = snapshot;
    }
}