package org.zfin.publication.repository;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Sep 27, 2006
 * Time: 5:20:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaginationUtil {

    private int firstRow;
    private int maxDisplayRows;
    private boolean usePagination;
    private List<String> orderBy = new ArrayList<String>();

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

    public void addOrdering(String orderVariable){
        orderBy.add(orderVariable);
    }

    public void removeOrderByFields() {
        orderBy.clear();
    }

    public String getOrderByClause(){
        StringBuilder sb = new StringBuilder();
        if(orderBy.size() > 0){
            sb.append(" ORDER BY ");
            for (String variable: orderBy){
                sb.append(variable);
            }
        }
        return sb.toString();
    }

}
