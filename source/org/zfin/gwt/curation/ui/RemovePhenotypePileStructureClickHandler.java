package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.PhenotypePileStructureDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.gwt.root.ui.ErrorHandler;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RemovePhenotypePileStructureClickHandler implements ClickHandler {

    private PhenotypePileStructureDTO structure;
    private ErrorHandler errorMessage;
    private ExpressionSection expressionModule;
    private AsyncCallback callback;

    // RPC class being used for this section.
    private PileStructuresRPCAsync pileStructuresRPCAsync = PileStructuresRPC.App.getInstance();

    public RemovePhenotypePileStructureClickHandler(PhenotypePileStructureDTO structure, ErrorHandler errorMessage, ExpressionSection expressionModule, AsyncCallback callback) {
        this.structure = structure;
        this.errorMessage = errorMessage;
        this.expressionModule = expressionModule;
        this.callback = callback;
    }

    public void onClick(ClickEvent event) {
        PhenotypeTermDTO dto = structure.getPhenotypeTerm();
        if (expressionModule.getExpressedTermDTOs().contains(dto)) {
            Window.alert("Please remove the expression records first");
            errorMessage.setError("Please remove the expression records that use this structure first.");
            expressionModule.markStructuresForDeletion(dto, true);
        } else {
            boolean confirmed = Window.confirm("Do you really want to delete this phenotype from the pile");
            if (confirmed)
                pileStructuresRPCAsync.deletePhenotypeStructure(structure, callback);
        }
    }
}