package org.zfin.sequence.blast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * One problem found with one physical blast database by {@link ValidateBlastDatabases}.
 *
 * @param abbrev       Abbreviation of the offending blast database.
 * @param problem      Short category, used as the report's grouping column.
 * @param detail       Human readable explanation, including what to do about it.
 * @param numSequences Sequence count as reported by the database header, or
 *                     {@link DatabaseStatistics#BAD_DATABASE} when unknown.
 * @param created      Creation date from the database header, or null when unknown.
 */
public record BlastDatabaseValidationFinding(String abbrev,
                                            Problem problem,
                                            String detail,
                                            int numSequences,
                                            Date created) {

    public enum Problem {
        UNREADABLE("unreadable"),
        EMPTY("empty"),
        NO_IDENTIFIER_INDEX("no identifier index"),
        ACCESSION_NOT_RETRIEVABLE("accession not retrievable"),
        STALE("stale"),
        ;

        private final String label;

        Problem(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Column order must match Validate-Blast-Databases_d.errors.headerColumns in
     * server_apps/DB_maintenance/report_data/report.properties.
     */
    public List<String> toReportRow() {
        return Arrays.asList(
                abbrev,
                problem.toString(),
                detail,
                numSequences == DatabaseStatistics.BAD_DATABASE ? "unknown" : String.format("%,d", numSequences),
                created == null ? "unknown" : new SimpleDateFormat("yyyy/MM/dd").format(created));
    }

    @Override
    public String toString() {
        return abbrev + " [" + problem + "]: " + detail;
    }
}
