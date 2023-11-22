package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


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
        log.debug("RemoveFromLostUniProtsActionCreator " + dbName);
        List<SecondaryTermLoadAction> newActions = new ArrayList<>();

        //if there is an interpro (or pfam, or prosite, etc) in the DB, but not in the load file for the corresponding gene, delete it.
        //start by iterating over all the interpros in the DB
        List<DBLinkSlimDTO> iplinks = context.getFlattenedDbLinksByDbName(dbName);
        log.debug("existing iplinks: " + iplinks.size());

        for(DBLinkSlimDTO iplink : iplinks) {

            //find the gene related to this dblink (perhaps an interpro dblink, for example)
            //then find the uniprot for that gene that already exists in our DB
            //then use that uniprot to find the corresponding uniprot record in the load file
            //if it's not there in the load file, delete it from the DB
            DBLinkSlimDTO existingUniprotByGene = context.getUniprotByGene(iplink.getDataZdbID());
            RichSequenceAdapter parsedUniprotRecordFromLoadFile = existingUniprotByGene == null ? null :
                    uniProtRecords.getByAccession(existingUniprotByGene.getAccession());

            if(parsedUniprotRecordFromLoadFile == null) {
                newActions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(isSubTypeHandlerFor())
                        .dbName(this.dbName)
                        .accession(iplink.getAccession())
                        .geneZdbID(iplink.getDataZdbID())
                        .details(
                                existingUniprotByGene == null ? "" :
                                    uniProtRecords.getUniprotFormatByAccession(existingUniprotByGene.getAccession())
                        )
                        .build());

            }
        }
        return newActions;
    }

}
