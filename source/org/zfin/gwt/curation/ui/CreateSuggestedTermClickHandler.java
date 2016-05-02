package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.ExpressedTermDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Click Handler for suggested alternate terms that match a given stage range.
 */
public class CreateSuggestedTermClickHandler implements ClickHandler {

    private ExpressedTermDTO expressedTerm;
    private String publicationID;
    private AsyncCallback callback;
    // RPC class being used for this section.
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();

    public CreateSuggestedTermClickHandler(ExpressedTermDTO expressedTerm, String publicationID, AsyncCallback callback) {
        this.expressedTerm = expressedTerm;
        this.publicationID = publicationID;
        this.callback = callback;
    }

    public void onClick(ClickEvent event) {
        //Window.alert("Create new post-composed term: " + expressedTerm.getDisplayName());
        List<ExpressedTermDTO> dtoList = new ArrayList<>(1);
        dtoList.add(expressedTerm);
        pileStructureRPCAsync.createPileStructure(dtoList, publicationID, callback);
    }
}
