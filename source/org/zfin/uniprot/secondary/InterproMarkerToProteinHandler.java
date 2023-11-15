package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.MarkerToProteinDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.uniprot.adapter.RichSequenceAdapter.DatabaseSource.ZFIN;


/**
 * Creates actions for adding and deleting marker to protein information (replaces part of protein_domain_info_load.pl)
 * marker_to_protein table
 * zfinprotein.txt was the legacy load file
 * Uses MarkerToProteinDTO
 *
 */
@Log4j2
public class InterproMarkerToProteinHandler implements SecondaryLoadHandler {

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<MarkerToProteinDTO> existingRecords = context.getExistingMarkerToProteinRecords();
        List<MarkerToProteinDTO> keepRecords = new ArrayList<>(); //all the records to keep (not delete) includes new records too

        //create new records
        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            List<String> uniprotRecordZdbIDs = richSequenceAdapter.getCrossRefIDsByDatabase(ZFIN);
            for (String zdbID : uniprotRecordZdbIDs) {
                MarkerToProteinDTO newRecord = new MarkerToProteinDTO(zdbID, uniprotKey);

                //skip records that don't exist in ZFIN db_link table
                if (!context.hasUniprotGeneAssociation(uniprotKey, zdbID)) {
                    keepRecords.add(newRecord);
                    continue;
                }

                if (!existingRecords.contains(new MarkerToProteinDTO(zdbID, uniprotKey))) {
                    actions.add(createLoadAction(newRecord));
                    keepRecords.add(newRecord);
                } else {
                    keepRecords.add(newRecord);
                }
            }
        }

        //delete records
        for(MarkerToProteinDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                actions.add(createDeleteAction(existingRecord));
            }
        }

    }

    private SecondaryTermLoadAction createLoadAction(MarkerToProteinDTO newRecord) {
        return SecondaryTermLoadAction.builder()
                .geneZdbID(newRecord.markerZdbID())
                .accession(newRecord.accession())
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN)
                .handlerClass(this.getClass().getName())
                .build();
    }
    private SecondaryTermLoadAction createDeleteAction(MarkerToProteinDTO recordToDelete) {
        return SecondaryTermLoadAction.builder()
                .geneZdbID(recordToDelete.markerZdbID())
                .accession(recordToDelete.accession())
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN)
                .handlerClass(this.getClass().getName())
                .build();
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        processInserts(actions);
        processDeletes(actions);
    }

    private void processInserts(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            if (action.getType() == SecondaryTermLoadAction.Type.LOAD) {
                log.debug("inserting interpro for marker: " + action.getGeneZdbID() + " interpro: " + action.getAccession());
                getMarkerRepository().insertInterProForMarker(action.getGeneZdbID(), action.getAccession());
            }
        }
    }

    private void processDeletes(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            if (action.getType() == SecondaryTermLoadAction.Type.DELETE) {
                log.debug("deleting interpro for marker: " + action.getGeneZdbID() + " interpro: " + action.getAccession());
                getMarkerRepository().deleteInterProForMarker(action.getGeneZdbID(), action.getAccession());
            }
        }
    }

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN;
    }

}
