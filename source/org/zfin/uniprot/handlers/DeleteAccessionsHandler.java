package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Map;
import java.util.Set;

/**
 * This handler calculates all uniprot/gene associations that should be loaded (based on RefSeq)
 * That creates a set of actions for LOAD (call it set A): eg. UP1->Gene1, UP2->Gene2, UP4->Gene4
 * The next step uses the existing uniprot/gene pairs in our DB (set B) to figure out what to remove: eg. UP1->Gene1, UP2->Gene2, UP3->Gene3
 *
 * The end result is a set of actions for DELETE that we should delete from our DB.
 * This would be (set B - set A), or in the examples, the uniprot/gene pair of UP3->Gene3
 *
 */
@Log4j2
public class DeleteAccessionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {

        //combine the logic of the following handlers:
        // MatchOnRefSeqHandler (computes the set of accessions that would be added or lost)
        // ReportLostUniProtsHandler (using the output from first step, reports the accessions that would be lost)
        // ResetLoadActionsHandler (reset the load actions so the rest of the pipeline can run as normal)
        (new MatchOnRefSeqHandler()).handle(uniProtRecords, actions, context);
        (new ReportLostUniProtsHandler()).handle(uniProtRecords, actions, context);
        (new ResetLoadActionsHandler()).handle(uniProtRecords, actions, context);
    }
}
