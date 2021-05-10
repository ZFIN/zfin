package org.zfin.gwt.root.dto;


import java.util.List;


/**
 * Transcript RPC object.

 */

public class ConstructDTO extends RelatedEntityDTO {
    protected String constructType;

    public String getConstructType() {
        return constructType;
    }

    public void setConstructType(String constructType) {
        this.constructType = constructType;
    }
}