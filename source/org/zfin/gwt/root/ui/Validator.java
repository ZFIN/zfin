package org.zfin.gwt.root.ui;

/**
 */
public interface Validator {

    /**
     * Validate thing.  If false, set error on handlesError.
     *
     * @param stringToValidate String to validate.
     * @param handlesError     Thing to set error on.
     * @return Is true if passes validation and false if fails.
     */
    boolean validate(String stringToValidate, HandlesError handlesError);
}
