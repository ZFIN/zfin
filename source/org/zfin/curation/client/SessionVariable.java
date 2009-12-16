package org.zfin.curation.client;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public enum SessionVariable {

    STAGE_RANGE_IN_EXPRESSION_SINGLE("FX-Curation - Expression Section - Stage Selector", "single"),
    STAGE_RANGE_IN_EXPRESSION_MULTI("FX-Curation - Expression Section - Stage Selector", "multi");

    private String attributeName;
    private String attributeValue;

    private SessionVariable(String attributeName, String attributeValue) {
        this.attributeName = this.attributeName;
        this.attributeValue = this.attributeValue;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public static SessionVariable[] getSessionVariables() {
        return SessionVariable.values();
    }

}
