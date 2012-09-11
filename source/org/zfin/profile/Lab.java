package org.zfin.profile;

/**
 * Lab domain model.
 */
public class Lab extends Organization {


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
    public String getType() {
        return "lab";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Lab{");
        sb.append(super.toString()) ;
        sb.append('}');
        return sb.toString();
    }

}
