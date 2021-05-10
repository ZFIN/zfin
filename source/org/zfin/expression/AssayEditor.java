package org.zfin.expression;

import java.beans.PropertyEditorSupport;

/**
 * Custom Property Editor used to have Spring automatically instantiate
 * obejcts of type FilterType.
 */
public class AssayEditor extends PropertyEditorSupport {

    /**
     * Logic to obtain a Species object from a string binary name.
     *
     * @param textValue string representation of a Species
     */
    public void setAsText(String textValue) {
        Assay assay = Assay.getAsssay(textValue);
        setValue(assay);
    }

    public String getAsText() {
        return ((Assay) getValue()).getName();
    }

}