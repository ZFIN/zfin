package org.zfin.util;

import java.util.ArrayList;

/**
 * User: giles
 * Date: Aug 17, 2006
 * Time: 12:18:56 PM
 */


public class ErrorCollection {
    private ArrayList<String> errors = new ArrayList<String>();

    public void addError(String error) {
        errors.add(error);
    }

    public void removeError(String error) {
        errors.remove(error);
    }

    public void clearErrors() {
        errors.clear();
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

}
