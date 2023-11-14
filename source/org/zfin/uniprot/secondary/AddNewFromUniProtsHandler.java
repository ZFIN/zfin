package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.util.ZfinCollectionUtils.firstInEachGrouping;

/**
 * Adds InterPro, PFAM, EC, PROSITE accessions to db_links table.
 * This is based on the entries that appear in the uniprot release file.
 * If the accession is already in the database, it is not added.
 */
@Log4j2
public class AddNewFromUniProtsHandler implements SecondaryLoadHandler {

    private final ForeignDB.AvailableName dbName;

    public AddNewFromUniProtsHandler() {
        this.dbName = null;
    }

    public AddNewFromUniProtsHandler(ForeignDB.AvailableName dbName) {
        this.dbName = dbName;
    }

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> newActions = getLoadActionsNotAlreadyInDatabase(uniProtRecords, context);

        //remove duplicates
        log.debug("Removing duplicates from " + newActions.size() + " new actions");

        //first group by gene, accession
        newActions = firstInEachGrouping(newActions, action -> action.getGeneZdbID() + "," + action.getAccession() + "," + action.getDbName());
        log.debug("Removed duplicates, now have " + newActions.size() + " new actions");

        actions.addAll(newActions);
    }

    private List<SecondaryTermLoadAction> getLoadActionsNotAlreadyInDatabase(Map<String, RichSequenceAdapter> uniProtRecords, SecondaryLoadContext context) {
        //if there is an interpro in the load file, but not in the DB for the corresponding gene, add it.
        // corresponding gene means: get the gene by taking the uniprot from the load file and cross referencing it to loaded uniprots

        int previouslyExistedCount = 0;
        int newlyAddedCount = 0;

        List<SecondaryTermLoadAction> newActions = new java.util.ArrayList<>();
        for(String uniprot : uniProtRecords.keySet()) {
            List<DBLinkSlimDTO> dbls = context.getGeneByUniprot(uniprot);
            if(CollectionUtils.isEmpty(dbls)) {
                continue;
            }

            String geneID = dbls.get(0).getDataZdbID(); //TODO: should we account for more than 1 gene?

            //at this point, we know that the uniprot is in the load file and has a gene in the DB
            //so we should load any interpros for that gene

            RichSequenceAdapter record = uniProtRecords.get(uniprot);

            for(CrossRefAdapter iplink : record.getCrossRefsByDatabase(dbName.toString())) {
                iplink.getAccession();

                //does it already exist?
                DBLinkSlimDTO interproLink = context.getDbLinkByGeneAndAccession(dbName, geneID, iplink.getAccession());
                boolean alreadyExists = interproLink != null;

                //if not, add it
                if(!alreadyExists) {
                    newActions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.LOAD)
                            .subType(SecondaryTermLoadAction.SubType.DB_LINK)
                            .accession(iplink.getAccession())
                            .dbName(dbName)
                            .geneZdbID(geneID)
                            .handlerClass(this.getClass().getName())
                            .build());
                    newlyAddedCount++;
                } else {
                    previouslyExistedCount++;
                }
            }
        }
        log.debug("Previously existed: " + previouslyExistedCount);
        log.debug("Newly added: " + newlyAddedCount);
        return newActions;
    }

    private static String getDBLinkInfo() {
        //eg. 2023-08-27 Swiss-Prot
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return timestamp + " Swiss-Prot";
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        List<Marker> markers = getMarkerRepository().getMarkersByZdbIDs(actions.stream().map(SecondaryTermLoadAction::getGeneZdbID).toList());
        Map<String, Marker> markerMap = markers.stream().collect(Collectors.toMap(Marker::getZdbID, marker -> marker));
        List<MarkerDBLink> dblinks = new ArrayList<>();

        for(SecondaryTermLoadAction action : actions) {
            log.debug("Loading " + action.getDbName() + " dblink for " + action.getGeneZdbID() + " " + action.getAccession());

            Marker marker = getMarkerRepository().getMarker(action.getGeneZdbID());
            MarkerDBLink newLink = new MarkerDBLink();
            newLink.setAccessionNumber(action.getAccession());
            newLink.setMarker(marker);
            newLink.setReferenceDatabase(SecondaryTermLoadService.getReferenceDatabaseForAction(action));
            newLink.setLength(action.getLength());
            newLink.setLinkInfo(getDBLinkInfo());
            dblinks.add(newLink);
        }
        Publication publication = getPublicationRepository().getPublication(SecondaryTermLoadService.DBLINK_PUBLICATION_ATTRIBUTION_ID);
        getSequenceRepository().addDBLinks(dblinks, publication, 50);
    }

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

}
