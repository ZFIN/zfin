package org.zfin.feature.presentation;

import java.util.Set;
import java.util.TreeSet;

/**
 */
public class LabFeaturePrefixRow {

    private String prefix ;
    private Set<String> locations = new TreeSet<String>();
    private Set<LabEntry> labs = new TreeSet<LabEntry>();


    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Set<String> getLocations() {
        return locations;
    }

    public void addLocation(String location) {
        locations.add(location) ;
    }

    public void addLab(LabEntry lab){
        this.labs.add(lab) ;
    }

    public Set<LabEntry> getLabs() {
        return labs;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LabFeaturePrefixRow");
        sb.append("{prefix='").append(prefix).append('\'');
        sb.append(", location='").append(locations).append('\'');
        sb.append(", labs=").append(labs);
        sb.append('}');
        return sb.toString();
    }

}
