package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.MarkerToProteinDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Creates actions for adding and deleting marker to protein information (replaces part of protein_domain_info_load.pl)
 */
@Log4j2
public class InterproMarkerToProteinHandler implements SecondaryLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        //TODO: create delete actions and load actions

        List<MarkerToProteinDTO> existingRecords = context.getExistingMarkerToProteinRecords();

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            Collection<CrossRefAdapter> zfinCrossRefs = richSequenceAdapter.getCrossRefsByDatabase("ZFIN");
            if (zfinCrossRefs.isEmpty()) {
                continue;
            }
            List<String> uniprotRecordZdbIDs = zfinCrossRefs.stream().map(ref -> ref.getAccession()).toList();
            for(String zdbID : uniprotRecordZdbIDs) {
                if (!existingRecords.contains(new MarkerToProteinDTO(zdbID, uniprotKey))) {
//                    existingRecordsAsMap.put(zdbID, uniprotKey);
                    actions.add(createLoadAction(zdbID, uniprotKey));
                }
            }
        }

    }

    private SecondaryTermLoadAction createLoadAction(String zdbID, String uniprotAccession) {
        return SecondaryTermLoadAction.builder()
                .geneZdbID(zdbID)
                .accession(uniprotAccession)
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN)
                .build();
    }

}
