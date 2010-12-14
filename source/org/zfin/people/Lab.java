package org.zfin.people;

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

    public boolean getLab() {
        return true;
    }

    public boolean getCompany() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Lab");
        sb.append("{bio='").append(bio).append('\'');
        sb.append(super.toString()) ;
        sb.append('}');
        return sb.toString();
    }
}
