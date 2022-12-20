package org.zfin.datatransfer.go;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GafEntriesValidator {

    private static final String ORGANIZATION_PREFIX = "ZFIN";

    /**
     * Given a list of GafEntries, return all IDs from the GafEntry inferences column that
     * are invalid.  They will be flagged as invalid if they are not in the zdb_active_data table.
     * This allows ZDB IDs that have been replaced.
     *
     * @param gafEntries list of GafEntries
     * @return all invalid IDs from the GafEntries
     */
    public static List<String> findInvalidZdbIDs(List<GafEntry> gafEntries) {
        //gather all the IDs first
        Set<String> allZdbIDs = new HashSet<>();
        for(GafEntry gaf : gafEntries) {
            allZdbIDs.addAll(gaf.getInferencesByOrganization(ORGANIZATION_PREFIX));
        }

        //get the bad IDs
        return ZdbIDValidator.getInvalidIDsFromSetResolvingMerged(allZdbIDs);
    }

    /**
     * This does the same validation as findInvalidZdbIDs, but throws an exception
     * if invalid IDs are found.
     *
     * @param gafEntries list of GafEntries
     * @throws GafEntryValidationException exception if invalid IDs found
     */
    public static void raiseExceptionForAnyInvalidIDs(List<GafEntry> gafEntries) throws GafEntryValidationException {
        List<String> badIDs = findInvalidZdbIDs(gafEntries);
        if (badIDs.size() != 0) {
            List<String> indices = getIndicesOfGafEntriesContainingIDs(gafEntries, badIDs);
            throw new GafEntryValidationException(
                    "Invalid ZDB IDs in GafEntries with index (roughly line number): \n\n" + String.join("\n", indices)
            );
        }
    }

    /**
     * Helper method to return the indices (should roughly correspond to line number) of gafEntries that
     * contain the badIDs.  (this method could be more efficient if computed at the time of the findInvalidZdbIDs).
     *
     * @param gafEntries list of entries to search through (haystack)
     * @param badIDs bad IDs to search for (needle)
     * @return collection of strings with index information about where the badIDs are found for error logging
     */
    private static List<String> getIndicesOfGafEntriesContainingIDs(List<GafEntry> gafEntries, List<String> badIDs) {
        List<String> badEntries = new ArrayList<>();
        int currentIndex = 2; //start at 2 to reflect the line number
        for(GafEntry entry : gafEntries) {
            List<String> inferences = entry.getInferencesByOrganization(ORGANIZATION_PREFIX);
            if (CollectionUtils.containsAny(inferences, badIDs)) {
                badEntries.add(
                        "  " +
                        currentIndex + ":" +
                        String.join(",", CollectionUtils.intersection(inferences, badIDs)) + ": " +
                        entry.toString().trim()
                );
            }
            currentIndex++;
        }
        return badEntries;
    }
}
