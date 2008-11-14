package org.zfin.util;

import java.beans.PropertyEditorSupport;

/**
 * Custom Property Editor used to have Spring automatically instantiate
 * obejcts of type FilterType.
 */
public class FilterTypeEditor  extends PropertyEditorSupport {

    /**
     * Logic to obtain a FilterType object from a string.
     * @param textValue string representation of a FilterType
     */
    public void setAsText(String textValue){
        FilterType type = FilterType.getFilterType(textValue);
        setValue(type);
    }

    public String getAsText() {
        return ((FilterType)getValue()).getName();
    }

}
