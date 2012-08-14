package org.zfin.framework.presentation;

import java.util.Map;
import java.util.TreeMap;

/**
 * Pagination logic for apg search pages.
 */
public class ApgPaginationBean extends PaginationBean {

    /**
     * Returns the start record on each page
     *
     * @return collection
     */
    public Map<Integer, Integer> getFirstRecordOnPageList() {
        int totalPages = getTotalNumPages();
        Map<Integer, Integer> pageList = new TreeMap<Integer, Integer>();
        for (int i = MAXPAGELINKS / 2; i > 0; i--) {
            if (pageInteger - i > 0)
                pageList.put(pageInteger - i, getFirstRecordOnGivenPage(pageInteger - i));
        }

        pageList.put(pageInteger, getFirstRecordOnGivenPage(pageInteger));

        for (int i = 1; i <= MAXPAGELINKS / 2; i++) {
            if (pageInteger + i <= totalPages)
                pageList.put(pageInteger + i, getFirstRecordOnGivenPage(pageInteger + i));
        }

        return pageList;
    }

    public int getFirstRecordOnGivenPage(int page) {
        return (page - 1) * maxDisplayRecordsInteger + 1;
    }

    public int getFirstRecordOnPreviousPage() {
        return getFirstRecordOnGivenPage(getPreviousPage());
    }

    public int getFirstRecordOnNextPage() {
        return getFirstRecordOnGivenPage(getNextPage());
    }

    public int getFirstRecordOnFirstPage() {
        return getFirstRecordOnGivenPage(1);
    }

    public int getFirstRecordOnLastPage() {
        return getFirstRecordOnGivenPage(getTotalNumPages());
    }
}
