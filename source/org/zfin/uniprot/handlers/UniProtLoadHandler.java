package org.zfin.uniprot.handlers;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;

import java.util.List;
import java.util.Map;

public interface UniProtLoadHandler {
    void handle(Map<String, RichSequence> uniProtRecords, List<UniProtLoadAction> actions, UniProtLoadContext context);
}
