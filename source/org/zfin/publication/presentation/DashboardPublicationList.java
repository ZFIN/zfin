package org.zfin.publication.presentation;

import java.util.List;
import java.util.Map;

public class DashboardPublicationList {

    int totalCount;
    List<DashboardPublicationBean> publications;
    Map<String, Long> statusCounts;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<DashboardPublicationBean> getPublications() {
        return publications;
    }

    public void setPublications(List<DashboardPublicationBean> publications) {
        this.publications = publications;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }
}
