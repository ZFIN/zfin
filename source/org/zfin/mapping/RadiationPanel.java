package org.zfin.mapping;

public class RadiationPanel extends Panel {

    private String radiationDose;
    private String numberOfCellLines;

    public String getNumberOfCellLines() {
        return numberOfCellLines;
    }

    public void setNumberOfCellLines(String numberOfCellLines) {
        this.numberOfCellLines = numberOfCellLines;
    }

    public String getRadiationDose() {
        return radiationDose;
    }

    public void setRadiationDose(String radiationDose) {
        this.radiationDose = radiationDose;
    }
}
