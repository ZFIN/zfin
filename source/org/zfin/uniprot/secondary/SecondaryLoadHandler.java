package org.zfin.uniprot.secondary;

import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Map;
import java.util.List;

public interface SecondaryLoadHandler {

    /**
     * Create actions for adding and deleting secondary term information
     * @param uniProtRecords map of uniprot records parsed from uniprot release file keyed by uniprot id
     * @param actions add new actions to this list (logic may be based on existing entries in the actions list)
     * @param context context for the load (existing database records, downloaded files, translation files, etc.)
     */
    void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context);

    /**
     * After all the actions have been created, the actions are processed.
     * This is where the database is updated.
     * @param actions
     */
    void processActions(List<SecondaryTermLoadAction> actions);

    /**
     * Sanity check to make sure the actions are for the correct sub-type
     * This returns the the subtype that the given handler is meant to process
     */
    SecondaryTermLoadAction.SubType isSubTypeHandlerFor();

}
