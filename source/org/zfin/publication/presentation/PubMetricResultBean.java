package org.zfin.publication.presentation;

import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;

import java.util.Date;

public class PubMetricResultBean {

    private PublicationTrackingStatus.Name status;
    private PublicationTrackingLocation.Name location;
    private Date date;
    private Long count;

    public PubMetricResultBean() {
    }

    public PubMetricResultBean(PublicationTrackingStatus.Name status, PublicationTrackingLocation.Name location, Date date, Long count) {
        this.status = status;
        this.location = location;
        this.date = date;
        this.count = count;
    }

    public PublicationTrackingStatus.Name getStatus() {
        return status;
    }

    public void setStatus(PublicationTrackingStatus.Name status) {
        this.status = status;
    }

    public PublicationTrackingLocation.Name getLocation() {
        return location;
    }

    public void setLocation(PublicationTrackingLocation.Name location) {
        this.location = location;
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
                "status=" + status +
                ", location=" + location +
                ", date=" + date +
                ", count=" + count +
                '}';
    }
}
