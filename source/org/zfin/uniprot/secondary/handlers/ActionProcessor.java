package org.zfin.uniprot.secondary.handlers;

import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

public interface ActionProcessor {

    /**
     * After all the actions have been created, the actions are processed.
     * This is where the database is updated.
     * @param actions list of actions to process
     */
    void processActions(List<SecondaryTermLoadAction> actions);

    /**
     * Sanity check to make sure the actions are for the correct sub-type
     * This returns the the subtype that the given handler is meant to process
     */
    SecondaryTermLoadAction.SubType isSubTypeHandlerFor();

}
