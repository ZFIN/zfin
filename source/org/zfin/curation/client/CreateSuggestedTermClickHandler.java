package org.zfin.curation.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.framework.presentation.dto.ExpressedTermDTO;

/**
 * Click Handler for suggested alternate terms that match a given stage range.
 */
public class CreateSuggestedTermClickHandler implements ClickHandler {

    private ExpressedTermDTO expressedTerm;
    private String publicationID;
    private AsyncCallback callback;
    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public CreateSuggestedTermClickHandler(ExpressedTermDTO expressedTerm, String publicationID, AsyncCallback callback) {
        this.expressedTerm = expressedTerm;
        this.publicationID = publicationID;
        this.callback = callback;
    }

    public void onClick(ClickEvent event) {
        //Window.alert("Create new post-composed term: " + expressedTerm.getDisplayName());
        curationRPCAsync.createPileStructure(expressedTerm, publicationID, callback);
    }
}
