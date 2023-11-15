package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;
import static org.zfin.uniprot.secondary.SecondaryTermLoadService.getReferenceDatabaseIDForAction;


/**
 * Remove from db_link table if the existing db_link table contains an accession, but the new uniprot release
 * no longer has that same accession (InterPro, EC, PROSITE, PFAM)
 * This is related to the AddNewFromUniProtsHandler, but handles deletes instead of inserts
 */
@Log4j2
public class RemoveFromLostUniProtsHandler implements SecondaryLoadHandler {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

    private final ForeignDB.AvailableName dbName;

    public RemoveFromLostUniProtsHandler() {
        this.dbName = null;
    }

    public RemoveFromLostUniProtsHandler(ForeignDB.AvailableName dbName) {
        this.dbName = dbName;
    }

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        //if there is an interpro in the DB, but not in the load file for the corresponding gene, delete it.
        // corresponding gene means: get the gene by taking the uniprot from the load file and cross referencing it to loaded uniprots (via this load pub?)
        List<DBLinkSlimDTO> iplinks = context.getDbLinksByDbName(this.dbName).values().stream().flatMap(Collection::stream).toList();

        for(DBLinkSlimDTO iplink : iplinks) {
            DBLinkSlimDTO uniprot = context.getUniprotByGene(iplink.getDataZdbID());
            if(uniprot == null) {
                actions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(isSubTypeHandlerFor())
                        .dbName(this.dbName)
                        .accession(iplink.getAccession())
                        .geneZdbID(iplink.getDataZdbID())
                        .handlerClass(this.getClass().getName())
                        .build());
            }
        }
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
            System.err.println("Removing dblink: " + dblink.getZdbID() + " " + dblink.getAccessionNumber() + " " + action.getGeneZdbID());
            log.debug("Removing dblink: " + dblink.getZdbID());
            getSequenceRepository().deleteReferenceProteinByDBLinkID(dblink.getZdbID());
            dblinksToDelete.add(dblink);
        }
        getSequenceRepository().removeDBLinks(dblinksToDelete);
    }
}
