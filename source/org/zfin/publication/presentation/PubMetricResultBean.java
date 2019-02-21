package org.zfin.publication.presentation;

import java.util.Date;

public class PubMetricResultBean {

    private Object category;
    private Date date;
    private Long count;

    public PubMetricResultBean() {
    }

    public PubMetricResultBean(Object category, Date date, Long count) {
        this.category = category;
        this.date = date;
        this.count = count;
    }

    public Object getCategory() {
        return category;
    }

    public void setCategory(Object category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "PubMetricResultBean{" +
                "category=" + category +
                ", date=" + date +
                ", count=" + count +
                '}';
    }
}
