package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.MarkerToProteinDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Creates actions for adding and deleting marker to protein information (replaces part of protein_domain_info_load.pl)
 * marker_to_protein table
 * zfinprotein.txt was the legacy load file
 * Uses MarkerToProteinDTO
 *
 */
@Log4j2
public class InterproMarkerToProteinActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<MarkerToProteinDTO> existingRecords = context.getExistingMarkerToProteinRecords();
        List<MarkerToProteinDTO> keepRecords = new ArrayList<>(); //all the records to keep (not delete) includes new records too
        List <SecondaryTermLoadAction> newActions = new ArrayList<>();

        //create new records
        for(String uniprotKey : uniProtRecords.getAccessions()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.getByAccession(uniprotKey);
            List<String> uniprotRecordZdbIDs = context.getGeneZdbIDsByUniprot(uniprotKey);
            for (String zdbID : uniprotRecordZdbIDs) {
                MarkerToProteinDTO newRecord = new MarkerToProteinDTO(zdbID, uniprotKey);

                //skip records that don't exist in ZFIN db_link table
                if (!context.hasUniprotGeneAssociation(uniprotKey, zdbID)) {
                    keepRecords.add(newRecord);
                    continue;
                }

                if (!existingRecords.contains(new MarkerToProteinDTO(zdbID, uniprotKey))) {
                    newActions.add(createLoadAction(newRecord, richSequenceAdapter));
                    keepRecords.add(newRecord);
                } else {
                    keepRecords.add(newRecord);
                }
            }
        }

        //delete records
        for(MarkerToProteinDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                newActions.add(createDeleteAction(existingRecord));
            }
        }
        return newActions;
    }

    private SecondaryTermLoadAction createLoadAction(MarkerToProteinDTO newRecord, RichSequenceAdapter richSequenceAdapter) {
        return SecondaryTermLoadAction.builder()
                .geneZdbID(newRecord.markerZdbID())
                .accession(newRecord.accession())
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN)
                .uniprotAccessions(Set.of(richSequenceAdapter.getAccession()))
                .build();
    }

    private SecondaryTermLoadAction createDeleteAction(MarkerToProteinDTO recordToDelete) {
        return SecondaryTermLoadAction.builder()
                .geneZdbID(recordToDelete.markerZdbID())
                .accession(recordToDelete.accession())
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN)
                .build();
    }
}
