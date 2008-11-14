package org.zfin.antibody;

import java.beans.PropertyEditorSupport;

/**
 *
 */

public class AntibodyTypeEditor extends PropertyEditorSupport {
    /**
     * Logic to obtain a Species object from a string binary name.
     *
     * @param textValue string representation of a Species
     */
    public void setAsText(String textValue) {
        AntibodyType antibodyType = AntibodyType.getType(textValue);
        setValue(antibodyType);
    }

    public String getAsText() {
        return ((AntibodyType) getValue()).getName();
    }

}

