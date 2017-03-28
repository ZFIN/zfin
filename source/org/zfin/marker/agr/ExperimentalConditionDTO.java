package org.zfin.marker.agr;

public class ExperimentalConditionDTO {

    private String textCondition;
    private String zecoId;

    public ExperimentalConditionDTO(String textCondition, String zecoId) {
        this.textCondition = textCondition;
        this.zecoId = zecoId;
    }

    public String getTextCondition() {
        return textCondition;
    }

    public String getZecoId() {
        return zecoId;
    }
}
