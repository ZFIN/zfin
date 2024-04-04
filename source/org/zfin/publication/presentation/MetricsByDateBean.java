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

    @Deprecated
    public boolean beansEquals(MetricsByDateBean bean) {
        if (this == bean) return true;
        if (bean == null || getClass() != bean.getClass()) return false;

        if (category != null ? !category.equals(bean.category) : bean.category != null) return false;
        if (date != null ? !date.equals(bean.date) : bean.date != null) return false;
        return count != null ? count.equals(bean.count) : bean.count == null;
    }
}
