package org.zfin.sequence.blast;

import java.io.Serializable;

/**
 */
public class DatabaseRelationship implements Comparable<DatabaseRelationship>, Serializable{

    private Long id ;
    private Integer order ;
    private Database parent ;
    private Database child ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Database getParent() {
        return parent;
    }

    public void setParent(Database parent) {
        this.parent = parent;
    }

    public Database getChild() {
        return child;
    }

    public void setChild(Database child) {
        this.child = child;
    }

    public int compareTo(DatabaseRelationship databaseRelationship) {
        return  (int) (getOrder() - databaseRelationship.getOrder()) ;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DatabaseRelationship");
        sb.append("{order=").append(order);
        sb.append(", parent=").append(parent);
        sb.append(", child=").append(child);
        sb.append('}');
        return sb.toString();
    }
}
