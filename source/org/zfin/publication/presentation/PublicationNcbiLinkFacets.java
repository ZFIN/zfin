package org.zfin.publication.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Number of a publication's NCBI Gene and RefSeq links per value of the NCBI links table's
 * checkbox filters. Each facet's counts honour all active filters except the facet's own
 * selection. Each map is in display order and lists every value the publication has, with 0 where
 * the other filters exclude all its links; a link on several assemblies counts once for each.
 *
 * @param types       type label to count
 * @param assemblies  assembly name to count in assembly order, then
 *                    {@link PublicationNcbiLinkFilter#NO_ASSEMBLY} for links without an assembly
 * @param dblinkDates date encoded in the db_link ZDB ID (yyyy-MM-dd) to count, newest first
 */
public record PublicationNcbiLinkFacets(Map<String, Long> types,
                                        Map<String, Long> assemblies,
                                        Map<String, Long> dblinkDates) {

    public static List<String> options(Map<String, Long> counts) {
        return new ArrayList<>(counts.keySet());
    }
}
