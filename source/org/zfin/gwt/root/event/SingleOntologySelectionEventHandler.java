package org.zfin.gwt.root.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

/**
 *
 */
public class SingleOntologySelectionEventHandler implements EventHandler {

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    private AsyncCallback<OntologyDTO> callBack;

    public SingleOntologySelectionEventHandler(AsyncCallback<OntologyDTO> callBack) {
        this.callBack = callBack;
    }

    public void onEvent(String termID) {
        lookupRPC.getOntology(termID, callBack);
    }

}
