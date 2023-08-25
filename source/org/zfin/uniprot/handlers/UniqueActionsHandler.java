package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Log4j2
public class UniqueActionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        Map<String, UniProtLoadAction> uniqueActions = new HashMap<>();

        Iterator<UniProtLoadAction> iterator = actions.iterator();
        while (iterator.hasNext()) {
            UniProtLoadAction action = iterator.next();
            if (uniqueActions.containsKey(action.getAccession())) {
                UniProtLoadAction existingAction = uniqueActions.get(action.getAccession());
                iterator.remove();
            } else {
                uniqueActions.put(action.getAccession(), action);
            }
        }
    }
}
