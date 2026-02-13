package org.zfin.repository;

import org.hibernate.ScrollableResults;
import org.zfin.framework.api.Pagination;
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
        int rowCount = 0;
        while (scrollableResults.next()) {
            if (rowCount < maxRecords) {
                list.add((T) scrollableResults.get());
            }
            rowCount++;
        }

        returnResult.setTotalCount(rowCount);
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
        PaginationResult<T> returnResult = new PaginationResult<>();
        List<T> list = new ArrayList<>();
        int rowCount = 0;
        int numberOfDuplicates = 0;
        while (scrollableResults.next()) {
            if (rowCount >= startRecord && rowCount < stopRecord) {
                T item = (T) scrollableResults.get();
                if (!list.contains(item)) {
                    list.add(item);
                } else {
                    numberOfDuplicates++;
                }
            }
            rowCount++;
        }

        returnResult.setTotalCount(rowCount - numberOfDuplicates);
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

    public static <T> PaginationResult<T> createResultFromScrollableResultAndClose(Pagination pagination, ScrollableResults scrollableResults) {
        return createResultFromScrollableResultAndClose(pagination.getStart(), pagination.getEnd(), scrollableResults);
    }

    /**
     * Return a paginated subset of the list provided based on the pagination object provided
     * It will use the parameters of pagination (size, page number, etc.) to figure out the subset
     * @param list
     * @param pagination
     * @return subset
     * @param <T>
     */
    public static <T> PaginationResult<T> createPaginationResultFromList(List<T> list, Pagination pagination) {
        PaginationResult<T> returnResult = new PaginationResult<T>();
        Integer page = pagination.getPage();
        Integer limit = pagination.getLimit();
        int start = (page - 1) * limit;
        int end = page * limit;
        if (end > list.size())
            end = list.size();
        if (start > list.size())
            start = list.size();
        if (start < 0)
            start = 0;
        if (end < 0)
            end = 0;
        if (start > end)
            start = end;
        List<T> sublist = list.subList(start, end);
        returnResult.setPopulatedResults(sublist);
        returnResult.setTotalCount(list.size());
        return returnResult;
    }

}
