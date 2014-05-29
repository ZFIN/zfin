package org.zfin.framework.presentation;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for PaginationBean
 */
public class PaginationBeanTest {

    /**
     * Query string has no page= parameter.
     */
    @Test
    public void actionUrlNoPageParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("pageSize=25&query=nkx2.2&category=GENES");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/query/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/query/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

    /**
     * Query string has page= parameter as the first parameter.
     * Should be removed from the returning URL.
     */
    @Test
    public void actionUrlPageParamFirstParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("page=2&pageSize=25&query=nkx2.2&category=GENES");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/query/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/query/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

    @Test
    public void actionUrlPageParamLastParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("pageSize=25&query=nkx2.2&category=GENES&page=24436");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/query/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/query/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

    @Test
    public void actionUrlPageParamMiddleParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("pageSize=25&page=24436&query=nkx2.2&category=GENES");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/query/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/query/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }


    @Test
    public void testValidation() {

        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords("25");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<PaginationBean>> violations = validator.validate(bean);

        assertNotNull(violations);
        assertEquals("No violations", 0, violations.size());

        bean.setMaxDisplayRecords("10000000");
        violations = validator.validate(bean);
        assertEquals("One violations", 1, violations.size());
        assertEquals("maxDisplayRecords must be less or equal 1000", violations.iterator().next().getMessage());

        bean = new PaginationBean();
        bean.setMaxDisplayRecords("25anad");
        violations = validator.validate(bean);
        assertEquals("One violations", 1, violations.size());
        ConstraintViolation<PaginationBean> viols = violations.iterator().next();
        assertEquals("The value {0} of the request parameter 'maxDisplayRecordsString' is not a number", viols.getMessage());
    }

}