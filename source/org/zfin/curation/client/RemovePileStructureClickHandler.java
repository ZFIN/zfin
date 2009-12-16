package org.zfin.curation.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import org.zfin.framework.presentation.dto.PileStructureDTO;
import org.zfin.framework.presentation.dto.ExpressedTermDTO;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RemovePileStructureClickHandler implements ClickHandler {

    private PileStructureDTO structure;
    private Label errorMessage = new Label();
    private ExpressionSection expressionModule;
    private AsyncCallback callback;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public RemovePileStructureClickHandler(PileStructureDTO structure, Label errorMessage, ExpressionSection expressionModule, AsyncCallback callback) {
        this.structure = structure;
        this.errorMessage = errorMessage;
        this.expressionModule = expressionModule;
        this.callback = callback;
    }

    public void onClick(ClickEvent event) {
        ExpressedTermDTO dto = structure.getExpressedTerm();
        if (expressionModule.getExpressedTermDTOs().contains(dto)) {
            //Window.alert("Please remove the expression records first");
            errorMessage.setText("Please remove the expression records that use this structure first.");
            expressionModule.markStructuresForDeletion(dto, true);
        } else {
            boolean confirmed = Window.confirm("Do you really want to delete this structure form the pile");
            if (confirmed)
                curationRPCAsync.deleteStructure(structure, callback);
        }
    }
}

