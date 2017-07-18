package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.publication.Journal;
import org.zfin.repository.RepositoryFactory;

import java.util.Calendar;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

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
        infrastructureRepository.insertUpdatesTable(marker, "", actionDone);
    }
    public static void insertUpdate(Journal journal, String actionDone) {
        logger.debug("Update: " + journal.getZdbID() + " " + actionDone);
        infrastructureRepository.insertUpdatesTable(journal, "", actionDone);
    }

    public static void insertUpdate(Marker marker, String fieldname, String oldValue, String newValue) {
        logger.debug("Update " + fieldname + ": " + marker.getZdbID() + " old: " + oldValue + " new: " + newValue);
        infrastructureRepository.insertUpdatesTable(marker, fieldname, "", newValue, oldValue);
    }

    public static Object getEntityById(String entityID) {
        if (entityID == null)
            return null;

        // Todo: This could be done much more generically
        if (ActiveSource.validateID(entityID) == ActiveSource.Type.PUB)
            return getPublicationRepository().getPublication(entityID);
        if (ActiveSource.validateID(entityID) == ActiveSource.Type.PERS)
            return getProfileRepository().getPerson(entityID);

        throw new RuntimeException("No implementation for this type of entity: " + entityID);
    }

    public static String getZdbDate(Calendar date) {
        if (date == null)
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(Integer.toString(date.get(Calendar.YEAR)).substring(2));
        int month = date.get(Calendar.MONTH) + 1;
        if (month < 10)
            builder.append("0");
        builder.append(month);
        int day = date.get(Calendar.DAY_OF_MONTH);
        if (day < 10)
            builder.append("0");
        builder.append(day);
        return builder.toString();
    }
}
