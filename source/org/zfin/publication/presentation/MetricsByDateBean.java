package org.zfin.publication.presentation;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
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

    @Override
    public String toString() {
        return "MetricsByDateBean{" +
                "category=" + category +
                ", date=" + date +
                ", count=" + count +
                '}';
    }
}
