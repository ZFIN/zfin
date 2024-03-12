package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

/**
 * Adds new SPKW terms to marker_go_term_evidence table.
 * Special case of AddNewSecondaryTermToGoHandler.
 */
@Log4j2
public class AddNewSpKeywordTermToGoActionProcessor implements ActionProcessor {

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions, SecondaryTermLoadAction.Type type) {
        (new MarkerGoTermEvidenceActionProcessor()).processActions(actions, type);
    }
}
