package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;
import org.zfin.uniprot.secondary.SecondaryTermLoadService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Adds InterPro, PFAM, EC, PROSITE accessions to db_links table.
 * This is based on the entries that appear in the uniprot release file.
 * If the accession is already in the database, it is not added.
 */
@Log4j2
public class AddNewDBLinksFromUniProtsActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

    private static String getDBLinkInfo() {
        //eg. 2023-08-27 Swiss-Prot
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return timestamp + " Swiss-Prot";
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
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

}
