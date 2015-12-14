package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RadioButton;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.CollectionUtils;
import org.zfin.gwt.root.util.StageRangeIntersectionService;
import org.zfin.gwt.root.util.StageRangeUnion;

import java.util.*;

/**
 * construction zone
 */
public class StructurePilePresenter implements Presenter {

    private StructurePileView view;
    private String publicationID;
    private List<EapQualityTermDTO> fullQualityList = new ArrayList<>();
    private Map<CheckBox, EapQualityTermDTO> checkBoxMap = new HashMap<>();
    private boolean processing = false;

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionPileStructureDTO> displayedStructures = new ArrayList<ExpressionPileStructureDTO>(10);

    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public StructurePilePresenter(StructurePileView view, String publicationID) {
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

    protected void updateStructures() {
        UpdateExpressionDTO updateEntity = getSelectedStructures();
        List<ExpressionFigureStageDTO> efs = view.getStructurePileTable().getExpressionZoneView().getSelectedExpressions();
        updateEntity.setFigureAnnotations(efs);
        curationRPCAsync.updateStructuresForExpression(updateEntity, new UpdateExpressionCallback());

    }

    private UpdateExpressionDTO getSelectedStructures() {
        Set<Integer> keys = view.getStructurePileTable().getDisplayTableMap().keySet();
        UpdateExpressionDTO dto = new UpdateExpressionDTO();
        for (Integer row : keys) {
            RadioButton add = view.getStructurePileTable().getAddRadioButton(row);
            RadioButton remove = view.getStructurePileTable().getRemoveRadioButton(row);
            PileStructureAnnotationDTO psa = new PileStructureAnnotationDTO();
            if (add.getValue() || remove.getValue()) {
                ExpressionPileStructureDTO term = view.getStructurePileTable().getDisplayTableMap().get(row).copy();
                psa.setExpressedTerm(term.getExpressedTerm());
                psa.setZdbID(term.getZdbID());
            }
            if (add.getValue())
                psa.setAction(PileStructureAnnotationDTO.Action.ADD);
            else if (remove.getValue())
                psa.setAction(PileStructureAnnotationDTO.Action.REMOVE);
            if (add.getValue() || remove.getValue())
                dto.addPileStructureAnnotationDTO(psa);
        }
        return dto;
    }

    public void updateFigureAnnotations(List<ExpressionFigureStageDTO> selectedExpressions) {
        if (selectedExpressions == null)
            return;

        List<ExpressedTermDTO> intersectionOfStructures = createIntersectionOfStructures(selectedExpressions);
        selectUnselectStructuresOnPile(intersectionOfStructures);
        StageRangeUnion stageUnion = new StageRangeUnion(selectedExpressions);
        StageRangeIntersectionService stageIntersection = new StageRangeIntersectionService(selectedExpressions);
        view.getStructurePileTable().markOverlappingStructures(stageIntersection);

    }

    /**
     * Select or un-select the structure on the pile.
     *
     * @param expressedTerms list of ExpressedTermDTO
     */
    private void selectUnselectStructuresOnPile(List<ExpressedTermDTO> expressedTerms) {
        if (expressedTerms == null)
            return;

        view.getStructurePileTable().setCommonStructures(expressedTerms);
    }


    private List<ExpressedTermDTO> createIntersectionOfStructures(Collection<ExpressionFigureStageDTO> figureAnnotations) {
        List<ExpressedTermDTO> intersectionOfStructures = new ArrayList<ExpressedTermDTO>(figureAnnotations.size());
        int index = 0;
        for (ExpressionFigureStageDTO figureAnnotation : figureAnnotations) {
            if (index == 0)
                intersectionOfStructures.addAll(figureAnnotation.getExpressedTerms());
            else {
                intersectionOfStructures = (List<ExpressedTermDTO>) CollectionUtils.intersection(intersectionOfStructures, figureAnnotation.getExpressedTerms());
            }
            index++;
        }
        return intersectionOfStructures;
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

    private class UpdateExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        UpdateExpressionCallback() {
            super("Error while update Figure Annotations with pile structures ", view.getErrorElement());
        }

        public void onSuccess(List<ExpressionFigureStageDTO> updatedFigureAnnotations) {
            //Window.alert("Success");
            // update the expression list
            CreateExpressionEvent event = new CreateExpressionEvent();
            AppUtils.EVENT_BUS.fireEvent(event);
            view.getStructurePileTable().resetActionButtons();
            view.getLoadingImage().setVisible(false);
            clearErrorMessages();
            view.alternateStructurePanel.setVisible(false);
        }

        public void onFailureCleanup() {
            view.getLoadingImage().setVisible(true);
        }
    }
}
