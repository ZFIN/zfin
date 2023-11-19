package org.zfin.uniprot.secondary.handlers;

import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.Map;
import java.util.List;

public interface ActionCreator {

    /**
     * Create actions for adding and deleting secondary term information
     * @param uniProtRecords map of uniprot records parsed from uniprot release file keyed by uniprot id
     * @param actions add new actions to this list (logic may be based on existing entries in the actions list)
     * @param context context for the load (existing database records, downloaded files, translation files, etc.)
     */
    List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context);

    /**
     * Sanity check to make sure the actions are for the correct sub-type
     * This returns the the subtype that the given handler is meant to process
     */
    SecondaryTermLoadAction.SubType isSubTypeHandlerFor();

}
