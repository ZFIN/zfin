package org.zfin.framework.presentation;

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
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
