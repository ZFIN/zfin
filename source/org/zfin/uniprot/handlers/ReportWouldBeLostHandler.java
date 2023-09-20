package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.ResetLoadActionsHandler;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Map;
import java.util.Set;

@Log4j2
public class ReportWouldBeLostHandler implements UniProtLoadHandler {
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
