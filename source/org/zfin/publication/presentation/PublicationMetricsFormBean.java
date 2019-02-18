package org.zfin.publication.presentation;

import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;

import java.io.Serializable;
import java.util.Calendar;

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
        YEAR("Year", Calendar.YEAR, "yyyy"),
        MONTH("Month", Calendar.MONTH, "MMMM yyyy"),
        DAY("Day", Calendar.DAY_OF_MONTH, "dd MMMM yyyy");

        private String display;
        private int field;
        private String format;

        Interval(String display, int field, String format) {
            this.display = display;
            this.field = field;
            this.format = format;
        }

        public String getDisplay() {
            return display;
        }

        public int getField() {
            return field;
        }

        public String getFormat() {
            return format;
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
