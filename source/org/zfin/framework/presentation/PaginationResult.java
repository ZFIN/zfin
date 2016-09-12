package org.zfin.framework.presentation;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * This class holds the total count for a particular query in addition to a list of a set type T
 * and a list of that subset for that particular query.
 */
public class PaginationResult<T> {

    private List<T> populatedResults = null;
    private int totalCount = -1;
    private int start ;

    public PaginationResult() {
    }


    public PaginationResult(List<T> populatedResults) {
        this.totalCount = populatedResults.size();
        this.populatedResults = populatedResults;
    }

    public PaginationResult(int totalCount, List<T> populatedResults) {
        this.totalCount = totalCount;
        this.populatedResults = populatedResults;
    }

    public PaginationResult(int totalCount, int start, List<T> populatedResults) {
        this.totalCount = totalCount;
        this.start = start;
        this.populatedResults = populatedResults;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getPopulatedResults() {
        return populatedResults;
    }

    public void setPopulatedResults(List<T> populatedResults) {
        this.populatedResults = populatedResults;
        if(CollectionUtils.isEmpty(populatedResults))
            totalCount = 0;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void add(PaginationResult<T> paginationResult) {
        this.totalCount += paginationResult.totalCount;
        this.populatedResults.addAll(paginationResult.getPopulatedResults());
    }
}
