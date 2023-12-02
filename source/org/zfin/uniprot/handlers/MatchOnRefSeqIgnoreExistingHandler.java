package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.ResetLoadActionsHandler;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Log4j2
public class MatchOnRefSeqIgnoreExistingHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {

        //combine the logic of the following handlers:
        // MatchOnRefSeqHandler (computes the set of accessions that would be added or lost)
        // IgnoreAccessionsAlreadyInDatabaseHandler (using the output from first step, removes any redundant additions)
        Set<UniProtLoadAction> newActions = new TreeSet<>();
        (new MatchOnRefSeqHandler()).handle(uniProtRecords, newActions, context);
        (new IgnoreLoadActionsAlreadyInDatabaseHandler()).handle(uniProtRecords, newActions, context);
        actions.addAll(newActions);
    }
}
