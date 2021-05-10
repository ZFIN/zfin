package org.zfin.framework.search;

import java.util.List;

/**
 * Intended for passing search parameters to the search service from the controller
 *
 * Each of our searches should use a specific class that inherits from this.
 */
public abstract class AbstractSearchCriteria {
    private int rows;
    private int start;
    protected List<SortType> sort;

    public abstract List<SearchCriterion> getAllCriteria();

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public List<SortType> getSort() {
        return sort;
    }

    public void setSort(List<SortType> sort) {
        this.sort = sort;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nstart: ");
        sb.append(start);
        sb.append("\nrows: ");
        sb.append(rows);
        

        for(SearchCriterion criterion : getAllCriteria()) {
            sb.append("\n");
            sb.append(criterion.getType().getName());
            sb.append(": ");
            sb.append(criterion.getValue());
        }
        if (sb.length() > 0)
            sb.append("\n");
        else if (sb.length() == 0)
            sb.append("No Criteria");
        return sb.toString();
    }

}
