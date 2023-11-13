package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.MarkerToProteinDTO;
import org.zfin.uniprot.interpro.PdbDTO;
import org.zfin.uniprot.interpro.ProteinToInterproDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Creates actions for adding and deleting PDB information (replaces part of protein_domain_info_load.pl)
 *  protein_to_pdb table (select ptp_uniprot_id, ptp_pdb_id from protein_to_pdb)
 * unipro2pdb.txt was the legacy load file
 * uses PdbDTO
 */
@Log4j2
public class PDBHandler implements SecondaryLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<PdbDTO> existingRecords = context.getExistingPdbRecords();
        List<MarkerToProteinDTO> keepRecords = new ArrayList<>(); //all the records to keep (not delete) includes new records too

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            Collection<CrossRefAdapter> zfinCrossRefs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
            if (zfinCrossRefs.isEmpty()) {
                continue;
            }
            List<String> pdbs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.PDB)
                    .stream()
                    .map(ref -> ref.getAccession()).toList();

            for(String pdb : pdbs) {
                PdbDTO newRecord = new PdbDTO(uniprotKey, pdb);
                if (!existingRecords.contains(newRecord)) {
                    actions.add(createLoadAction(newRecord));
                }
                keepRecords.add(new MarkerToProteinDTO(uniprotKey, pdb));
            }
        }

        for(PdbDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                actions.add(createDeleteAction(existingRecord));
            }
        }

    }

    private SecondaryTermLoadAction createLoadAction(PdbDTO newRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PDB)
                .relatedEntityFields(newRecord.toMap())
                .build();
    }

    private SecondaryTermLoadAction createDeleteAction(PdbDTO dbRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PDB)
                .relatedEntityFields(dbRecord.toMap())
                .build();
    }

}
