package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.handlers.UniProtLoadHandler;

import java.util.*;

@Getter
@Setter
public class UniProtLoadPipeline {
    private List<UniProtLoadHandler> handlers = new ArrayList<>();
    private Set<UniProtLoadAction> actions = new TreeSet<>();
    private UniProtLoadContext context;

    private Map<String, RichSequenceAdapter> uniProtRecords;

    public void addHandler(UniProtLoadHandler handler) {
        handlers.add(handler);
    }

    public Set<UniProtLoadAction> execute() {
        for (UniProtLoadHandler handler : handlers) {
            handler.handle(uniProtRecords, actions, context);
        }
        return actions;
    }

}
