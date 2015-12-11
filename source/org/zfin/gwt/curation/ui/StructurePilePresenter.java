package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.CheckBox;
import org.zfin.gwt.root.dto.EapQualityTermDTO;
import org.zfin.gwt.root.dto.ExpressionPileStructureDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.*;

/**
 * construction zone
 */
public class StructurePilePresenter implements Presenter {

    private StructurePileModule view;
    private String publicationID;
    private List<EapQualityTermDTO> fullQualityList = new ArrayList<>();
    private Map<CheckBox, EapQualityTermDTO> checkBoxMap = new HashMap<>();
    private boolean processing = false;

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionPileStructureDTO> displayedStructures = new ArrayList<ExpressionPileStructureDTO>(10);

    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public StructurePilePresenter(StructurePileModule view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
    }

    public void bind() {
        view.getStructurePileTable().setRemoveStructureCallBack(new RemovePileStructureCallback());
        addDynamicClickHandler();
    }

    private void addDynamicClickHandler() {

    }

    @Override
    public void go() {
        retrieveStructurePile();
        bind();
    }

    protected void retrieveStructurePile() {
        // list of Eap qualities
        curationRPCAsync.getStructures(publicationID, new RetrieveStructuresCallback());
    }

    public void setError(String message) {
        view.getErrorElement().setText(message);
    }

    public void clearErrorMessages() {
        view.getErrorElement().setError("");
    }

    private void resetUI() {
        view.getErrorElement().clearAllErrors();
        clearErrorMessages();
    }

    public void submitStructure() {
        // expect only 1-2 checked normally
    }

    public void onPileStructureCreation(List<ExpressionPileStructureDTO> pileStructure) {
        if (pileStructure == null)
            return;
        for (ExpressionPileStructureDTO dto : pileStructure)
            onPileStructureCreation(dto);
    }

    /**
     * Method being called upon successful creation of a new pile structure:
     * 1) Add new structure to pile
     * 2) resort the pile
     * 3) re-recreate the display table
     * 4) update figure annotations
     *
     * @param pileStructure PileStructureDTO
     */
    public void onPileStructureCreation(ExpressionPileStructureDTO pileStructure) {
        displayedStructures.add(pileStructure);
        Collections.sort(displayedStructures);
        view.getStructurePileTable().createStructureTable();
        view.getAlternateStructurePanel().setVisible(false);
        clearErrorMessages();
    }

    public void reloadPile() {

    }

    private class RetrieveStructuresCallback extends ZfinAsyncCallback<List<ExpressionPileStructureDTO>> {

        private static final String UNSPECIFIED = "unspecified";

        public RetrieveStructuresCallback() {
            super("Error while reading Structures", view.getErrorElement());
        }

        @Override
        public void onSuccess(List<ExpressionPileStructureDTO> list) {
            displayedStructures.clear();
            if (list == null)
                return;

            for (ExpressionPileStructureDTO structure : list) {
                // do not add 'unspecified'
                if (!structure.getExpressedTerm().getEntity().getSuperTerm().getTermName().equals(UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            //         reCreateStructurePileLink.setVisible(false);
            //Window.alert("SIZE: " + list.size());
            view.getStructurePileTable().createStructureTable(displayedStructures);
            //       updateFigureAnnotations(expressionSection.getSelectedExpressions());
            //     loadingImage.setVisible(false);
        }

        @Override
        public void onFailureCleanup() {
            ///        loadingImage.setVisible(true);
        }
    }

    private class RemovePileStructureCallback extends ZfinAsyncCallback<ExpressionPileStructureDTO> {

        public RemovePileStructureCallback() {
            super("Error while deleting Figure Annotation", view.getErrorElement());
        }

        public void onSuccess(ExpressionPileStructureDTO structure) {
            //Window.alert("Success");
            // remove from the dashboard list
            displayedStructures.remove(structure);
            // recreate table to update the correct striping
            view.getStructurePileTable().removeStructure(structure);
            //updateFigureAnnotations(expressionSection.getSelectedExpressions());
            ///loadingImage.setVisible(false);
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            ///   loadingImage.setVisible(true);
        }
    }

}
