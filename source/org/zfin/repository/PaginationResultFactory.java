package org.zfin.repository;

import org.hibernate.ScrollableResults;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.PhenotypeStatement;

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
     * Return a generic PaginationResult object from a scrollableResult object
     * in which the first component contains the interested generic object while all other
     * components are disregarded (for sorting purposes only).
     * Note: In order to sort a list of objects by an attribute of another object you
     * have to include that other object in the select statement. However, it is not
     * needed as a return object and thus can be discarded from the result.
     * @param startRecord       This is inclusive.
     * @param stopRecord        This is exclusive.
     * @param scrollableResults Scrollable Object
     * @return pagination result
     */
    @SuppressWarnings("unchecked")
    public static <T> PaginationResult<T> createResultFromScrollableAndArray(int startRecord, int stopRecord, ScrollableResults scrollableResults) {
        PaginationResult<T> returnResult = new PaginationResult<T>();
        List<T> list = new ArrayList<T>();
        if (startRecord == 0) {
            scrollableResults.beforeFirst();
        } else {
            scrollableResults.setRowNumber(startRecord - 1);
        }
        boolean foundAtLeastOneRecord = false;
        while (scrollableResults.next() && scrollableResults.getRowNumber() < stopRecord) {
            foundAtLeastOneRecord = true;
            if (scrollableResults.get().length == 1){
                Object objects = scrollableResults.get(0);
                list.add((T) objects);
            }else{
                Object[] objects = scrollableResults.get();
                list.add((T) objects[0]);
            }
        }
        scrollableResults.last();
        // first row is '0' in Hibernate.
        if (foundAtLeastOneRecord)
            returnResult.setTotalCount(scrollableResults.getRowNumber() + 1);
        else
            returnResult.setTotalCount(scrollableResults.getRowNumber());
        returnResult.setPopulatedResults(list);
        scrollableResults.close();
        return returnResult;
    }

    /**
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
        boolean foundAtLeastOneRecord = false;
        while (scrollableResults.next() && scrollableResults.getRowNumber() < stopRecord) {
            foundAtLeastOneRecord = true;
            if (scrollableResults.get().length == 1)
                list.add((T) scrollableResults.get(0));
            else
                list.add((T) scrollableResults.get());
        }
        scrollableResults.last();
        // first row is '0' in Hibernate.
        if (foundAtLeastOneRecord)
            returnResult.setTotalCount(scrollableResults.getRowNumber() + 1);
        else
            returnResult.setTotalCount(scrollableResults.getRowNumber());
        returnResult.setPopulatedResults(list);
        scrollableResults.close();
        return returnResult;
    }

    /**
     * @param bean              PaginationBean
     * @param scrollableResults Scrollable Object
     * @return pagination result
     */
    public static <T> PaginationResult<T> createResultFromScrollableResultAndClose(PaginationBean bean, ScrollableResults scrollableResults) {
        return createResultFromScrollableResultAndClose(bean.getFirstRecord() - 1, bean.getLastRecord(), scrollableResults);
    }

    /**
     * @param bean              PaginationBean
     * @param scrollableResults Scrollable Object
     * @return pagination result
     */
    public static <T> PaginationResult<T> createPaginationResultFromScrollableAndArray(PaginationBean bean, ScrollableResults scrollableResults) {
        if (bean == null) {
            return createAllRecordsFromScrollable(scrollableResults);
        }
        return createResultFromScrollableAndArray(bean.getFirstRecord() - 1, bean.getLastRecord(), scrollableResults);
    }

    private static <T> PaginationResult<T> createAllRecordsFromScrollable(ScrollableResults scrollableResults) {
        PaginationResult<T> returnResult = new PaginationResult<T>();
        List<T> list = new ArrayList<T>();
            scrollableResults.beforeFirst();
        boolean foundAtLeastOneRecord = false;
        while (scrollableResults.next()) {
            foundAtLeastOneRecord = true;
            if (scrollableResults.get().length == 1){
                Object objects = scrollableResults.get(0);
                list.add((T) objects);
            }else{
                Object[] objects = scrollableResults.get();
                list.add((T) objects[0]);
            }
        }
        scrollableResults.last();
        // first row is '0' in Hibernate.
        if (foundAtLeastOneRecord)
            returnResult.setTotalCount(scrollableResults.getRowNumber() + 1);
        else
            returnResult.setTotalCount(scrollableResults.getRowNumber());
        returnResult.setPopulatedResults(list);
        scrollableResults.close();
        return returnResult;
    }
}
