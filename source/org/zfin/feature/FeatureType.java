package org.zfin.feature;

/**
 * Should be of type FeatureTypeEnum
 * @deprecated Use FeatureTypeEnum instead.  Only keeping this around because used in hql to map significance.
 */
public class FeatureType {

    private int significance;
    public String dispName;
    private String name;

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

   

