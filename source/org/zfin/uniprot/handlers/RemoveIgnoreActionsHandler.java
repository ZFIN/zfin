package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Log4j2
public class RemoveIgnoreActionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        for(UniProtLoadAction action : new HashSet<>(actions)) {
            if(action.getType().equals(UniProtLoadAction.Type.IGNORE)) {
                actions.remove(action);
            }
        }
    }
}
