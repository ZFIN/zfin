package org.zfin.gwt.root.ui;

/**
 */
public class NameValidator implements Validator {

    private final int MIN_LENGTH = 3;


    @Override
    public boolean validate(String markerName, HandlesError handlesError) {

        if (markerName == null || markerName.trim().length() < MIN_LENGTH) {
            handlesError.setError("Name must be at least " + MIN_LENGTH + " characters long.");
            return false;
        }

        return true;
    }
}
