package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

/**
 * Column filters of the publication page's NCBI links table. {@code zdbID}, {@code symbol} and
 * {@code accession} are case-insensitive substring matches; the other three are the values picked from the table's checkbox lists,
 * which the DataTable sends as one '|'-separated string per column.
 *
 * @param types       type labels ("NCBI Gene", "RefSeq RNA", ...)
 * @param assemblies  assembly names, or {@link #NO_ASSEMBLY} for links without any assembly
 * @param dblinkDates dates encoded in the db_link ZDB ID, formatted yyyy-MM-dd
 */
public record PublicationNcbiLinkFilter(String zdbID,
                                        String symbol,
                                        String accession,
                                        List<String> types,
                                        List<String> assemblies,
                                        List<String> dblinkDates) {

    public static final String NO_ASSEMBLY = "None";

    /**
     * The yyMMdd date part of a ZDB ID. Two-digit years 70-99 are 19xx and 00-69 are 20xx, as in
     * postgres' to_date(..., 'YYMMDD'); ZFIN IDs start in the 1990s.
     */
    private static final DateTimeFormatter ID_DATE = new DateTimeFormatterBuilder()
        .appendValueReduced(ChronoField.YEAR, 2, 2, 1970)
        .appendPattern("MMdd")
        .toFormatter();

    public static PublicationNcbiLinkFilter fromRequest(String zdbID, String symbol, String accession, String types,
                                                        String assemblies, String dblinkDates) {
        return new PublicationNcbiLinkFilter(StringUtils.trimToNull(zdbID), StringUtils.trimToNull(symbol),
            StringUtils.trimToNull(accession),
            splitSelection(types), splitSelection(assemblies), splitSelection(dblinkDates));
    }

    private static List<String> splitSelection(String value) {
        if (StringUtils.isBlank(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("\\|")).map(String::trim).filter(StringUtils::isNotEmpty).toList();
    }

    /**
     * "261106" (the date part of a ZDB ID) to "2026-11-06"; null if it is not a valid date.
     */
    public static String idDateToIsoDate(String idDate) {
        try {
            return LocalDate.parse(idDate, ID_DATE).toString();
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    /**
     * "2026-11-06" to "261106", the date part of a ZDB ID; null if it is not a valid date.
     */
    public static String isoDateToIdDate(String isoDate) {
        try {
            return LocalDate.parse(isoDate).format(ID_DATE);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}
