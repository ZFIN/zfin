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
 * Creates actions for adding and deleting marker to protein information (replaces part of protein_domain_info_load.pl)
 */
@Log4j2
public class ProteinToInterproHandler implements SecondaryLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        //TODO: create delete actions
        List<ProteinToInterproDTO> existingRecords = context.getExistingProteinToInterproRecords();

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
            }
        }

    }

    private SecondaryTermLoadAction createLoadAction(ProteinToInterproDTO newRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_TO_INTERPRO)
                .relatedEntityFields(newRecord.toMap())
                .build();
    }

}
