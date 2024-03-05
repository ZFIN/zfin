package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.uniprot.persistence.BatchInserter;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;

import static org.zfin.framework.HibernateUtil.currentSession;
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
    public void processActions(List<SecondaryTermLoadAction> actions, SecondaryTermLoadAction.Type type) {
        if (type == SecondaryTermLoadAction.Type.LOAD) {
            processInserts(actions);
        } else if (type == SecondaryTermLoadAction.Type.DELETE) {
            processDeletes(actions);
        }
        currentSession().flush();
    }

    private void processInserts(List<SecondaryTermLoadAction> actions) {
        List<Tuple2<String, String>> insertionRows = actions.stream()
                .map(action -> new Tuple2<>(action.getGeneZdbID(), action.getAccession()))
                .toList();
        processInsertsFromTuples(insertionRows);
    }
    public void processInsertsFromTuples(List<Tuple2<String, String>> actions) {
        List<Map<String, Object>> insertionRows = actions.stream()
                .map(action -> Map.of(
                        "mtp_mrkr_zdb_id", (Object)action.v1(),
                        "mtp_uniprot_id", action.v2()))
                .toList();
        BatchInserter batchInserter = new BatchInserter("marker_to_protein", insertionRows);
        batchInserter.execute();
    }


    private void processDeletes(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            getMarkerRepository().deleteInterProForMarker(action.getGeneZdbID(), action.getAccession());
        }
    }

}
