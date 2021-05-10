package org.zfin.publication.presentation;

import java.util.Date;

public class MetricsByDateBean {

    private Object category;
    private Date date;
    private Number count;

    public MetricsByDateBean() {
    }

    public MetricsByDateBean(Object category, Date date, Long count) {
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

    public Number getCount() {
        return count;
    }

    public void setCount(Number count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "MetricsByDateBean{" +
                "category=" + category +
                ", date=" + date +
                ", count=" + count +
                '}';
    }
}
