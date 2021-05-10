package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

import java.util.List;
import java.util.Map;

public class DashboardPublicationList {

    @JsonView(View.API.class) int totalCount;
    @JsonView(View.API.class) List<DashboardPublicationBean> publications;
    @JsonView(View.API.class) Map<String, Long> statusCounts;

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
