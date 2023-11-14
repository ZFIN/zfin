package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.uniprot.persistence.UniProtRelease;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

@Getter
@Setter
@Log4j2
public class SecondaryTermLoadService {


    public static final String DBLINK_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;
    public static final String EXTNOTE_PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;


    public static final String EXTNOTE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-47";
    public static final String INTERPRO_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-48";
    public static final String EC_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-49";
    public static final String PFAM_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-50";
    public static final String PROSITE_REFERENCE_DATABASE_ID = "ZDB-FDBCONT-040412-51";


    /**
     * Process the actions.
     * Some examples of actions from a run are below (count dbName type subType):
     *   33 InterPro DELETE DB_LINK
     *  851 InterPro LOAD DB_LINK
     *  306 EC LOAD DB_LINK
     *   15 Pfam DELETE DB_LINK
     *  332 Pfam LOAD DB_LINK
     *    9 PROSITE DELETE DB_LINK
     *  233 PROSITE LOAD DB_LINK
     *  635 InterPro LOAD MARKER_GO_TERM_EVIDENCE
     *   16 InterPro DELETE MARKER_GO_TERM_EVIDENCE
     *  400 EC LOAD MARKER_GO_TERM_EVIDENCE
     *  636 UniProtKB LOAD MARKER_GO_TERM_EVIDENCE
     *  124 UniProtKB DELETE MARKER_GO_TERM_EVIDENCE
     * 25983 null LOAD EXTERNAL_NOTE
     *
     * @param actions list of actions to perform
     */
    public static void processActions(List<SecondaryTermLoadAction> actions, UniProtRelease release) {
        //groupBy the actions
        Map<SecondaryTermLoadAction.Type, List<SecondaryTermLoadAction>> groupedByType = actions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getType));

        //process the actions
        currentSession().beginTransaction();
        for(SecondaryTermLoadAction.Type type : groupedByType.keySet()) {
            log.debug("Processing action types of " + type);
            List<SecondaryTermLoadAction> transactionActions = groupedByType.get(type);
            processLoadActions(type, transactionActions);
            log.debug("Finished action types of " + type);
        }
        if (release != null) {
            if (release.getProcessedDate() == null) {
                log.error("Release ID# " + release.getUpr_id() + " has not been processed yet. Must process before secondary terms.");
                currentSession().getTransaction().rollback();
                System.exit(5);
            }
            release.setSecondaryLoadDate(new Date());
            getInfrastructureRepository().updateUniProtRelease(release);
        }
        log.debug("Committing changes");
        currentSession().getTransaction().commit();
    }

    private static void processLoadActions(SecondaryTermLoadAction.Type type, List<SecondaryTermLoadAction> transactionActions) {
        if (type != SecondaryTermLoadAction.Type.LOAD && type != SecondaryTermLoadAction.Type.DELETE) {
            log.warn("Ignoring action type " + type);
            return;
        }

        //groupBy the actions by subtype
        Map<SecondaryTermLoadAction.SubType, List<SecondaryTermLoadAction>> groupedBySubType =
                transactionActions.stream().collect(Collectors.groupingBy(SecondaryTermLoadAction::getSubType));

        for(SecondaryTermLoadAction.SubType subType : groupedBySubType.keySet()) {
            log.debug("Processing action subtypes of " + subType);
            List<SecondaryTermLoadAction> subTypeActions = groupedBySubType.get(subType);
            processLoadActionsBySubType(subType, subTypeActions);
            log.debug("Finished action subtypes of " + subType);
        }
    }

    private static void processLoadActionsBySubType(SecondaryTermLoadAction.SubType subType, List<SecondaryTermLoadAction> subTypeActions) {
        String handlerClass = subTypeActions.get(0).getHandlerClass();
        SecondaryLoadHandler handler = getHandler(handlerClass);
        if (handler.isSubTypeHandlerFor() != subType) {
            throw new RuntimeException("Handler class " + handlerClass + " does not support subType " + subType);
        }
        handler.processActions(subTypeActions);
    }

    /**
     * Use reflection to get an instance of the class that handles the actions.
     * @param handlerClass the class name
     * @return the handler
     */
    private static SecondaryLoadHandler getHandler(String handlerClass) {
        try {
            Class<?> cls = Class.forName(handlerClass);
            Object instance = cls.getDeclaredConstructor().newInstance();
            return (SecondaryLoadHandler) instance;
        } catch (ClassNotFoundException e) {
            // Handle the case where the class doesn't exist
            e.printStackTrace();
            throw new RuntimeException("No such handler class: " + handlerClass);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            // Handle other potential exceptions
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate handler class: " + handlerClass);
        }
    }

    public static ReferenceDatabase getReferenceDatabaseForAction(SecondaryTermLoadAction action) {
        return getReferenceDatabase(getReferenceDatabaseIDForAction(action));
    }

    public static String getReferenceDatabaseIDForAction(SecondaryTermLoadAction action) {
        String referenceDatabaseID = null;
        switch (action.getDbName()) {
            case INTERPRO -> referenceDatabaseID = INTERPRO_REFERENCE_DATABASE_ID;
            case EC -> referenceDatabaseID = EC_REFERENCE_DATABASE_ID;
            case PFAM -> referenceDatabaseID = PFAM_REFERENCE_DATABASE_ID;
            case PROSITE -> referenceDatabaseID = PROSITE_REFERENCE_DATABASE_ID;
            default -> log.error("Unknown dblink dbname to load " + action.getDbName());
        }
        return referenceDatabaseID;
    }

    public static ReferenceDatabase getReferenceDatabase(String referenceDatabaseID) {
        return getSequenceRepository().getReferenceDatabaseByID(referenceDatabaseID);
    }

}
