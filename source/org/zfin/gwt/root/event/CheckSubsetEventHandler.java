package org.zfin.gwt.root.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

/**
 *
 */
public class CheckSubsetEventHandler implements EventHandler {

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    private AsyncCallback<Boolean> callBack;

    public CheckSubsetEventHandler(AsyncCallback<Boolean> callBack) {
        this.callBack = callBack;
    }

    public void onEvent(String termName) {
        lookupRPC.isTermRelationalQuality(termName, callBack);
    }

}
