package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.MarkerToProteinDTO;
import org.zfin.uniprot.interpro.ProteinToInterproDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Creates actions for adding and deleting protein to interpro information (replaces part of protein_domain_info_load.pl)
 * protein_to_interpro table (select pti_uniprot_id, pti_interpro_id from protein_to_interpro)
 * unipro2interpro.txt was the legacy load file
 * Uses ProteinToInterproDTO
 */
@Log4j2
public class ProteinToInterproHandler implements SecondaryLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<ProteinToInterproDTO> existingRecords = context.getExistingProteinToInterproRecords();
        List<ProteinToInterproDTO> keepRecords = new java.util.ArrayList<>(); //all the records to keep (not delete) includes new records too

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            Collection<CrossRefAdapter> zfinCrossRefs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
            if (zfinCrossRefs.isEmpty()) {
                continue;
            }
            List<String> iprs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.INTERPRO)
                    .stream()
                    .map(ref -> ref.getAccession()).toList();

            for(String ipr : iprs) {
                ProteinToInterproDTO newRecord = new ProteinToInterproDTO(uniprotKey, ipr);
                if (!existingRecords.contains(newRecord)) {
                    actions.add(createLoadAction(newRecord));
                }
                keepRecords.add(newRecord);
            }
        }

        for(ProteinToInterproDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                actions.add(createDeleteAction(existingRecord));
            }
        }

    }

    private SecondaryTermLoadAction createDeleteAction(ProteinToInterproDTO existingRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_TO_INTERPRO)
                .relatedEntityFields(existingRecord.toMap())
                .build();
    }

    private SecondaryTermLoadAction createLoadAction(ProteinToInterproDTO newRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_TO_INTERPRO)
                .relatedEntityFields(newRecord.toMap())
                .build();
    }

}
