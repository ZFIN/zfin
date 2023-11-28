package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This handler is used to filter down the number of UniProt records to consider for loading.
 * If the UniProt record is already in the database, it is removed from the uniProtRecords map.
 * This makes it so that the rest of the pipeline only considers UniProt records that are not already in the database.
 *
 */
@Log4j2
public class IgnoreAccessionsAlreadyInDatabaseHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        log.info("Uniprot Accessions in Load File: " + uniProtRecords.size());
        log.info("Uniprot Accessions in DB: " + context.getUniprotDbLinks().size());

        Set<String> accessionsInUniprotLoadFile = uniProtRecords.keySet();
        Set<String> uniprotAccessionsInDB = context.getUniprotDbLinks().keySet();

        Set<String> accessionsInDBButNotInLoadFile = new HashSet<>(uniprotAccessionsInDB);
        accessionsInDBButNotInLoadFile.removeAll(accessionsInUniprotLoadFile);
        log.info("Accessions in DB but not in Load File: " + accessionsInDBButNotInLoadFile.size());

        Set<String> accessionsInLoadFileButNotInDB = new HashSet<>(accessionsInUniprotLoadFile);
        accessionsInLoadFileButNotInDB.removeAll(uniprotAccessionsInDB);
        log.info("Accessions in Load File but not in DB: " + accessionsInLoadFileButNotInDB.size());

        for (String accession : uniprotAccessionsInDB) {
            uniProtRecords.remove(accession);
        }
        log.info("Uniprot Accessions in Load File after removing those already in DB: " + uniProtRecords.size());
    }
}
