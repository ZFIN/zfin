package org.zfin.uniprot;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.handlers.UniProtLoadHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ResetLoadActionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequence> uniProtRecords, List<UniProtLoadAction> actions, UniProtLoadContext context) {
        //iterate over actions
        Iterator<UniProtLoadAction> iter = actions.iterator();
        while(iter.hasNext()) {
            UniProtLoadAction action = iter.next();
            //if the action is not a match, then remove it
            if(action.getType().equals(UniProtLoadAction.Type.LOAD)) {
                iter.remove();
            }
        }
    }
}
