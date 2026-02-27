package org.zfin.repository;

import jakarta.servlet.http.HttpServletRequest;
import org.zfin.framework.presentation.PaginationBean;

import java.util.Collections;
import java.util.List;

/**
 * Utility for in-memory pagination using PaginationBean (JSP/form-bean pagination).
 * <p>
 * Counterpart to the Pagination-based methods in {@link PaginationResultFactory},
 * this class works with {@link PaginationBean} for controllers that render JSP pages
 * with the pagination.tag.
 */
public class PaginationBeanResultFactory {

    /**
     * Paginate a list in memory: configures the PaginationBean with total records,
     * request URL, and query string, then returns the sublist for the current page.
     *
     * @param list       the full list to paginate
     * @param pagination the PaginationBean to configure (must already have maxDisplayRecords set)
     * @param request    the current HTTP request (for URL and query string)
     * @return the sublist for the current page
     */
    public static <T> List<T> paginateList(List<T> list, PaginationBean pagination, HttpServletRequest request) {
        pagination.setTotalRecords(list.size());
        pagination.setRequestUrl(request.getRequestURL());
        pagination.setQueryString(request.getQueryString());
        return subList(list, pagination);
    }

    /**
     * Return the sublist for the current page based on the PaginationBean's page and maxDisplayRecords.
     */
    private static <T> List<T> subList(List<T> list, PaginationBean pagination) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int start = Math.max(0, pagination.getFirstRecord() - 1);
        int end = Math.min(start + pagination.getMaxDisplayRecordsInteger(), list.size());
        if (start >= list.size()) {
            return Collections.emptyList();
        }
        return list.subList(start, end);
    }

}
