package org.zfin.uniprot;

import lombok.extern.log4j.Log4j2;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.uniprot.persistence.UniProtRelease;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.Species.Type.ZEBRAFISH;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.ForeignDB.AvailableName.UNIPROTKB;
import static org.zfin.sequence.ForeignDBDataType.DataType.POLYPEPTIDE;
import static org.zfin.sequence.ForeignDBDataType.SuperType.SEQUENCE;
import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;
import static org.zfin.uniprot.UniProtTools.LOAD_PUBS;

@Log4j2
public class UniProtLoadService {


    public static final String PUBLICATION_ATTRIBUTION_ID = AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

    public static void processActions(Set<UniProtLoadAction> actions, UniProtRelease release) {
        currentSession().beginTransaction();

        bulkProcessActions(actions);

        if (release != null) {
            release.setProcessedDate(new Date());
            currentSession().saveOrUpdate(release);
        }
        currentSession().getTransaction().commit();
    }

    private static void bulkProcessActions(Set<UniProtLoadAction> actions) {
        Map<UniProtLoadAction.Type, List<UniProtLoadAction>> groupedActions = actions.stream().collect(Collectors.groupingBy(UniProtLoadAction::getType));

        for(UniProtLoadAction.Type type : groupedActions.keySet()) {
            if(type.equals(UniProtLoadAction.Type.LOAD)) {
                bulkLoadAction(groupedActions.get(type));
            } else if(type.equals(UniProtLoadAction.Type.DELETE)) {
                bulkDeleteAction(groupedActions.get(type));
            } else {
                //ignore other action types used for reporting
            }
        }
    }

    private static void bulkLoadAction(List<UniProtLoadAction> actions) {
        Map<UniProtLoadAction.SubType, List<UniProtLoadAction>> groupedActions =
                actions.stream().collect(Collectors.groupingBy(UniProtLoadAction::getSubType));

        for(UniProtLoadAction.SubType subType : groupedActions.keySet()) {
            if (subType.equals(UniProtLoadAction.SubType.ADD_ATTRIBUTION)) {
                bulkAddAttributionAction(groupedActions.get(subType));
            } else if (subType.equals(UniProtLoadAction.SubType.MATCH_BY_REFSEQ)) {
                bulkLoadActionForNewDbLinks(groupedActions.get(subType));
            } else {
                //ignore other action types used for reporting
                throw new RuntimeException("Unknown action load subtype: " + subType);
            }
        }

    }

    private static void bulkAddAttributionAction(List<UniProtLoadAction> uniProtLoadActions) {
        for(UniProtLoadAction action : uniProtLoadActions) {
            log.info("Adding attribution: " + action.getAccession() + " " + action.getGeneZdbID());
            DBLink existingDBLink = getSequenceRepository().getDBLink(action.getGeneZdbID(), action.getAccession(), UNIPROTKB.toString());
            log.info("Existing dblink: " + existingDBLink);
            if(existingDBLink == null) {
                throw new RuntimeException("No existing dblink found for: " + action.getAccession() + " " + action.getGeneZdbID());
            }
            getInfrastructureRepository().insertRecordAttribution(existingDBLink.getZdbID(), PUBLICATION_ATTRIBUTION_ID);
        }
    }

    private static void bulkLoadActionForNewDbLinks(List<UniProtLoadAction> actions) {
        ReferenceDatabase refDB = getUniProtReferenceDatabase();
        String linkInfo = getUniProtLoadLinkInfo();
        List<Marker> markers = getMarkerRepository().getMarkersByZdbIDs(actions.stream().map(UniProtLoadAction::getGeneZdbID).distinct().toList());
        Map<String, Marker> markerMap = markers.stream().collect(Collectors.toMap(Marker::getZdbID, marker -> marker, (a, b) -> a));
        List<MarkerDBLink> dblinks = new ArrayList<>();
        for(UniProtLoadAction action : actions) {
            log.info("Adding dblink to load list: " + action.getAccession() + " " + action.getGeneZdbID());
            Marker marker = markerMap.get(action.getGeneZdbID());
            MarkerDBLink newLink = new MarkerDBLink();
            newLink.setAccessionNumber(action.getAccession());
            newLink.setMarker(marker);
            newLink.setReferenceDatabase(refDB);
            newLink.setLength(action.getLength());
            newLink.setLinkInfo(linkInfo);
            dblinks.add(newLink);
        }
        addHistoryUpdatesForNewDbLinks(dblinks);
        Publication publication = getPublicationRepository().getPublication(PUBLICATION_ATTRIBUTION_ID);
        log.info("Loading dblink list to database");
        getSequenceRepository().addDBLinks(dblinks, publication, 50);
    }

    private static void addHistoryUpdatesForNewDbLinks(List<MarkerDBLink> dblinks) {
        for(MarkerDBLink dblink : dblinks) {
            getInfrastructureRepository().insertUpdatesTableWithoutPerson(dblink.getMarker().getZdbID(), "dblink", null, dblink.getAccessionNumber(), "UniProtKB link added by load process.");
        }
    }

    private static void bulkDeleteAction(List<UniProtLoadAction> actions) {
        ReferenceDatabase referenceDatabase = getUniProtReferenceDatabase();
        List<DBLink> dblinksToDelete = new ArrayList<>();
        for(UniProtLoadAction action : actions) {
            DBLink dblink = getSequenceRepository().getDBLink(action.getGeneZdbID(), action.getAccession(), referenceDatabase.getForeignDB().getDbName().toString());
            List<String> pubIDs = dblink.getPublicationIdsAsList();
            if (pubIDs.size() >= 1) {
                //are all of the existing attributions load pubs? if so, remove the dblink
                if (pubIDs.stream().allMatch(UniProtTools::isLoadPublication)) {
                    log.info("Removing dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
                    getSequenceRepository().deleteReferenceProteinByDBLinkID(dblink.getZdbID());
                    dblinksToDelete.add(dblink);
                //are some of the existing attributions load pubs? if so, remove the load pub attributions
                } else if (pubIDs.stream().anyMatch(UniProtTools::isLoadPublication)) {
                    removeAttribution(dblink, action);
                }
            } else { //pubIDs.size() == 0
                //if there are no attributions, just remove the dblink
                log.info("Removing dblink with no attribution: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
                dblinksToDelete.add(dblink);
            }
        }
        addHistoryUpdatesForDeletedDbLinks(dblinksToDelete);
        getSequenceRepository().removeDBLinks(dblinksToDelete);
    }

    private static void addHistoryUpdatesForDeletedDbLinks(List<DBLink> dblinksToDelete) {
        for(DBLink dblink : dblinksToDelete) {
            getInfrastructureRepository().insertUpdatesTableWithoutPerson(dblink.getDataZdbID(), "dblink", dblink.getAccessionNumber(),null, "UniProtKB link removed by load process.");
        }
    }

    private static void removeAttribution(DBLink dblink, UniProtLoadAction action) {
        log.info("Removing attribution(s) from dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
        dblink.getPublications()
            .stream()
            .filter(attribution -> List.of(LOAD_PUBS).contains(attribution.getSourceZdbID()))
            .forEach(attribution -> {
                getInfrastructureRepository().deleteRecordAttribution(dblink.getZdbID(),attribution.getSourceZdbID());
            });
    }

    public static ReferenceDatabase getUniProtReferenceDatabase() {
        return getSequenceRepository().getReferenceDatabase(UNIPROTKB, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
    }

    private static String getUniProtLoadLinkInfo() {
        //eg. 2023-08-27 Swiss-Prot
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return timestamp + " Swiss-Prot";
    }
}
