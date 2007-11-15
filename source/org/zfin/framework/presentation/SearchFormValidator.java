package org.zfin.framework.presentation;

import org.zfin.util.ErrorCollection;

/**
 * User: giles
 * Date: Aug 16, 2006
 * Time: 1:53:11 PM
 */

public interface SearchFormValidator {

    public boolean isValid();

    public ErrorCollection getErrors();
}
