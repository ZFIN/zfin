package org.zfin.mutant;

import org.zfin.marker.Marker;

import java.util.Set;


public class FeatureType {

    private String name;
    private int significance;
       private String dispName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }

    public String getDispName() {
        return dispName;
    }

    public void setDispName(String dispName) {
        this.dispName = dispName;
    }
}
