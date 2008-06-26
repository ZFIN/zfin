package org.zfin.framework.presentation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BeanPropertyBindingResult;

public class SearchTextValidatorTest {

    private static SearchTextValidator validator = new SearchTextValidator();


    // Check that any alpha character is valid
    @Test
    public void singleAlpha() {
        String term = "a";
        AnatomySearchBean bean = new AnatomySearchBean();
        bean.setSearchTerm(term);

        assertTrue(validator.supports(bean.getClass()));

        // check for a match
        BindingResult result = new BeanPropertyBindingResult(bean, "search");
        Errors errors = new BindException(result);

        validator.validate(bean, errors);

        int numOfErrors = errors.getErrorCount();
        assertEquals(0, numOfErrors);

    }

    // Check that any alpha character is valid
    @Test
    public void alpha() {
        String term = "asbcdefghijklmnopqrstuvwxyzASBCDEFGHIJKLMNOPQRSTUVWXYZ";
        AnatomySearchBean bean = new AnatomySearchBean();
        bean.setSearchTerm(term);

        assertTrue(validator.supports(bean.getClass()));

        // check for a match
        BindingResult result = new BeanPropertyBindingResult(bean, "search");
        Errors errors = new BindException(result);

        validator.validate(bean, errors);

        int numOfErrors = errors.getErrorCount();
        assertEquals(0, numOfErrors);

    }

    // Check that a single numeral is valid
    @Test
    public void singleNumeral() {
        String term = "1";
        AnatomySearchBean bean = new AnatomySearchBean();
        bean.setSearchTerm(term);

        assertTrue(validator.supports(bean.getClass()));

        // check for a match
        BindingResult result = new BeanPropertyBindingResult(bean, "search");
        Errors errors = new BindException(result);

        validator.validate(bean, errors);

        int numOfErrors = errors.getErrorCount();
        assertEquals(0, numOfErrors);

    }

    // Check that a single numeral is valid
    @Test
    public void multipleNumerals() {
        String term = "1234567890";
        AnatomySearchBean bean = new AnatomySearchBean();
        bean.setSearchTerm(term);

        assertTrue(validator.supports(bean.getClass()));

        // check for a match
        BindingResult result = new BeanPropertyBindingResult(bean, "search");
        Errors errors = new BindException(result);

        validator.validate(bean, errors);

        int numOfErrors = errors.getErrorCount();
        assertEquals(0, numOfErrors);

    }

    // Check that a quote is valid, e.g. in Brachet's cleft
    @Test
    public void quote() {
        String term = "Brachet's";
        AnatomySearchBean bean = new AnatomySearchBean();
        bean.setSearchTerm(term);

        assertTrue(validator.supports(bean.getClass()));

        // check for a match
        BindingResult result = new BeanPropertyBindingResult(bean, "search");
        Errors errors = new BindException(result);

        validator.validate(bean, errors);

        int numOfErrors = errors.getErrorCount();
        assertEquals(0, numOfErrors);

    }

    // Check that a quote is valid, e.g. in Brachet's cleft
    @Test
    public void whiteSpace() {
        String term = "Brachet's Cleft";
        AnatomySearchBean bean = new AnatomySearchBean();
        bean.setSearchTerm(term);

        assertTrue(validator.supports(bean.getClass()));

        // check for a match
        BindingResult result = new BeanPropertyBindingResult(bean, "search");
        Errors errors = new BindException(result);

        validator.validate(bean, errors);

        int numOfErrors = errors.getErrorCount();
        assertEquals(0, numOfErrors);

    }
}