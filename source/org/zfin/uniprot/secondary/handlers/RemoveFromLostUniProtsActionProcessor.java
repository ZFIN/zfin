package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.DBLink;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.uniprot.secondary.SecondaryTermLoadService.getReferenceDatabaseIDForAction;


/**
 * Remove from db_link table if the existing db_link table contains an accession, but the new uniprot release
 * no longer has that same accession (InterPro, EC, PROSITE, PFAM)
 * This is related to the AddNewFromUniProtsHandler, but handles deletes instead of inserts
 */
@Log4j2
public class RemoveFromLostUniProtsActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        List<DBLink> dblinksToDelete = new ArrayList<>();
        for(SecondaryTermLoadAction action : actions) {
            DBLink dblink = getSequenceRepository().getDBLinkByReferenceDatabaseID(action.getGeneZdbID(), action.getAccession(), getReferenceDatabaseIDForAction(action));
            if (dblink == null) {
                log.error("Error deleting dblink (none found by attributes): " + action.getGeneZdbID() + " " + action.getAccession() + " " + action.getDbName() + " " + action.getSubType() + " " + getReferenceDatabaseIDForAction(action));
                continue;
            }
            log.info("Removing dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
            getSequenceRepository().deleteReferenceProteinByDBLinkID(dblink.getZdbID());
            dblinksToDelete.add(dblink);
        }
        getSequenceRepository().removeDBLinks(dblinksToDelete);
    }
}
