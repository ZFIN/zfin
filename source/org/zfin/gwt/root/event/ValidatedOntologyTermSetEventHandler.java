package org.zfin.gwt.root.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

/**
 *
 */
public class ValidatedOntologyTermSetEventHandler implements EventHandler {

    private LookupComposite lookupComposite;

    public ValidatedOntologyTermSetEventHandler(LookupComposite lookupComposite) {
        this.lookupComposite = lookupComposite;
    }

    public void onEvent() {
        lookupComposite.unsetUnValidatedTextMarkup();
    }

}
