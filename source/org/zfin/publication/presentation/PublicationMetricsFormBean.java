package org.zfin.publication.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;

import java.io.Serializable;
import java.util.Calendar;

@Getter
@Setter
public class PublicationMetricsFormBean implements Serializable {

    @Getter
    public enum QueryType {
        PET_DATE("PET Date", true, true),
        STATUS_DATE("Status Change Date", true, true),
        CUMULATIVE("Cumulative Stats", false, true),
        SNAPSHOT("Snapshot", false, false);

        private final String display;
        private final boolean fromRequired;
        private final boolean toRequired;

        QueryType(String display, boolean fromRequired, boolean toRequired) {
            this.display = display;
            this.fromRequired = fromRequired;
            this.toRequired = toRequired;
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
        MAXIMUM("Maximum Days in Status"),
        OLDEST_AVERAGE("Average Age of 10 Oldest");

        private String display;

        Statistic(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    @Getter
    public enum GroupType {
        ACTIVE("Active"),
        INDEXED("Indexed Status"),
        STATUS("Curation Status"),
        LOCATION("Location");

        private String display;

        GroupType(String display) {
            this.display = display;
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

}
