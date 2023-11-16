package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


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
    public List<SecondaryTermLoadAction> createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        //if there is an interpro in the DB, but not in the load file for the corresponding gene, delete it.
        // corresponding gene means: get the gene by taking the uniprot from the load file and cross referencing it to loaded uniprots (via this load pub?)
        List<DBLinkSlimDTO> iplinks = context.getDbLinksByDbName(this.dbName).values().stream().flatMap(Collection::stream).toList();
        List<SecondaryTermLoadAction> newActions = new ArrayList<>();

        for(DBLinkSlimDTO iplink : iplinks) {
            DBLinkSlimDTO uniprot = context.getUniprotByGene(iplink.getDataZdbID());
            if(uniprot == null) {
                newActions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(isSubTypeHandlerFor())
                        .dbName(this.dbName)
                        .accession(iplink.getAccession())
                        .geneZdbID(iplink.getDataZdbID())
                        .build());
            }
        }
        return newActions;
    }

}
