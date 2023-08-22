package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import org.biojavax.bio.seq.RichSequence;
import org.zfin.uniprot.handlers.UniProtLoadHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UniProtLoadPipeline {
    private List<UniProtLoadHandler> handlers = new ArrayList<>();
    private List<UniProtLoadAction> actions = new ArrayList<>();
    private UniProtLoadContext context;

    private Map<String, RichSequence> uniProtRecords;

    public void addHandler(UniProtLoadHandler handler) {
        handlers.add(handler);
    }

    public List<UniProtLoadAction> execute() {
        for (UniProtLoadHandler handler : handlers) {
            handler.handle(uniProtRecords, actions, context);
        }
        return actions;
    }

}
