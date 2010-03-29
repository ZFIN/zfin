package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.ExpressionPileStructureDTO;
import org.zfin.gwt.root.ui.ErrorHandler;

/**
 * GWT class to handle a deletion of an expression term on the pile.
 */
public class RemoveExpressionPileStructureClickHandler implements ClickHandler {

    private ExpressionPileStructureDTO structure;
    private ErrorHandler errorHandler;
    private ExpressionSection expressionModule;
    private AsyncCallback callback;

    // RPC class being used for this section.
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();

    public RemoveExpressionPileStructureClickHandler(ExpressionPileStructureDTO structure, ErrorHandler errorMessage, ExpressionSection expressionModule, AsyncCallback callback) {
        this.structure = structure;
        this.errorHandler = errorMessage;
        this.expressionModule = expressionModule;
        this.callback = callback;
    }

    public void onClick(ClickEvent event) {
        ExpressedTermDTO dto = structure.getExpressedTerm();
        if (expressionModule.getExpressedTermDTOs().contains(dto)) {
            //Window.alert("Please remove the expression records first");
            errorHandler.setError("Please remove the expression records that use this structure first.");
            expressionModule.markStructuresForDeletion(dto, true);
        } else {
            boolean confirmed = Window.confirm("Do you really want to delete this structure from the pile");
            if (confirmed)
                pileStructureRPCAsync.deleteStructure(structure, callback);
        }
    }
}

