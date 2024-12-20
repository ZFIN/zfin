package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.PdbDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Creates actions for adding and deleting PDB information (replaces part of protein_domain_info_load.pl)
 *  protein_to_pdb table (select ptp_uniprot_id, ptp_pdb_id from protein_to_pdb)
 * unipro2pdb.txt was the legacy load file
 * uses PdbDTO
 */
@Log4j2
public class PDBActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PDB;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<PdbDTO> existingRecords = (context.getExistingPdbRecords() != null) ? context.getExistingPdbRecords() : new ArrayList<>();
        List<PdbDTO> keepRecords = new ArrayList<>(); //all the records to keep (not delete) includes new records too
        List<SecondaryTermLoadAction> newActions = new ArrayList<>();

        for(String uniprotKey : uniProtRecords.getAccessions()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.getByAccession(uniprotKey);

            if (!isUniprotRecordLinkedToZFINGene(richSequenceAdapter)) continue;

            List<String> pdbs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.PDB)
                    .stream()
                    .map(ref -> ref.getAccession()).toList();

            for(String pdb : pdbs) {
                PdbDTO newRecord = new PdbDTO(uniprotKey, pdb);
                if (!existingRecords.contains(newRecord)) {
                    newActions.add(createLoadAction(newRecord, richSequenceAdapter));
                }
                keepRecords.add(new PdbDTO(uniprotKey, pdb));
            }
        }

        for(PdbDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                newActions.add(createDeleteAction(existingRecord));
            }
        }

        return newActions;
    }

    private static boolean isUniprotRecordLinkedToZFINGene(RichSequenceAdapter richSequenceAdapter) {
        Collection<CrossRefAdapter> zfinCrossRefs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
        if (zfinCrossRefs.isEmpty()) {
            return false;
        }
        return true;
    }

    private SecondaryTermLoadAction createLoadAction(PdbDTO newRecord, RichSequenceAdapter richSequenceAdapter) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PDB)
                .relatedEntityFields(newRecord.toMap())
                .uniprotAccessions(Set.of(richSequenceAdapter.getAccession()))
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
