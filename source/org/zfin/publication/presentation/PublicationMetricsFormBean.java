package org.zfin.publication.presentation;

import org.zfin.publication.Publication;
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

    public enum GroupType {
        ACTIVE("Active"),
        INDEXED("Indexed"),
        STATUS("Status"),
        LOCATION("Location");

        private String display;

        GroupType(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public static final String[] INDEXED_STATUSES = new String[] { "Indexed", "Unindexed" };

    private QueryType queryType;
    private String fromDate;
    private String toDate;
    private Interval groupBy;
    private GroupType groupType;
    private Statistic[] statistics;
    private Publication.Status[] activationStatuses = Publication.Status.values();
    private String[] indexedStatuses = INDEXED_STATUSES;
    private PublicationTrackingStatus.Name[] statuses = PublicationTrackingStatus.Name.values();
    private PublicationTrackingLocation.Name[] locations = PublicationTrackingLocation.Name.values();
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

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public Publication.Status[] getActivationStatuses() {
        return activationStatuses;
    }

    public void setActivationStatuses(Publication.Status[] activationStatuses) {
        this.activationStatuses = activationStatuses;
    }

    public String[] getIndexedStatuses() {
        return indexedStatuses;
    }

    public void setIndexedStatuses(String[] indexedStatuses) {
        this.indexedStatuses = indexedStatuses;
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
