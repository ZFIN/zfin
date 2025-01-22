package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;


/**
 * Remove from db_link table if the existing db_link table contains an accession, but the new uniprot release
 * no longer has that same accession (InterPro, EC, PROSITE, PFAM)
 * This is related to the AddNewFromUniProtsHandler, but handles deletes instead of inserts
 */
@Log4j2
public class RemoveFromLostUniProtsActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.DB_LINK;
    }

    private final ForeignDB.AvailableName dbName;

    public RemoveFromLostUniProtsActionCreator(ForeignDB.AvailableName dbName) {
        this.dbName = dbName;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        log.info("RemoveFromLostUniProtsActionCreator " + dbName);
        List<SecondaryTermLoadAction> newActions = new ArrayList<>();

        //if there is an interpro (or pfam, or prosite, etc) in the DB, but not in the load file for the corresponding gene, delete it.
        //start by iterating over all the interpros in the DB
        List<DBLinkSlimDTO> iplinks = context.getFlattenedDbLinksByDbName(dbName);
        log.info("existing iplinks: " + iplinks.size());

        for(DBLinkSlimDTO iplink : iplinks) {

            //find the gene related to this dblink (perhaps an interpro dblink, for example)
            //then find the uniprot for that gene that already exists in our DB
            //then use that uniprot to find the corresponding uniprot record in the load file
            //if it's not there in the load file, delete it from the DB
            List<DBLinkSlimDTO> existingUniprotsByGene = context.getUniprotsByGene(iplink.getDataZdbID());

            List<RichSequenceAdapter> parsedUniprotRecordsFromLoadFile = existingUniprotsByGene.isEmpty() ? emptyList() :
                    existingUniprotsByGene.stream()
                            .map(uniprot -> uniProtRecords.getByAccession(uniprot.getAccession()))
                            .filter(Objects::nonNull)
                            .toList();

            if(parsedUniprotRecordsFromLoadFile.isEmpty()) {
                newActions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(isSubTypeHandlerFor())
                        .dbName(this.dbName)
                        .accession(iplink.getAccession())
                        .geneZdbID(iplink.getDataZdbID())
                        .uniprotAccessions(existingUniprotsByGene.stream().map(DBLinkSlimDTO::getAccession).collect(Collectors.toSet()))
                        .build());
            }
        }
        return newActions;
    }

}
