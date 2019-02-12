package org.zfin.publication.presentation;

import java.util.Date;

public class PubMetricResultBean {

    private Long statusId;
    private Long locationId;
    private Date date;
    private Long count;

    public PubMetricResultBean() {
    }

    public PubMetricResultBean(Long statusId, Long locationId, Date date, Long count) {
        this.statusId = statusId;
        this.locationId = locationId;
        this.date = date;
        this.count = count;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
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
                "statusId=" + statusId +
                ", locationId=" + locationId +
                ", date=" + date +
                ", count=" + count +
                '}';
    }
}
