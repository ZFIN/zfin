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

@Log4j2
public class UniProtLoadService {


    private static final String PUBLICATION_ATTRIBUTION_ID = "ZDB-PUB-230615-71";

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
        List<Marker> markers = getMarkerRepository().getMarkersByZdbIDs(actions.stream().map(UniProtLoadAction::getGeneZdbID).toList());
        Map<String, Marker> markerMap = markers.stream().collect(Collectors.toMap(Marker::getZdbID, marker -> marker));
        List<MarkerDBLink> dblinks = new ArrayList<>();
        for(UniProtLoadAction action : actions) {
            log.debug("Adding dblink: " + action.getAccession() + " " + action.getGeneZdbID());
            Marker marker = markerMap.get(action.getGeneZdbID());
            MarkerDBLink newLink = new MarkerDBLink();
            newLink.setAccessionNumber(action.getAccession());
            newLink.setMarker(marker);
            newLink.setReferenceDatabase(getUniProtReferenceDatabase());
            newLink.setLength(action.getLength());
            newLink.setLinkInfo(getUniProtLoadLinkInfo());
            dblinks.add(newLink);
        }
        Publication publication = getPublicationRepository().getPublication(PUBLICATION_ATTRIBUTION_ID);
        getSequenceRepository().addDBLinks(dblinks, publication, 50);
    }

    private static void bulkDeleteAction(List<UniProtLoadAction> actions) {
        ReferenceDatabase referenceDatabase = getUniProtReferenceDatabase();
        List<DBLink> dblinksToDelete = new ArrayList<>();
        for(UniProtLoadAction action : actions) {
            DBLink dblink = getSequenceRepository().getDBLink(action.getGeneZdbID(), action.getAccession(), referenceDatabase.getForeignDB().getDbName().toString());
            System.err.println("Removing dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
            log.debug("Removing dblink: " + dblink.getZdbID());
            getSequenceRepository().deleteReferenceProteinByDBLinkID(dblink.getZdbID());
            dblinksToDelete.add(dblink);
        }
        getSequenceRepository().removeDBLinks(dblinksToDelete);
    }

    private static void loadAction(UniProtLoadAction action) {
        Marker marker = getMarkerRepository().getMarker(action.getGeneZdbID());
        MarkerDBLink newLink = new MarkerDBLink();
        newLink.setAccessionNumber(action.getAccession());
        newLink.setMarker(marker);
        newLink.setReferenceDatabase(getUniProtReferenceDatabase());
        newLink.setLength(action.getLength());
        newLink.setLinkInfo(getUniProtLoadLinkInfo());

        Publication publication = getPublicationRepository().getPublication(PUBLICATION_ATTRIBUTION_ID);

        ArrayList<MarkerDBLink> dblinks = new ArrayList<>();
        dblinks.add(newLink);
        getSequenceRepository().addDBLinks(dblinks, publication, 1);
    }

    public static ReferenceDatabase getUniProtReferenceDatabase() {
        return getSequenceRepository().getReferenceDatabase(UNIPROTKB, POLYPEPTIDE, SEQUENCE, ZEBRAFISH);
    }

    private static void deleteAction(UniProtLoadAction action) {
        ReferenceDatabase referenceDatabase = getUniProtReferenceDatabase();
        DBLink dblink = getSequenceRepository().getDBLink(action.getGeneZdbID(), action.getAccession(), referenceDatabase.getForeignDB().getDbName().toString());
        System.err.println("Removing dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
        log.debug("Removing dblink: " + dblink.getZdbID());
        getSequenceRepository().deleteReferenceProteinByDBLinkID(dblink.getZdbID());
        getSequenceRepository().removeDBLinks(Collections.singletonList(dblink));
    }

    private static String getUniProtLoadLinkInfo() {
        //eg. 2023-08-27 Swiss-Prot
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return timestamp + " Swiss-Prot";
    }
}
