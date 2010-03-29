package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.PhenotypeFigureStageDTO;
import org.zfin.gwt.root.dto.PhenotypePileStructureDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.gwt.root.dto.PileStructureAnnotationDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 */
public abstract class AbstractStructureModule extends Composite implements StructurePile<PhenotypeTermDTO, PhenotypeFigureStageDTO, PhenotypePileStructureDTO> {

    // div-elements
    public static final String SHOW_HIDE_STRUCTURES = "show-hide-structures";
    public static final String NEW_TERM_SUGGESTION = "new-term-suggestion";
    public static final String STRUCTURES_DISPLAY = "display-structures";
    public static final String IMAGE_LOADING_STRUCTURE_SECTION = "image-loading-structure-section";
    public static final String STRUCTURES_DISPLAY_ERRORS = "display-structure-errors";
    public static final String CONSTRUCTION_ZONE = "structure-pile-construction-zone";
    public static final String UPDATE_MUTANTS_TOP = "update-experiments";
    public static final String UPDATE_MUTANTS_BOTTOM = "update-experiments-bottom";
    public static final String CHECK_SIZE_BAR = "structures-check-size-bar";

    // GUI elements
    // this panel holds the Title and the show / hide link
    private HorizontalPanel titlePanel = new HorizontalPanel();
    private Hyperlink showStructureSection = new Hyperlink();
    protected PhenotypeStructurePileTable displayTable;
    private Image loadingImage = new Image();
    private ErrorHandler errorElement = new SimpleErrorElement(STRUCTURES_DISPLAY_ERRORS);
    private Button updateButtonAbove = new Button("Update Phenotypes for Mutants");
    private Button updateButtonBelow = new Button("Update Phenotypes for Mutants");
    private Hyperlink reCreatePhenotypePileLink = new Hyperlink("Re-Create Phenotype Pile", "re-create-pile");

    // all expressions displayed on the page (all or a subset defined by the filter elements)
    protected List<PhenotypePileStructureDTO> displayedStructures = new ArrayList<PhenotypePileStructureDTO>(15);

    // flag that indicates if the experiment section is visible or not.
    private boolean sectionVisible;
    // used to control that this module is loaded only once.
    private boolean initialized;

    // RPC class being used for this section.
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();
    private CurationPhenotypeRPCAsync phenotypeRPCAsync = CurationPhenotypeRPC.App.getInstance();
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();

    public static final String HIDE = "hide";
    public static final String SHOW = "show";
    public static final String NOT = "not";

    // injected variables
    protected String publicationID;
    protected ExpressionSection<PhenotypeTermDTO, PhenotypeFigureStageDTO> expressionSection;

    private static final String UNSPECIFIED = "unspecified";

    public AbstractStructureModule(String publicationID) {
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
        RootPanel.get(UPDATE_MUTANTS_TOP).add(panel);
        RootPanel.get(UPDATE_MUTANTS_BOTTOM).add(updateButtonBelow);

        RootPanel.get(IMAGE_LOADING_STRUCTURE_SECTION).add(loadingImage);
        loadingImage.setUrl("/images/ajax-loader.gif");

        displayTable = new PhenotypeStructurePileTable(displayedStructures, null, errorElement);
        displayTable.setRemoveStructureCallBack(new RemovePhenotypePileStructureCallback());
        displayTable.setCreateStructureCallback(new CreatePileStructureCallback());
        displayTable.setPublicationID(publicationID);
        RootPanel.get(STRUCTURES_DISPLAY).add(displayTable);
        initUpdateButton(updateButtonBelow);

        initSizePanel();
    }

    private void initUpdateButton(Button button) {
        button.setTitle("Update Phenotype for Mutants");
        button.addClickHandler(new UpdateStructuresClickListener());
    }

    private void initSizePanel() {
        Widget boxCheckAndSize = new BoxCheckAndSize(false, RootPanel.get(STRUCTURES_DISPLAY), publicationID);
        boxCheckAndSize.setWidth("100%"); 
        boxCheckAndSize.setStyleName("yourinputwelcome");
        RootPanel.get(CHECK_SIZE_BAR).add(boxCheckAndSize);
    }

    private void initShowHideGUI() {
        RootPanel.get(SHOW_HIDE_STRUCTURES).add(titlePanel);
        Widget phenotypesLabel = new Label("Phenotypes: ");
        phenotypesLabel.setStyleName("bold");
        titlePanel.add(phenotypesLabel);
        showStructureSection.setStyleName("small");
        showStructureSection.setText(SHOW);
        showStructureSection.setTargetHistoryToken(SHOW);
        showStructureSection.addClickHandler(new ShowHideStructureSectionListener());
        //panel.add(showStructureSection);
        reCreatePhenotypePileLink.addClickHandler(new CreatePhenotypePileHandler(reCreatePhenotypePileLink));
        reCreatePhenotypePileLink.setVisible(false);
        titlePanel.add(WidgetUtil.getNbsp());
        titlePanel.add(reCreatePhenotypePileLink);
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
        phenotypeRPCAsync.isReCreatePhenotypePileLinkNeeded(publicationID, new ReCreatePileNeedCallback("Error while reading pile-recreation need"));
    }

    // Retrieve pile structures from the server

    protected abstract void retrievePileStructures();

    /**
     * Select or un-select the structure on the pile.
     *
     * @param phenotypeTerms list of PhenotypeTermDTO
     */
    protected void selectUnSelectStructuresOnPile(List<PhenotypeTermDTO> phenotypeTerms) {
        if (phenotypeTerms == null)
            return;

        displayTable.setCommonStructures(phenotypeTerms);
    }

    /**
     * Method being called upon successful creation of a new pile structure:
     * 1) Add new structure to pile
     * 2) re-sort the pile
     * 3) re-recreate the display table
     * 4) update figure annotations
     *
     * @param pileStructure PileStructureDTO
     */
    public void onPileStructureCreation(PhenotypePileStructureDTO pileStructure) {
        displayedStructures.add(pileStructure);
        Collections.sort(displayedStructures);
        displayTable.createStructureTable();
        updateFigureAnnotations(expressionSection.getSelectedExpressions());
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
     * @param phenotypeTerm pile structure that is checked against the currently displayed structures.
     * @return true or false
     */
    public boolean hasStructureOnPile(PhenotypeTermDTO phenotypeTerm) {
        if (phenotypeTerm == null)
            return false;

        for (PhenotypePileStructureDTO structure : displayedStructures) {
            if (phenotypeTerm.equalsByNameOnly(structure.getPhenotypeTerm()))
                return true;
        }
        return false;
    }


    // *********************************************************************************************************************

    //      Listener: click, callback, change

    private class RetrieveStructuresCallback extends ZfinAsyncCallback<List<PhenotypePileStructureDTO>> {

        public RetrieveStructuresCallback() {
            super("Error while reading Structures", errorElement);
        }

        @Override
        public void onSuccess(List<PhenotypePileStructureDTO> list) {

            displayedStructures.clear();
            if (list == null)
                return;

            for (PhenotypePileStructureDTO structure : list) {
                // do not add 'unspecified'
                if (!structure.getExpressedTerm().getSuperterm().getTermName().equals(UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            //Window.alert("SIZE: " + experiments.size());
            displayTable.createStructureTable();
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            loadingImage.setVisible(false);
        }

        @Override
        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class RetrievePhenotypeStructuresCallback extends ZfinAsyncCallback<List<PhenotypePileStructureDTO>> {

        public RetrievePhenotypeStructuresCallback() {
            super("Error while reading Structures", errorElement);
        }

        @Override
        public void onSuccess(List<PhenotypePileStructureDTO> list) {
            //Window.alert("List: " + list.size());
            displayedStructures.clear();
            if (list == null)
                return;

            for (PhenotypePileStructureDTO structure : list) {
                // do not add 'unspecified'
                if (!structure.getPhenotypeTerm().getSuperterm().getTermName().equals(UNSPECIFIED))
                    displayedStructures.add(structure);
            }
            reCreatePhenotypePileLink.setVisible(false);
            //Window.alert("SIZE: " + list.size());
            displayTable.createStructureTable();
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            loadingImage.setVisible(false);
        }

        @Override
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
                        new VoidAsyncCallback(errorMessage, errorElement, IMAGE_LOADING_STRUCTURE_SECTION));
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
                        new VoidAsyncCallback(errorMessage, errorElement, IMAGE_LOADING_STRUCTURE_SECTION));
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

    /**
     * Remove error messages.
     * Un-mark structures in figure annotations.
     */
    public void clearErrorMessages() {
        errorElement.clearAllErrors();
        expressionSection.markStructuresForDeletion(null, false);
    }


    private class SectionVisibilityCallback extends ZfinAsyncCallback<Boolean> {
        public SectionVisibilityCallback(String message) {
            super(message, AbstractStructureModule.this.errorElement);
        }

        @Override
        public void onSuccess(Boolean visible) {
            sectionVisible = visible;
            //Window.alert("Show: " + sectionVisible);
            if (sectionVisible) {
                if (displayedStructures == null || displayedStructures.isEmpty()) {
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

        @Override
        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class ReCreatePileNeedCallback extends ZfinAsyncCallback<Boolean> {

        public ReCreatePileNeedCallback(String message) {
            super(message, errorElement);
        }

        @Override
        public void onSuccess(Boolean isReCreatePileNeed) {
            //Window.alert("Show: " + sectionVisible);
            if (isReCreatePileNeed)
                reCreatePhenotypePileLink.setVisible(true);
            else
                reCreatePhenotypePileLink.setVisible(false);
        }

        @Override
        public void onFailureCleanup() {
            
        }
    }

    private class UpdateStructuresClickListener implements ClickHandler {

        public void onClick(ClickEvent widget) {
            //Window.alert("Update Structures");
            UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> updateEntity = getSelectedStructures();
            List<PhenotypeFigureStageDTO> efs = getSelectedMutants();
            updateEntity.setFigureAnnotations(efs);
            phenotypeRPCAsync.updateStructuresForExpression(updateEntity, new UpdateExpressionCallback());
        }

        private UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> getSelectedStructures() {
            Set<Integer> keys = displayTable.getDisplayTableMap().keySet();
            UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> dto = new UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO>();
            for (Integer row : keys) {
                RadioButton add = displayTable.getAddRadioButton(row);
                RadioButton remove = displayTable.getRemoveRadioButton(row);
                PileStructureAnnotationDTO psa = new PileStructureAnnotationDTO();
                if (add.getValue() || remove.getValue()) {
                    PhenotypePileStructureDTO term = displayTable.getDisplayTableMap().get(row).copy();
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

        private List<PhenotypeFigureStageDTO> getSelectedMutants() {
            return expressionSection.getSelectedExpressions();
        }

    }

    private class UpdateExpressionCallback extends ZfinAsyncCallback<List<PhenotypeFigureStageDTO>> {

        UpdateExpressionCallback() {
            super("Error while update Figure Annotations with pile structures ", errorElement);
        }

        @Override
        public void onSuccess(List<PhenotypeFigureStageDTO> updatedFigureAnnotations) {
            //Window.alert("Success");
            // update the expression list
            expressionSection.postUpdateStructuresOnExpression();
            resetStructureHighlighting();
            loadingImage.setVisible(false);
            clearErrorMessages();
        }

        @Override
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


    private class RemovePhenotypePileStructureCallback extends ZfinAsyncCallback<PhenotypePileStructureDTO> {

        public RemovePhenotypePileStructureCallback() {
            super("Error while deleting Figure Annotation", errorElement);
        }

        @Override
        public void onSuccess(PhenotypePileStructureDTO structure) {
            //Window.alert("Success");
            // remove from the dashboard list
            displayedStructures.remove(structure);
            // recreate table to update the correct striping
            displayTable.createStructureTable();
            //updateFigureAnnotations(expressionSection.getSelectedMutants());
            loadingImage.setVisible(false);
            clearErrorMessages();
        }

        @Override
        public void onFailureCleanup() {
            loadingImage.setVisible(true);
        }
    }

    private class CreatePileStructureCallback implements AsyncCallback<PhenotypePileStructureDTO> {

        public void onFailure(Throwable throwable) {
            errorElement.setError(throwable.getMessage());
        }

        public void onSuccess(PhenotypePileStructureDTO pileStructure) {
            Window.alert("Success");
            displayedStructures.add(pileStructure);
            Collections.sort(displayedStructures);
            displayTable.createStructureTable();
            updateFigureAnnotations(expressionSection.getSelectedExpressions());
            clearErrorMessages();
        }
    }


    private class CreatePhenotypePileHandler implements ClickHandler {

        private Hyperlink phenotypePile;

        public CreatePhenotypePileHandler(Hyperlink createPhenotypePile) {
            phenotypePile = createPhenotypePile;
        }

        public void onClick(ClickEvent clickEvent) {
            loadingImage.setVisible(true);
            pileStructureRPCAsync.recreatePhenotypeStructurePile(publicationID, new RetrievePhenotypeStructuresCallback());
        }
    }

}