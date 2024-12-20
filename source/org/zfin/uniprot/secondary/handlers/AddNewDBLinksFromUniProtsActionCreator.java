package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.zfin.util.ZfinCollectionUtils.firstInEachGrouping;

/**
 * Adds InterPro, PFAM, EC, PROSITE accessions to db_links table.
 * This is based on the entries that appear in the uniprot release file.
 * If the accession is already in the database, it is not added.
 */
@Log4j2
public class AddNewDBLinksFromUniProtsActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

    private final ForeignDB.AvailableName dbName;

    public AddNewDBLinksFromUniProtsActionCreator(ForeignDB.AvailableName dbName) {
        this.dbName = dbName;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        log.info("AddNewDBLinksFromUniProtsActionCreator for " + dbName);
        List<SecondaryTermLoadAction> newActions = getLoadActionsNotAlreadyInDatabase(uniProtRecords, context);

        //remove duplicates
        log.info("Removing duplicates from " + newActions.size() + " new actions");

        //first group by gene, accession
        newActions = firstInEachGrouping(newActions, action -> action.getGeneZdbID() + "," + action.getAccession() + "," + action.getDbName());
        log.info("Removed duplicates, now have " + newActions.size() + " new actions");

        return newActions;
    }

    private List<SecondaryTermLoadAction> getLoadActionsNotAlreadyInDatabase(UniprotReleaseRecords uniProtRecords, SecondaryLoadContext context) {
        //if there is an interpro in the load file, but not in the DB for the corresponding gene, add it.
        // corresponding gene means: get the gene by taking the uniprot from the load file and cross referencing it to loaded uniprots

        int previouslyExistedCount = 0;
        int newlyAddedCount = 0;

        List<SecondaryTermLoadAction> newActions = new java.util.ArrayList<>();
        for(Map.Entry<String, RichSequenceAdapter> uniprotEntry : uniProtRecords.getEntriesByAccession()) {
            RichSequenceAdapter uniprot = uniprotEntry.getValue();
            String uniprotAccession = uniprot.getAccession();

            List<DBLinkSlimDTO> dbls = context.getGenesByUniprot(uniprotAccession);
            if(CollectionUtils.isEmpty(dbls)) {
                continue;
            }

            //iterate over all genes that have this uniprot (usually would be one)
            for(DBLinkSlimDTO dbl : dbls) {
                String geneID = dbl.getDataZdbID();
                if(geneID == null) {
                    continue;
                }

                //at this point, we know that the uniprot is in the load file and has a gene in the DB
                //so we should load any interpros for that gene
                for(String ipAccession : uniprot.getCrossRefIDsByDatabase(dbName.toString())) {

                    //does it already exist?
                    DBLinkSlimDTO interproLink = context.getDbLinkByGeneAndAccession(dbName, geneID, ipAccession);
                    boolean alreadyExists = interproLink != null;

                    //if not, add it
                    if(!alreadyExists) {
                        newActions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.LOAD)
                                .subType(SecondaryTermLoadAction.SubType.DB_LINK)
                                .accession(ipAccession)
                                .dbName(dbName)
                                .geneZdbID(geneID)
                                .uniprotAccessions(Set.of(uniprotAccession))
                                .build());
                        newlyAddedCount++;
                    } else {
                        previouslyExistedCount++;
                    }
                }
            }
        }
        log.info("Previously existed: " + previouslyExistedCount);
        log.info("Newly added: " + newlyAddedCount);
        return newActions;
    }

}
