package org.zfin.uniprot.secondary;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.*;


//TODO: we should move the logic for processing actions into this class
@Getter
@Setter
@Log4j2
public class SecondaryTermLoadPipeline {
    private List<SecondaryLoadHandler> handlers = new ArrayList<>();
    private List<SecondaryTermLoadAction> actions = new ArrayList<>();
    private SecondaryLoadContext context;

    private Map<String, RichSequenceAdapter> uniprotRecords;

    public void addHandler(SecondaryLoadHandler handler) {
        handlers.add(handler);
    }

    public List<SecondaryTermLoadAction> execute() {
        int i = 1;
        int actionCount = 0;
        int previousActionCount = 0;

        for (SecondaryLoadHandler handler : handlers) {
            String handlerClassName = handler.getClass().getName();
            log.debug("Starting handler " + i + " of " + handlers.size() + " (" + handlerClassName + ")");
            handler.createActions(uniprotRecords, actions, context);
            actionCount = actions.size();
            log.debug("Finished handler " + i + " of " + handlers.size() + " (" + handlerClassName + ")");

            if (actionCount == previousActionCount) {
                log.debug("No new actions were created by this handler");
            } else {
                log.debug("This handler created " + (actionCount - previousActionCount) + " new actions");
            }
            previousActionCount = actionCount;
            i++;
        }
        return actions;
    }

}
