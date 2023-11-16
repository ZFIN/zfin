package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;


/**
 * Creates actions for adding and deleting marker to protein information (replaces part of protein_domain_info_load.pl)
 * marker_to_protein table
 * zfinprotein.txt was the legacy load file
 * Uses MarkerToProteinDTO
 *
 */
@Log4j2
public class InterproMarkerToProteinActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.INTERPRO_MARKER_TO_PROTEIN;
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

}
