package org.zfin.mapping;

public class MeioticPanel extends Panel {

    private int numberOfMeioses;
    private String crossType;

    public String getCrossType() {
        return crossType;
    }

    public void setCrossType(String crossType) {
        this.crossType = crossType;
    }

    public int getNumberOfMeioses() {
        return numberOfMeioses;
    }

    public void setNumberOfMeioses(int numberOfMeioses) {
        this.numberOfMeioses = numberOfMeioses;
    }
}
