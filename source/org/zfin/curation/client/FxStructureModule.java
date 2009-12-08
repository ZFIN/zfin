package org.zfin.curation.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.curation.dto.UpdateExpressionDTO;
import org.zfin.framework.presentation.dto.ExpressedTermDTO;
import org.zfin.framework.presentation.dto.ExpressionFigureStageDTO;
import org.zfin.framework.presentation.dto.PileStructureAnnotationDTO;
import org.zfin.framework.presentation.dto.PileStructureDTO;
import org.zfin.framework.presentation.gwtutils.CollectionUtils;
import org.zfin.framework.presentation.gwtutils.StageRangeIntersection;
import org.zfin.framework.presentation.gwtutils.StageRangeUnion;
import org.zfin.framework.presentation.gwtutils.WidgetUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 */
public class FxStructureModule extends Composite implements StructurePile {

    // div-elements
    public static final String SHOW_HIDE_STRUCTURES = "show-hide-structures";
    public static final String UPDATE_EXPERIMENTS_TOP = "update-experiments";
    public static final String UPDATE_EXPERIMENTS_BOTTOM = "update-experiments-bottom";
    public static final String NEW_TERM_SUGGESTION = "new-term-suggestion";
    public static final String STRUCTURES_DISPLAY = "display-structures";
    public static final String IMAGE_LOADING_STRUCTURE_SECTION = "image-loading-structure-section";
    public static final String STRUCTURES_DISPLAY_ERRORS = "display-structure-errors";
    public static final String CONSTRUCTION_ZONE = "structure-pile-construction-zone";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink showStructureSection = new Hyperlink();
    private StructurePileTable displayTable;
    private Image loadingImage = new Image();
    private Label errorMessage = new Label();
    private Button updateButtonAbove = new Button("Update Structures for Expressions");
    private Button updateButtonBelow = new Button("Update Structures for Expressions");
    private StructureAlternateComposite structureSuggestionBox;

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    private List<PileStructureDTO> displayedStructures = new ArrayList<PileStructureDTO>();

    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible;
    // used to control that this module is loaded only once.
    private boolean initialized;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    public static final String HIDE = "hide";
    public static final String SHOW = "show";
    public static final String NOT = "not";

    // injected variables
    private String publicationID;
    private ExpressionSection expressionSection;

    private static final String UNSPECIFIED = "unspecified";

    public FxStructureModule(String publicationID) {
        this.publicationID = publicationID;
        initGUI();
    }

    public void setExpressionSection(ExpressionSection expressionSection) {
        this.expressionSection = expressionSection;
        displayTable.setExpressionSection(expressionSection);
    }

    private void initGUI() {
        initShowHideGUI();

        VerticalPanel panel = new VerticalPanel();
        initUpdateButton(updateButtonAbove);
        panel.add(updateButtonAbove);
        panel.add(new HTML("&nbsp"));
        RootPanel.get(UPDATE_EXPERIMENTS_TOP).add(panel);

        RootPanel.get(STRUCTURES_DISPLAY_ERRORS).add(errorMessage);
        RootPanel.get(IMAGE_LOADING_STRUCTURE_SECTION).add(loadingImage);
        errorMessage.setStyleName(WidgetUtil.ERROR);
        loadingImage.setUrl("/images/ajax-loader.gif");

        structureSuggestionBox = new StructureAlternateComposite();
        RootPanel.get(NEW_TERM_SUGGESTION).add(structureSuggestionBox);
        displayTable = new StructurePileTable(displayedStructures, structureSuggestionBox, errorMessage);
        displayTable.setRemoveStructureCallBack(new RemovePileStructureCallback());
        displayTable.setCreateStructureCallback(new CreatePileStructureCallback());
        displayTable.setPublicationID(publicationID);
        RootPanel.get(STRUCTURES_DISPLAY).add(displayTable);

        initUpdateButton(updateButtonBelow);
        RootPanel.get(UPDATE_EXPERIMENTS_BOTTOM).add(updateButtonBelow);
    }

    private void initUpdateButton(Button button) {
        button.setTitle("Update Structures for Expressions");
        button.addClickHandler(new UpdateStructuresClickListener());
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_STRUCTURES).add(panel);
        Label experimentLabel = new Label("Structures: ");
        experimentLabel.setStyleName("bold");
        panel.add(experimentLabel);
        showStructureSection.setStyleName("small");
        showStructureSection.setText(SHOW);
        showStructureSection.setTargetHistoryToken(SHOW);
        showStructureSection.addClickHandler(new ShowHideStructureSectionListener());
        panel.add(showStructureSection);
    }

    /**
     * The data should only be loaded when the filter bar is initialized.
     * So this method is called from the FxFilterTable.
     */
    public void runModule() {
        if (!initialized) {
            loadSectionVisibility();
            initialized = true;
        }
    }

    /**
     * Check curator session if this section should be displayed or hidden.
     */
    private void loadSectionVisibility() {
        String message = "Error while reading Section Visibility";
        curationRPCAsync.readStructureSectionVisibility(publicationID, new SectionVisibilityCallback(message));
    }

    // Retrieve pile structures from the server
    protected void retrievePileStructures() {
        loadingImage.setVisible(true);
        curationRPCAsync.getStructures(publicationID, new RetrieveStructuresCallback());
    }

    /**
     * 1) Set radio button to 'add' for expressed structures if all checked ones share the structure
     * 2) highlight structures that fall into the intersection of all checked annotations.
     * <p/>
     * Called by the Expression Module.
     *
     * @param figureAnnotations figure annotations
     */
    public void updateFigureAnnotations(List<ExpressionFigureStageDTO> figureAnnotations) {
        //Window.alert("FigureAnnotations: " + figureAnnotations.size());
        if (figureAnnotations == null)
            return;

        List<ExpressedTermDTO> intersectionOfStructures = createIntersectionOfStructures(figureAnnotations);
        selectUnslectStructuresOnPile(intersectionOfStructures);
        StageRangeUnion stageUnion = new StageRangeUnion(figureAnnotations);
        StageRangeIntersection stageIntersection = new StageRangeIntersection(stageUnion.getStart(), stageUnion.getEnd());
        displayTable.markOverlappingStructures(stageIntersection);
    }

    @SuppressWarnings("unchecked")
    private List<ExpressedTermDTO> createIntersectionOfStructures(List<ExpressionFigureStageDTO> figureAnnotations) {
        List<ExpressedTermDTO> intersectionOfStructures = new ArrayList<ExpressedTermDTO>();
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

    /**
     * Select or un-select the structure on the pile.
     *
     * @param expressedTerms list of ExpressedTermDTO
     */
    private void selectUnslectStructuresOnPile(List<ExpressedTermDTO> expressedTerms) {
        if (expressedTerms == null)
            return;

        displayTable.setCommonStructures(expressedTerms);
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
    public void onPileStructureCreation(PileStructureDTO pileStructure) {
        displayedStructures.add(pileStructure);
        Collections.sort(displayedStructures);
        displayTable.createStructureTable();
        updateFigureAnnotations(expressionSection.getSelectedExpressions());
        structureSuggestionBox.setVisible(false);
        clearErrorMessages();
    }

    public void setPileStructureClickListener(ConstructionZone pileStructureClickListener) {
        displayTable.setPileStructureClickListener(pileStructureClickListener);
    }

    /**
     * Check to see if the current structure pile holds a given structure.
     * Can be used for new structure creation to avoid a server request.
     * <p/>
     * It checks if the expressedTermDTO on any in the displayed PileStructureDTO objects.
     * <p/>
     * If no entity provided it returns null;
     *
     * @param expressedTerm pile structure that is checked against the currently displayed structures.
     * @return true or false
     */
    public boolean hasStructureOnPile(ExpressedTermDTO expressedTerm) {
        if (expressedTerm == null)
            return false;

        for (PileStructureDTO structure : displayedStructures) {
            if (expressedTerm.equalsByNameOnly(structure.getExpressedTerm()))
                return true;
        }
        return false;
    }


    // *********************************************************************************************************************

    //      Listener: click, callback, change

    private class RetrieveStructuresCallback extends ZfinAsyncCallback<List<PileStructureDTO>> {

        public RetrieveStructuresCallback() {
            super("Error while reading Structures", errorMessage);
        }

        public void onSuccess(List<PileStructureDTO> list) {

            displayedStructures.clear();
            if (list == null)
                return;

            for (PileStructureDTO structure : list) {
                // do not add 'unspecfied'
                if (!structure.getExpressedTerm().getSupertermName().equals(UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            //Window.alert("SIZE: " + experiments.size());
            displayTable.createStructureTable();
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    /**
     * Show or hide structure pile section.
     */
    private class ShowHideStructureSectionListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            String errorMessage = "Error while trying to save structure visibility";
            if (sectionVisible) {
                // hide section
                showStructureSection(false);
                showStructureSection.setText(SHOW);
                sectionVisible = false;
                curationRPCAsync.setStructureVisibilitySession(publicationID, false,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            } else {
                // display structure pile
                // check if it already exists
                if (displayTable != null && displayTable.getRowCount() > 0) {
                    showStructureSection(true);
                } else {
                    retrievePileStructures();
                }
                showStructureSection.setText(HIDE);
                sectionVisible = true;
                curationRPCAsync.setStructureVisibilitySession(publicationID, true,
                        new VoidAsyncCallback(new Label(errorMessage), loadingImage));
            }
            clearErrorMessages();
        }

    }

    private void showStructureSection(boolean show) {
        if (show) {
            displayTable.setVisible(true);
            updateButtonAbove.setVisible(true);
            updateButtonBelow.setVisible(true);
            RootPanel.get(CONSTRUCTION_ZONE).setVisible(true);
        } else {
            displayTable.setVisible(false);
            updateButtonAbove.setVisible(false);
            updateButtonBelow.setVisible(false);
            RootPanel.get(CONSTRUCTION_ZONE).setVisible(false);
        }

    }

    private List<ExpressionFigureStageDTO> getSelectedExpressions() {
        return expressionSection.getSelectedExpressions();
    }

    /**
     * Remove error messages.
     * Unmark structures in figure annotations.
     */
    public void clearErrorMessages() {
        errorMessage.setText(null);
        expressionSection.markStructuresForDeletion(null, false);
    }


    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisibilityCallback(String message) {
            super(message, FxStructureModule.this.errorMessage);
        }

        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (sectionVisible) {
                if (displayedStructures == null || displayedStructures.size() == 0) {
                    retrievePileStructures();
                } else {
                    displayTable.createStructureTable();
                }
                showStructureSection.setText(HIDE);
                showStructureSection(true);
            } else {
                retrievePileStructures();
                showStructureSection.setText(SHOW);
                showStructureSection(false);
            }
            loadingImage.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class UpdateStructuresClickListener implements ClickHandler {

        public void onClick(ClickEvent widget) {
            //Window.alert("Update Structures");
            UpdateExpressionDTO updateEntity = getSelectedStructures();
            List<ExpressionFigureStageDTO> efs = getSelectedExpressions();
            updateEntity.setFigureAnnotations(efs);
            curationRPCAsync.updateStructuresForExpression(updateEntity, new UpdateExpressionCallback());
        }
    }

    private class UpdateExpressionCallback extends ZfinAsyncCallback<List<ExpressionFigureStageDTO>> {

        UpdateExpressionCallback() {
            super("Error while update Figure Annotations with pile structures ", errorMessage);
        }

        public void onSuccess(List<ExpressionFigureStageDTO> updatedFigureAnnotations) {
            //Window.alert("Success");
            // update the expression list
            expressionSection.postUpdateStructuresOnExpression();
            resetStructureHighlighting();
            loadingImage.setVisible(false);
            clearErrorMessages();
            structureSuggestionBox.setVisible(false);
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }


    /**
     * Undo all 'add', 'remove', 'not' selections and highlighting
     */
    public void resetStructureHighlighting() {
        displayTable.resetActionButtons();
    }


    private UpdateExpressionDTO getSelectedStructures() {
        Set<Integer> keys = displayTable.getDisplayTableMap().keySet();
        UpdateExpressionDTO dto = new UpdateExpressionDTO();
        for (Integer row : keys) {
            RadioButton add = displayTable.getAddRadioButton(row);
            RadioButton remove = displayTable.getRemoveRadioButton(row);
            CheckBox modifier = displayTable.getNotCheckBox(row);
            PileStructureAnnotationDTO psa = new PileStructureAnnotationDTO();
            if (add.getValue() || remove.getValue()) {
                PileStructureDTO term = displayTable.getDisplayTableMap().get(row).copy();
                psa.setExpressedTerm(term.getExpressedTerm());
                psa.setExpressed(!modifier.getValue());
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

    protected class RemovePileStructureCallback extends ZfinAsyncCallback<PileStructureDTO> {

        public RemovePileStructureCallback() {
            super("Error while deleting Figure Annotation", errorMessage);
        }

        public void onSuccess(PileStructureDTO structure) {
            //Window.alert("Success");
            // remove from the dashboard list
            displayedStructures.remove(structure);
            // recreate table to update the correct striping
            displayTable.createStructureTable();
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            loadingImage.setVisible(false);
            clearErrorMessages();
        }

        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    class CreatePileStructureCallback implements AsyncCallback<PileStructureDTO> {

        public void onFailure(Throwable throwable) {
            if (throwable instanceof PileStructureExistsException) {
                errorMessage.setText(throwable.getMessage());
            }
            errorMessage.setText(throwable.getMessage());
        }

        public void onSuccess(PileStructureDTO pileStructure) {
            //Window.alert("Success");
            displayedStructures.add(pileStructure);
            Collections.sort(displayedStructures);
            displayTable.createStructureTable();
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            structureSuggestionBox.setVisible(false);
            clearErrorMessages();
        }
    }


}
