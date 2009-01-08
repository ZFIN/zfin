package org.zfin.repository;

import org.hibernate.ScrollableResults;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * This class build PaginationResults using Hibernate / JDBC Constructs
 */
public class PaginationResultFactory {

    /**
     * Return records from 0 to max records
     *
     * @param maxRecords        max number of records
     * @param scrollableResults Scrollable Result
     * @return Result of scrolling
     */
    @SuppressWarnings("unchecked")
    public static <T> PaginationResult<T> createResultFromScrollableResultAndClose(int maxRecords, ScrollableResults scrollableResults) {
        PaginationResult<T> returnResult = new PaginationResult<T>();
        List<T> list = new ArrayList<T>();
        while (scrollableResults.next() && scrollableResults.getRowNumber() < maxRecords) {
            list.add((T) scrollableResults.get(0));
        }

        scrollableResults.last();
        returnResult.setTotalCount(scrollableResults.getRowNumber() + 1);
        returnResult.setPopulatedResults(list);
        scrollableResults.close();
        return returnResult;
    }

    /**
     * stopRecord = 0 is interpreted to retrieve all records.
     *
     * @param startRecord       This is inclusive.
     * @param stopRecord        This is exclusive.
     * @param scrollableResults Scrollable Object
     * @return pagination result
     */
    @SuppressWarnings("unchecked")
    public static <T> PaginationResult<T> createResultFromScrollableResultAndClose(int startRecord, int stopRecord, ScrollableResults scrollableResults) {
        PaginationResult<T> returnResult = new PaginationResult<T>();
        List<T> list = new ArrayList<T>();
        if (startRecord == 0) {
            scrollableResults.beforeFirst();
        } else {
            scrollableResults.setRowNumber(startRecord - 1);
        }
        // if stopRecord = 0 then retrieve all records.
        if (stopRecord > 0){
            while (scrollableResults.next() && scrollableResults.getRowNumber() < stopRecord) {
                list.add((T) scrollableResults.get(0));
            }
        }
        else{
            while (scrollableResults.next()) {
                list.add((T) scrollableResults.get(0));
            }
        }
        if (scrollableResults.last())
            returnResult.setTotalCount(scrollableResults.getRowNumber() + 1);
        else
            returnResult.setTotalCount(0);
        returnResult.setPopulatedResults(list);
        scrollableResults.close();
        return returnResult;
    }

    /**
     * If no pagination bean object is provided a new one is created
     * with first record = 0 and
     * last record = 0 meaning all records will be retrieved.
     *
     * @param bean              PaginationBean
     * @param scrollableResults Scrollable Object
     * @return pagination result
     */
    public static <T> PaginationResult<T> createResultFromScrollableResultAndClose(PaginationBean bean, ScrollableResults scrollableResults) {
        if (bean == null) {
            bean = new PaginationBean();
        }
        return createResultFromScrollableResultAndClose(bean.getFirstRecord() - 1, bean.getLastRecord(), scrollableResults);
    }
}
