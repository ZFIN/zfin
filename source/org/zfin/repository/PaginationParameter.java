package org.zfin.repository;

/**
 * These parameters are used for paginated search results and allow
 * a more generic way to deal with this.
 */
public interface PaginationParameter {

    void setFirstRow(int firstRow);

    void setMaxDisplayRows(int maxRows);

    void setUsePagination(boolean usePagination);
}
