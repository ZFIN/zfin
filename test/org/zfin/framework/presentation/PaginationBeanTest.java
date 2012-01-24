package org.zfin.framework.presentation;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

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
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

    /**
     * Query string has page= parameter as the first parameter.
     * Should be removed from the returning URL.
     */
    @Test
    public void actionUrlPageParamFirstParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("page=2&pageSize=25&query=nkx2.2&category=GENES");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

    @Test
    public void actionUrlPageParamLastParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("pageSize=25&query=nkx2.2&category=GENES&page=24436");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

    @Test
    public void actionUrlPageParamMiddleParam(){
        PaginationBean bean = new PaginationBean();
        bean.setQueryString("pageSize=25&page=24436&query=nkx2.2&category=GENES");
        bean.setRequestUrl(new StringBuffer("http://zfin.org/action/quicksearch/search_category.jsp"));
        String actionUrl = bean.getActionUrl();
        assertEquals("http://zfin.org/action/quicksearch/search_category.jsp?pageSize=25&query=nkx2.2&category=GENES&", actionUrl);
    }

}