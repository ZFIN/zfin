package org.zfin.repository;

import org.zfin.framework.presentation.PaginationResult;
import org.hibernate.ScrollableResults;

import java.util.ArrayList;
import java.util.List;

/**
 * This class build PaginationResults using Hibernate / JDBC Constructs
 */
public class PaginationResultFactory  {

    /**
     * @return Result of scrolling
     * @param maxRecords
     * @param scrollableResults
     */
    public static <T> PaginationResult<T>  createResultFromScrollableResultAndClose(int maxRecords, ScrollableResults scrollableResults){
        PaginationResult<T> returnResult = new PaginationResult() ;
        List<T> list = new ArrayList<T>() ;
        while( scrollableResults.next() && scrollableResults.getRowNumber() < maxRecords){
            list.add( (T) scrollableResults.get(0)) ;
        }

        scrollableResults.last() ;
        returnResult.setTotalCount(scrollableResults.getRowNumber() +1);
        returnResult.setPopulatedResults(list );
        scrollableResults.close();
        return returnResult ;
    }

    /**
     *
     * @param startRecord  This is inclusive.
     * @param stopRecord  This is exclusive.
     * @param scrollableResults
     * @return
     */
    public static <T> PaginationResult<T>  createResultFromScrollableResultAndClose(int startRecord,int stopRecord, ScrollableResults scrollableResults){
        PaginationResult<T> returnResult = new PaginationResult() ;
        List<T> list = new ArrayList<T>() ;
        if(startRecord==0){
            scrollableResults.beforeFirst();
        }
        else{
            scrollableResults.setRowNumber(startRecord-1) ;
        }
        while( scrollableResults.next() && scrollableResults.getRowNumber() < stopRecord){
            list.add( (T) scrollableResults.get(0)) ;
        }

        scrollableResults.last() ;
        returnResult.setTotalCount(scrollableResults.getRowNumber() +1);
        returnResult.setPopulatedResults(list );
        scrollableResults.close();
        return returnResult ;
    }
}
