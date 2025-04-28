package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

/**
 * This handler is used to filter down the number of UniProt records to consider for loading.
 * If the UniProt record is already in the database, it is removed from the uniProtRecords map.
 * This makes it so that the rest of the pipeline only considers UniProt records that are not already in the database.
 *
 */
@Log4j2
public class IgnoreLoadActionsAlreadyInDatabaseHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        Iterator<UniProtLoadAction> iter = actions.iterator();
        while (iter.hasNext()) {
            UniProtLoadAction action = iter.next();
            DBLinkSlimDTO existingRecord = context.getDBLinkByUniprotAndGene(action.getAccession(), action.getGeneZdbID());
            if (existingRecord != null) {
                if (existingRecord.getPublicationIDs().contains(AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS)) {
                    iter.remove();
                } else {
                    if (existingRecord.getPublicationIDs().size() == 0) {
                        log.info("Keeping load action for " + action.getAccession() + " because it is already in the database, but has no attributions.");
                    } else {
                        log.info("Adding attribution for " + action.getAccession() + " to " + existingRecord.getPublicationIDs());
                    }
                    action.setSubType(UniProtLoadAction.SubType.ADD_ATTRIBUTION);
                }
            }
        }
    }
}
