package org.zfin.uniprot;

import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.handlers.UniProtLoadHandler;

import java.util.List;
import java.util.Map;

public class ResetLoadActionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<UniProtLoadAction> actions, UniProtLoadContext context) {
        //iterate over actions
        //if the action is not a match, then remove it
        actions.removeIf(action -> action.getType().equals(UniProtLoadAction.Type.LOAD));
    }
}
