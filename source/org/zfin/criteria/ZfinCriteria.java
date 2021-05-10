package org.zfin.criteria;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a basic container to hold criteria related to retrieving
 * BO from a persistence storage.
 */
public class ZfinCriteria {

    private int firstRow;
    private int maxDisplayRows;
    private boolean usePagination;
    private List<String> orderBy = new ArrayList<String>();
    private List<Boolean> ascending = new ArrayList<Boolean>();
    private boolean orRelationship;
    private List<Criteria> criteria;

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public int getMaxDisplayRows() {
        return maxDisplayRows;
    }

    public void setMaxDisplayRows(int maxDisplayRows) {
        this.maxDisplayRows = maxDisplayRows;
    }

    public boolean isUsePagination() {
        return usePagination;
    }

    public void setUsePagination(boolean usePagination) {
        this.usePagination = usePagination;
    }

    public void addOrdering(String orderVariable, boolean asc) {
        orderBy.add(orderVariable);
        ascending.add(asc);
    }

    public void removeOrderByFields() {
        orderBy.clear();
    }

    public List<String> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<String> orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isOrRelationship() {
        return orRelationship;
    }

    public void setOrRelationship(boolean orRelationship) {
        this.orRelationship = orRelationship;
    }

    public String getOrderByClause() {
        StringBuilder sb = new StringBuilder();
        if (orderBy.size() > 0) {
            sb.append(" ORDER BY ");
            int index = 0;
            for (String variable : orderBy) {
                sb.append(variable);
                boolean asc = ascending.get(index++);
                if (asc)
                    sb.append(" ASC");
                else
                    sb.append(" DESC");
            }
        }
        return sb.toString();
    }

}
