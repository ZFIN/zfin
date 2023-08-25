package org.zfin.uniprot.handlers;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.List;
import java.util.Map;

public interface UniProtLoadHandler {
    void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<UniProtLoadAction> actions, UniProtLoadContext context);
}
