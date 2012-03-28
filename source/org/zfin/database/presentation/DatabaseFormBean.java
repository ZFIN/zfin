package org.zfin.database.presentation;

/**
 * Created by IntelliJ IDEA.
 * User: cmpich
 * Date: 3/6/12
 * Time: 6:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseFormBean {
    
    private String orderBy;
    private String dbname;

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
