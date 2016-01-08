package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RadioButton;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.ExpressionPileStructureDTO;
import org.zfin.gwt.root.dto.PileStructureAnnotationDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.CollectionUtils;
import org.zfin.gwt.root.util.StageRangeIntersectionService;

import java.util.*;

/**
 * Structure pile
 */
public class StructurePilePresenter implements Presenter {

    private StructurePileView view;
    private String publicationID;
    private static final String UNSPECIFIED = "unspecified";
    // selected records in the expression zone.
    // need to keep track of them as we have to re-fresh the pile while
    // keeping the info about selections...
    private List<ExpressionFigureStageDTO> selectedExpressions;

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<ExpressionPileStructureDTO> displayedStructures = new ArrayList<>(10);

    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();

    public StructurePilePresenter(StructurePileView view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
        view.getStructurePileTable().setPublicationID(publicationID);
    }

    public void bind() {
        view.getStructurePileTable().setRemoveStructureCallBack(new RemovePileStructureCallback());
        view.getReCreatePile().addClickHandler(new CreateExpressionPileHandler());
        //view.getReCreatePile().setVisible(false);

        addDynamicClickHandler();
    }

    private void addDynamicClickHandler() {
        view.structurePileTable.setCreateStructureCallback(new CreatePileStructureCallback());
    }

    @Override
    public void go() {
        loadSectionVisibility();
        retrieveStructurePile();
        bind();
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationRPCAsync.readStructureSectionVisibility(publicationID, false, new SectionVisibilityCallback(message));
        curationRPCAsync.isReCreatePhenotypePileLinkNeeded(publicationID, new PileReCreationNeedCallback("Error while reading pile-recreation need"));
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

    public void onPileStructureCreation(List<ExpressionPileStructureDTO> pileStructure) {
        if (pileStructure == null)
            return;
        for (ExpressionPileStructureDTO dto : pileStructure)
            onPileStructureCreation(dto);
        refreshFigureAnnotations();
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

    protected void updateStructures() {
        UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateEntity = getSelectedStructures();
/* implementation to be done. For now the server is checking that EaPs cannot be added to wildtype / standard fish...
        if (addEapToWildType(updateEntity)) {
            setError("Cannot add an EaP to a wildtype fish");
            view.getLoadingImage().setVisible(false);
            return;
        }
*/
        List<ExpressionFigureStageDTO> efs = view.getStructurePileTable().getExpressionZoneView().getSelectedExpressions();
        updateEntity.setFigureAnnotations(efs);
        curationRPCAsync.updateStructuresForExpression(updateEntity, new UpdateExpressionCallback());

    }

    private boolean addEapToWildType(UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateEntity) {
        if (updateEntity == null)
            return false;
        for (PileStructureAnnotationDTO dto : updateEntity.getStructures()) {
            if (dto.getAction() == PileStructureAnnotationDTO.Action.ADD) {
                for (ExpressionFigureStageDTO figureStage : updateEntity.getFigureAnnotations()) {
                    if (figureStage.getExperiment().isWildtype())
                        return true;
                }
            }
        }

        return false;
    }

    private UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> getSelectedStructures() {
        Set<Integer> keys = view.getStructurePileTable().getDisplayTableMap().keySet();
        UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> dto = new UpdateExpressionDTO<>();
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

    /**
     * Inject the selected expression records in the expression section to
     * bold-face and set the configuration in the table of the pile.
     *
     * @param selectedExpressions list of ExpressionFigureStageDTO
     */
    public void updateFigureAnnotations(List<ExpressionFigureStageDTO> selectedExpressions) {
        if (selectedExpressions == null)
            return;
        this.selectedExpressions = selectedExpressions;
        refreshFigureAnnotations();
    }

    public void refreshFigureAnnotations() {
        if (selectedExpressions == null)
            return;

        List<ExpressedTermDTO> intersectionOfStructures = createIntersectionOfStructures(selectedExpressions);
        selectUnselectStructuresOnPile(intersectionOfStructures);
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
        List<ExpressedTermDTO> intersectionOfStructures = new ArrayList<>(figureAnnotations.size());
        int index = 0;
        for (ExpressionFigureStageDTO figureAnnotation : figureAnnotations) {
            if (index == 0)
                intersectionOfStructures.addAll(figureAnnotation.getUniqueExpressedTerms());
            else {
                intersectionOfStructures = (List<ExpressedTermDTO>) CollectionUtils.intersection(intersectionOfStructures, figureAnnotation.getUniqueExpressedTerms());
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
            refreshFigureAnnotations();
            view.loadingImage.setVisible(false);
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            ///   loadingImage.setVisible(true);
        }
    }

    class CreatePileStructureCallback implements AsyncCallback<ExpressionPileStructureDTO> {

        public void onFailure(Throwable throwable) {
            if (throwable instanceof PileStructureExistsException) {
                view.errorElement.setError(throwable.getMessage());
            }
            view.errorElement.setError(throwable.getMessage());
        }

        public void onSuccess(ExpressionPileStructureDTO pileStructure) {
            //Window.alert("Success");
            displayedStructures.add(pileStructure);
            Collections.sort(displayedStructures);
            view.getStructurePileTable().createStructureTable();
////            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            view.alternateStructurePanel.setVisible(false);
            clearErrorMessages();
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
            view.getLoadingImage().setVisible(false);
        }
    }

    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisibilityCallback(String message) {
            super(message, view.errorElement);
        }

        public void onSuccess(Boolean visible) {
            view.getStructurePile().setVisible(visible);
            view.getLoadingImage().setVisible(false);
        }

        public void onFailureCleanup() {
            view.getLoadingImage().setVisible(true);
        }
    }

    private class PileReCreationNeedCallback extends ZfinAsyncCallback<Boolean> {

        public PileReCreationNeedCallback(String message) {
            super(message, view.getErrorElement());
        }

        @Override
        public void onSuccess(Boolean isReCreatePileNeed) {
            //Window.alert("Show: " + sectionVisible);
            if (isReCreatePileNeed)
                view.getReCreatePile().setVisible(true);
            else
                view.getReCreatePile().setVisible(false);
        }

        @Override
        public void onFailureCleanup() {

        }
    }

    private class CreateExpressionPileHandler implements ClickHandler {

        public void onClick(ClickEvent clickEvent) {
            view.getLoadingImage().setVisible(true);
            pileStructureRPCAsync.recreateExpressionStructurePile(publicationID, new RetrieveExpressionPileCallback());
        }
    }

    private class RetrieveExpressionPileCallback extends ZfinAsyncCallback<List<ExpressionPileStructureDTO>> {

        public RetrieveExpressionPileCallback() {
            super("Error while reading Structures", view.errorElement);
        }

        @Override
        public void onSuccess(List<ExpressionPileStructureDTO> list) {
            //Window.alert("List: " + list.size());
            displayedStructures.clear();
            if (list == null)
                return;

            for (ExpressionPileStructureDTO structure : list) {
                // do not add 'unspecified'
                if (!structure.getExpressedTerm().getEntity().getSuperTerm().getTermName().equals(UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            view.getReCreatePile().setVisible(false);
            //Window.alert("SIZE: " + list.size());
            view.getStructurePileTable().createStructureTable();
////            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            view.getLoadingImage().setVisible(false);
        }

        @Override
        public void onFailureCleanup() {
            view.getLoadingImage().setVisible(true);
        }
    }

}
