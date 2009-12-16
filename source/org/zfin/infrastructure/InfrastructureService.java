package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;

/**
 * Service Class for Attribution, updates etc.
 */
public class InfrastructureService {
    private static Logger logger = Logger.getLogger(InfrastructureService.class);
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    /**
     * A method to make finding the user unnecessary, and create semi-consistent
     * comments in the updates table.
     *
     * @param marker     Marker to add the update for
     * @param actionDone Example: "removed ZDB-DBLINK-000000-1"
     */
    public static void insertUpdate(Marker marker, String actionDone) {
        logger.debug("Update: " + marker.getZdbID() + " " + actionDone);
        Person currentUser = Person.getCurrentSecurityUser();
        infrastructureRepository.insertUpdatesTable(marker, "", actionDone, currentUser);
    }

    public static void insertUpdate(Marker marker, String fieldname, String oldValue, String newValue) {
        logger.debug("Update " + fieldname + ": " + marker.getZdbID() + " old: " + oldValue + " new: " + newValue);
        Person currentUser = Person.getCurrentSecurityUser();
        infrastructureRepository.insertUpdatesTable(marker, fieldname, "", currentUser, newValue, oldValue);
    }

}
