package org.zfin.uniprot.secondary;

import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Map;
import java.util.List;

public interface SecondaryLoadHandler {
    void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context);
}
