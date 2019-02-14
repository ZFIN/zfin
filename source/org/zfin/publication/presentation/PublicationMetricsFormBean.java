package org.zfin.publication.presentation;

import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;

import java.io.Serializable;

public class PublicationMetricsFormBean implements Serializable {

    public enum QueryType {
        PET_DATE("PET Date"),
        STATUS_DATE("Status Change Date"),
        CUMULATIVE("Cumulative Stats");

        private String display;

        QueryType(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum Interval {
        YEAR("Year"),
        MONTH("Month"),
        DAY("Day");

        private String display;

        Interval(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum Statistic {
        COUNT("Count"),
        AVERAGE("Average Days in Status"),
        STANDARD_DEVIATION("Standard Deviation Days in Status"),
        MINIMUM("Minimum Days in Status"),
        MAXIMUM("Maximum Days in Status");

        private String display;

        Statistic(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    private QueryType queryType;
    private String fromDate;
    private String toDate;
    private Interval groupBy;
    private Statistic[] statistics;
    private PublicationTrackingStatus.Name[] statuses;
    private PublicationTrackingLocation.Name[] locations;
    private boolean currentStatusOnly;

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Interval getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(Interval groupBy) {
        this.groupBy = groupBy;
    }

    public Statistic[] getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistic[] statistics) {
        this.statistics = statistics;
    }

    public PublicationTrackingStatus.Name[] getStatuses() {
        return statuses;
    }

    public void setStatuses(PublicationTrackingStatus.Name[] statuses) {
        this.statuses = statuses;
    }

    public PublicationTrackingLocation.Name[] getLocations() {
        return locations;
    }

    public void setLocations(PublicationTrackingLocation.Name[] locations) {
        this.locations = locations;
    }

    public boolean isCurrentStatusOnly() {
        return currentStatusOnly;
    }

    public void setCurrentStatusOnly(boolean currentStatusOnly) {
        this.currentStatusOnly = currentStatusOnly;
    }
}
