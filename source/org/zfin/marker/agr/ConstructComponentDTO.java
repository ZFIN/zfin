package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstructComponentDTO {

    private String componentRelation;
    protected String componentSymbol;
    protected String componentId;

    public String getComponentRelation() {
        return componentRelation;
    }

    public void setComponentRelation(String componentRelation) {
        this.componentRelation = componentRelation;
    }

    public String getComponentSymbol() {
        return componentSymbol;
    }

    public void setComponentSymbol(String componentSymbol) {
        this.componentSymbol = componentSymbol;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
}


