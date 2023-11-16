package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.ProteinToInterproDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;

/**
 * Creates actions for adding and deleting protein to interpro information (replaces part of protein_domain_info_load.pl)
 * protein_to_interpro table (select pti_uniprot_id, pti_interpro_id from protein_to_interpro)
 * unipro2interpro.txt was the legacy load file
 * Uses ProteinToInterproDTO
 */
@Log4j2
public class ProteinToInterproActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN_TO_INTERPRO;
    }


    @Override
    public List<SecondaryTermLoadAction> createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<ProteinToInterproDTO> existingRecords = context.getExistingProteinToInterproRecords();
        List<ProteinToInterproDTO> keepRecords = new java.util.ArrayList<>(); //all the records to keep (not delete) includes new records too
        List<SecondaryTermLoadAction> newActions = new java.util.ArrayList<>();

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            List<String> zdbIDs = richSequenceAdapter.getCrossRefIDsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
            if (!context.hasAnyUniprotGeneAssociation(uniprotKey, zdbIDs)) {
                continue;
            }

            List<String> iprs = richSequenceAdapter.getCrossRefIDsByDatabase(RichSequenceAdapter.DatabaseSource.INTERPRO);

            for(String ipr : iprs) {
                ProteinToInterproDTO newRecord = new ProteinToInterproDTO(uniprotKey, ipr);
                if (!existingRecords.contains(newRecord)) {
                    newActions.add(createLoadAction(newRecord));
                }
                keepRecords.add(newRecord);
            }
        }

        for(ProteinToInterproDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                newActions.add(createDeleteAction(existingRecord));
            }
        }
        return newActions;
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
