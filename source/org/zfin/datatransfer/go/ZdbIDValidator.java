package org.zfin.datatransfer.go;

import org.apache.commons.collections4.CollectionUtils;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.infrastructure.ReplacementZdbID;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Helper class for validating ZDB IDs
 */
public class ZdbIDValidator {

    /**
     * returns true if the ID exists in the zdb_active_data table
     * @param ID
     * @return true if the ID exists in the zdb_active_data table
     */
    public static boolean validateExists(String ID) {
        boolean isValidStructure = ActiveData.validateActiveData(ID);
        if (!isValidStructure) {
            return false;
        }
        ActiveData activeData = getInfrastructureRepository().getActiveData(ID);
        return activeData != null;
    }

    /**
     * @param IDs
     * @return true if all the IDs exist in the zdb_active_data table
     *
     */
    public static boolean validateAllIDsExist(Set<String> IDs) {
        return 0 == getInvalidIDsFromSet(IDs).size();
    }

    /**
     * Alias, but accepting List instead of Set
     * @param IDs
     * @return true if all the IDs exist in the zdb_active_data table
     */
    public static boolean validateAllIDsExist(List<String> IDs) {
        return validateAllIDsExist(new HashSet<>(IDs));
    }

    /**
     * @param IDs
     * @return all of the given IDs that do not show up in the zdb_active_data table
     */
    public static List<String> getInvalidIDsFromSet(Set<String> IDs) {
        List<String> resultIDs = getInfrastructureRepository()
                                    .getAllActiveData(IDs)
                                    .stream()
                                    .map(ActiveData::getZdbID)
                                    .toList();
        return (List<String>) CollectionUtils.subtract(IDs, resultIDs);
    }

    /**
     * @param IDs
     * @return all of the given IDs that do not show up in the zdb_active_data table
     */
    public static List<String> getInvalidSourceIDsFromSet(Set<String> IDs) {
        List<String> resultIDs = getInfrastructureRepository()
                                    .getAllActiveSource(IDs)
                                    .stream()
                                    .map(ActiveSource::getZdbID)
                                    .toList();
        return (List<String>) CollectionUtils.subtract(IDs, resultIDs);
    }

    /**
     * @param IDs
     * @return all of the given IDs that do not show up in the zdb_active_data table -- ignoring merged IDs.
     */
    public static List<String> getInvalidIDsFromSetResolvingMerged(Set<String> IDs) {
        List<String> invalidIDs = getInvalidIDsFromSet(IDs);
        List<String> mergedIDs = getInfrastructureRepository()
                .getAllReplacementZdbIds(invalidIDs)
                .stream()
                .map(ReplacementZdbID::getOldZdbID)
                .toList();

        return (List<String>) CollectionUtils.subtract(invalidIDs, mergedIDs);
    }

}
