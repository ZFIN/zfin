package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

/**
 * Remove from our marker_go_term_evidence table if the new uniprot release no longer contains it.
 * This is related to AddNewSpKeywordTermToGoHandler which handles inserts
 */
@Log4j2
public class RemoveSpKeywordTermToGoActionProcessor implements ActionProcessor {

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions, SecondaryTermLoadAction.Type type) {
        (new RemoveSecondaryTermToGoActionProcessor()).processActions(actions, type);
    }

}
