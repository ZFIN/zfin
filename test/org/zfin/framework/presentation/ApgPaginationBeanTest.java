package org.zfin.framework.presentation;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static junit.framework.Assert.assertEquals;

public class ApgPaginationBeanTest {

    @Test
    public void test157Pages() {
        PaginationBean pagination = new PaginationBean();
        int maxDisplayRecords = 10;
        int totalRecords = 157;
        int firstPageRecord = 1;
        pagination.setTotalRecords(totalRecords);
        pagination.setMaxDisplayRecords(maxDisplayRecords);
        pagination.setFirstPageRecord(firstPageRecord);
        assertEquals(5, pagination.getPageList().size());
    }

    @Test
    public void test157PagesApg() {
        ApgPaginationBean pagination = new ApgPaginationBean();
        int maxDisplayRecords = 10;
        int totalRecords = 157;
        int firstPageRecord = 1;
        pagination.setTotalRecords(totalRecords);
        pagination.setMaxDisplayRecords(maxDisplayRecords);
        pagination.setFirstPageRecord(firstPageRecord);
        assertEquals(5, pagination.getFirstRecordOnPageList().size());
    }
}
