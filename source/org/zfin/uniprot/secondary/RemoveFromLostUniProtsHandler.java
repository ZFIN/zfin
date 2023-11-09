package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
public class RemoveFromLostUniProtsHandler implements SecondaryLoadHandler {
    private final ForeignDB.AvailableName dbName;

    public RemoveFromLostUniProtsHandler(ForeignDB.AvailableName dbName) {
        this.dbName = dbName;
    }

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        //if there is an interpro in the DB, but not in the load file for the corresponding gene, delete it.
        // corresponding gene means: get the gene by taking the uniprot from the load file and cross referencing it to loaded uniprots (via this load pub?)
        List<DBLinkSlimDTO> iplinks = context.getDbLinksByDbName(this.dbName).values().stream().flatMap(Collection::stream).toList();

        for(DBLinkSlimDTO iplink : iplinks) {
            DBLinkSlimDTO uniprot = context.getUniprotByGene(iplink.getDataZdbID());
            if(uniprot == null) {
                actions.add(SecondaryTermLoadAction.builder().type(SecondaryTermLoadAction.Type.DELETE)
                        .subType(SecondaryTermLoadAction.SubType.DB_LINK)
                        .dbName(this.dbName)
                        .accession(iplink.getAccession())
                        .geneZdbID(iplink.getDataZdbID())
                        .build());
            }
        }
    }
}
