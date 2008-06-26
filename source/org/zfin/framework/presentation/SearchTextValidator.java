package org.zfin.framework.presentation;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeanWrapper;

import java.util.regex.Pattern;
import java.util.Set;

/**
 * Validator that validates fields for search terms.
 * Check for special characters and wildcard type characters and disallow them.
 * <p/>
 * Allowed:
 * 1) alphanumeric characters
 * 2) a quote character
 * 3) a white space to allow for compound words
 *
 * This class can validate more than one field, although, the springframework allows only to
 * validate on a single root object passed into the validate method. Only classes that
 * implement {@link SearchTextInputFields} can be validate to keep this validator more reusable.
 * The getSearchFields() method should return a set of strings that define the attributes on the
 * target object.
 *
 * No validation is performed (silent as it does not throw an exception) if
 *  1) If no attributes are provided
 *  2) If no term value is found for a term
 */
public class SearchTextValidator implements Validator {

    /**
     * Validator only works for classes that implement
     * org.zfin.framwork.presentation.SearchTextInputFields
     *
     * @param aClass Class
     * @return boolean
     */
    public boolean supports(Class aClass) {
        return SearchTextInputFields.class.isAssignableFrom(aClass);
    }

    public void validate(Object targetBean, Errors errors) {
        SearchTextInputFields bean = (SearchTextInputFields) targetBean;
        Set<String> terms = bean.getSearchFields();
        if (terms == null)
            return;

        for (String termName : terms) {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            String termValue = (String) wrapper.getPropertyValue(termName);
            // if no term value exists do not validate
            if (termValue == null)
                return;

            boolean isMatch = Pattern.matches("[a-zA-Z' ]*\\d*", termValue);
            if (!isMatch)
                errors.rejectValue(termName,"code" ,"[" + termValue +"] is an invalid string. Only alphanumeric characters are allowed");
        }
    }
}
