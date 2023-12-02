package org.zfin.uniprot;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.handlers.UniProtLoadHandler;

import java.util.*;

@Log4j2
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
        int actionCount = 0;
        for (UniProtLoadHandler handler : handlers) {
            log.debug("Starting action creation handler " + handler.getClass().getName());
            handler.handle(uniProtRecords, actions, context);
            log.debug("Finished action creation handler " + handler.getClass().getName());
            log.debug("This handler created " + (actions.size() - actionCount) + " new actions");
            actionCount = actions.size();
            log.debug("Contains X1WGG3 - ZDB-GENE-060503-858?");
            actions.stream()
                    .filter(action -> action.getAccession().equals("X1WGG3"))
                    .forEach(action -> log.debug(action));
            log.debug("====================================");
        }
        return actions;
    }

}
