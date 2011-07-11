package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.event.CheckSubsetEventHandler;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.*;

import java.util.*;

/**
 * This class is responsible for the construction zone that creates post-composed structures.
 * Initially, it's used for FX but should be extended to also create annotations for PATO.
 * <p/>
 * Requirements:<br/>
 * A) Subterm Field: A subterm field in which either AO or GO terms are auto-completed depending on the ontology selector.
 * This field is optional.<br/>
 * B) Superterm Field: A superterm field in which an AO term is completed.<br/>
 * C) Swap Terms: Swapping terms will exchange the superterm's and the subterm's entry. If the subterm is a GO term no swapping is
 * possible as the superterm has to be an AO term.<br/>
 * D) Reset Button: Clicking this button will empty both term fields and set the subterm ontology to AO.
 * <p/>
 * Usage:<br/>
 * 1) Constructor with publication ID and termEntryMap that initializes the term parts being used and the Ontologies
 * used for each term.<br/>
 * 2) inject PileStructure object which is called after successful addition of new post-composed term.<br/>
 * 3) inject StructureValidator that validates combinations of post-composed terms.
 */
public class PileConstructionZoneModule extends Composite implements ConstructionZone {

    // div-elements
    public static final String STRUCTURE_PILE_CONSTRUCTION_ZONE = "structure-pile-construction-zone";
    public static final String SUBMIT_RESET = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-submit-reset";
    public static final String SWAP_TERMS = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-swap-terms";
    public static final String SWAP_RELATED_TERMS = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-swap-related-terms";
    public static final String ERRORS = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-errors";
    public static final String TERMINFO = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-terminfo";
    public static final String TAG = "tag";
    public static final String RELATED_TERMS_PANEL = "related-terms-panel";

    // GUI elements
    private Map<EntityPart, TermEntry> termEntryUnitsMap = new HashMap<EntityPart, TermEntry>(5);
    private Button swapTermsButton = new Button("Swap Terms &uarr;&darr;");
    private Button swapRelatedTermsButton = new Button("Swap Related Terms &uarr;&darr;");
    private Button submitButton = new Button("Add");
    private Button resetButton = new Button("Reset");
    private SimpleErrorElement errorElement = new SimpleErrorElement(ERRORS);
    private TermInfoComposite termInfoTable;
    private ZfinListBox tagList;
    private Label historyLabelTermInfo = new Label();
    private boolean isQualityRelational;

    private Map<EntityPart, List<OntologyDTO>> termEntryMap;
    private Collection<TermEntry> termEntryUnits = new ArrayList<TermEntry>(3);

    private Collection<PileStructureListener> pileListener = new ArrayList<PileStructureListener>(2);

    // injected variables
    private StructurePile structurePile;
    private StructureValidator structureValidator;
    private String publicationID;

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();

    public PileConstructionZoneModule(String publicationID, Map<EntityPart, List<OntologyDTO>> termEntryMap) {
        this.publicationID = publicationID;
        this.termEntryMap = termEntryMap;
        initGUI();
    }

    private void initGUI() {
        RootPanel.get(SWAP_TERMS).add(swapTermsButton);
        if (RootPanel.get(SWAP_RELATED_TERMS) != null)
            RootPanel.get(SWAP_RELATED_TERMS).add(swapRelatedTermsButton);
        HorizontalPanel submitResetPanel = new HorizontalPanel();
        submitButton.addClickHandler(new AddNewStructureClickListener());
        submitResetPanel.add(submitButton);
        submitResetPanel.add(resetButton);
        RootPanel.get(SUBMIT_RESET).add(submitResetPanel);
        termInfoTable = new TermInfoComposite();
        createTermEntryUnits();
        termInfoTable.addErrorHandler(errorElement);
        VerticalPanel termInfoPanel = new VerticalPanel();
        termInfoPanel.add(historyLabelTermInfo);
        termInfoPanel.add(termInfoTable);
        RootPanel.get(TERMINFO).add(termInfoPanel);
        addClickListener();
        // set related panel invisible if it exists
        setVisibilityForRelatedEntityPanel(false);
    }

    private void setVisibilityForRelatedEntityPanel(boolean visibility) {
        if (RootPanel.get(RELATED_TERMS_PANEL) != null) {
            RootPanel.get(RELATED_TERMS_PANEL).setVisible(visibility);
            // remove any entry in case the related entity is
            if (!visibility) {
                getTermEntry(EntityPart.RELATED_ENTITY_SUPERTERM).reset();
                getTermEntry(EntityPart.RELATED_ENTITY_SUBTERM).reset();
            }
            isQualityRelational = visibility;
        }
    }

    private void createTermEntryUnits() {
        for (Map.Entry<EntityPart, List<OntologyDTO>> postComposedEntry : termEntryMap.entrySet()) {
            List<OntologyDTO> ontologies = postComposedEntry.getValue();
            TermEntry termEntry = new TermEntry(ontologies, postComposedEntry.getKey(), termInfoTable);
            termEntry.getCopyFromTerminfoToTextButton().addClickHandler(
                    new CopyTermToEntryFieldClickListener(termEntry));
            String divName = getDivName(postComposedEntry.getKey());
            termEntry.addOnFocusHandler(new OnAutcompleteFocusHandler(termEntry));
            termEntryUnits.add(termEntry);
            termEntryUnitsMap.put(postComposedEntry.getKey(), termEntry);
            RootPanel.get(divName).add(termEntry);
        }
        // add dependency handler to super and sub term changes
        if (termEntryMap.containsKey(EntityPart.QUALITY)) {
            TermEntry qualityTermEntry = getTermEntry(EntityPart.QUALITY);
            qualityTermEntry.setSubsetCheckHandler(new CheckSubsetEventHandler(new RelatedQualityCheckCallback()));
            TermEntry superTerm = getTermEntry(EntityPart.ENTITY_SUPERTERM);
            superTerm.addOnOntologyChangeHandler(new OntologyDependencyHandler(superTerm, termEntryUnitsMap));
            TermEntry subTerm = getTermEntry(EntityPart.ENTITY_SUBTERM);
            subTerm.addOnOntologyChangeHandler(new OntologyDependencyHandler(subTerm, termEntryUnitsMap));
            tagList = new ZfinListBox(false);
            tagList.addItem("abnormal");
            tagList.addItem("normal");
            HorizontalPanel tagPanel = new HorizontalPanel();
            HTML tagHtml = new HTML("Tag: ");
            tagHtml.setStyleName(WidgetUtil.BOLD);
            tagPanel.add(tagHtml);
            tagPanel.add(WidgetUtil.getNbsp());
            tagPanel.add(tagList);
            RootPanel.get(TAG).add(tagPanel);
        }
    }

    /**
     * STRUCTURE_PILE_CONSTRUCTION_ZONE-<superterm>-info
     *
     * @param termPart post-composed part
     * @return id of <div> element
     */
    private String getDivName(Enum termPart) {
        StringBuilder builder = new StringBuilder(60);
        builder.append(STRUCTURE_PILE_CONSTRUCTION_ZONE);
        builder.append("-");
        builder.append(termPart.name().toLowerCase());
        builder.append("-info");
        return builder.toString();
    }

    private void addClickListener() {
        swapTermsButton.addClickHandler(new SwapTermsClickListener(false));
        swapRelatedTermsButton.addClickHandler(new SwapTermsClickListener(true));
        resetButton.addClickHandler(new ResetClickListener());
    }

    /**
     * Register listeners to respond to a create new pile structure event.
     *
     * @param listener CreatePileStructureListener
     */
    public void addCreatePileChangeListener(PileStructureListener listener) {
        pileListener.add(listener);
    }

    /**
     * This method takes an ExpressedTermDTO and pre-populates the construction
     * zone with the given entities. The EntityPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    public void prepopulateConstructionZone(ExpressedTermDTO term, EntityPart selectedEntity) {
        // ToDo: go through each TermEntryPart in a more scalable way. Requires ExpressedTermDTO to
        // to make use of EntityPart object
        //Window.alert(selectedEntity.name());
        switch (selectedEntity) {
            case ENTITY_SUPERTERM:
                lookupRPC.getTermInfo(term.getEntity().getSuperTerm().getOntology(), term.getEntity().getSuperTerm().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getEntity().getSuperTerm().getZdbID()));
                break;
            case ENTITY_SUBTERM:
                lookupRPC.getTermInfo(term.getEntity().getSubTerm().getOntology(), term.getEntity().getSubTerm().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getEntity().getSubTerm().getZdbID()));
                break;
        }
        populateTermEntryUnits(term);
        errorElement.clearAllErrors();
    }

    public void prepopulateConstructionZoneWithPhenotype(PhenotypeStatementDTO term, EntityPart selectedEntity) {
        // ToDo: go through each TermEntryPart in a more scalable way. Requires ExpressedTermDTO to
        // to make use of EntityPart object
        //Window.alert(selectedEntity.name());
        switch (selectedEntity) {
            case ENTITY_SUPERTERM:
                lookupRPC.getTermInfo(term.getEntity().getSuperTerm().getOntology(), term.getEntity().getSuperTerm().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getEntity().getSuperTerm().getZdbID()));
                break;
            case ENTITY_SUBTERM:
                lookupRPC.getTermInfo(term.getEntity().getSubTerm().getOntology(), term.getEntity().getSubTerm().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getEntity().getSubTerm().getZdbID()));
                break;
            case QUALITY:
                lookupRPC.getTermInfo(term.getQuality().getOntology(), term.getQuality().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getQuality().getZdbID()));
                isQualityRelational = term.getQuality().isSubsetOf(SubsetDTO.RELATIONAL_SLIM);
                break;
            case RELATED_ENTITY_SUPERTERM:
                lookupRPC.getTermInfo(term.getRelatedEntity().getSuperTerm().getOntology(), term.getRelatedEntity().getSuperTerm().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getRelatedEntity().getSuperTerm().getZdbID()));
                break;
            case RELATED_ENTITY_SUBTERM:
                lookupRPC.getTermInfo(term.getRelatedEntity().getSubTerm().getOntology(), term.getRelatedEntity().getSubTerm().getZdbID(),
                        new TermInfoCallBack(termInfoTable, term.getRelatedEntity().getSubTerm().getZdbID()));
                break;
        }
        errorElement.clearAllErrors();
        populateTermEntryUnitsPhenotype(term);
    }

    private void populateTermEntryUnitsPhenotype(PhenotypeStatementDTO term) {
        populateTermEntryUnits(term);
        String tag = term.getTag();
        if (tag != null) {
            tagList.selectEntryByDisplayName(tag);
        }
    }

    private void populateTermEntryUnits(ExpressedTermDTO term) {
        resetConstructionValuesZone();
        for (EntityPart entityPart : EntityPart.values())
            populateSingleTermEntry(term, entityPart);

    }

    private void populateTermEntryUnits(PhenotypeStatementDTO term) {
        resetConstructionValuesZone();
        for (EntityPart entityPart : EntityPart.values())
            populateSingleTermEntry(term, entityPart);
    }

    private void resetConstructionValuesZone() {
        resetConstructionZone();
        for (EntityPart entityPart : EntityPart.values()) {
            TermEntry termEntry = getTermEntry(entityPart);
            if (termEntry != null) {
                termEntry.reset();
            }
        }
        setVisibilityForRelatedEntityPanel(false);
    }

    private void populateSingleTermEntry(ExpressedTermDTO term, EntityPart entityPart) {
        TermEntry termEntry = getTermEntry(entityPart);
        if (termEntry != null) {
            TermDTO termDTO = null;
            switch (entityPart) {
                case ENTITY_SUPERTERM:
                    termDTO = term.getTermDTO(EntityPart.ENTITY_SUPERTERM);
                    break;
                case ENTITY_SUBTERM:
                    termDTO = term.getTermDTO(EntityPart.ENTITY_SUBTERM);
                    break;
                case RELATED_ENTITY_SUPERTERM:
                    termDTO = term.getTermDTO(EntityPart.RELATED_ENTITY_SUPERTERM);
                    if (termDTO != null)
                        setVisibilityForRelatedEntityPanel(true);
                    break;
                case RELATED_ENTITY_SUBTERM:
                    termDTO = term.getTermDTO(EntityPart.RELATED_ENTITY_SUBTERM);
                    break;
                case QUALITY:
                    termDTO = term.getTermDTO(EntityPart.QUALITY);
                    break;
            }
            if (termDTO == null)
                return;
            termEntry.getTermTextBox().setText(termDTO.getTermName());
            ZfinListBox selector = termEntry.getOntologySelector();
            if (selector != null && selector.getItemCount() > 0) {
                selector.selectEntryByDisplayName(termDTO.getOntology().getDisplayName());
            }
            LookupComposite lookupEntryBox = termEntry.getTermTextBox();
            lookupEntryBox.setOntology(termDTO.getOntology());
            lookupEntryBox.unsetUnValidatedTextMarkup();
        }
    }

    /**
     * Sets the construction zone to the default setting:
     * 1. all post-composed parts set to an empty string
     * 2. set ontology selectors to AO
     * 3. displays the default structure in the term info box (currently AO:anatomical structure)
     */
    public void resetConstructionZone() {
        errorElement.clearAllErrors();
    }

    /**
     * Injected variable on which logic is performed.
     *
     * @param structurePile Structure Pile
     */
    public void setStructurePile(StructurePile structurePile) {
        this.structurePile = structurePile;
        addCreatePileChangeListener(structurePile);
    }

    /**
     * Injected validator for adding new structures. Validates the combination of valid post-composed terms.
     *
     * @param structureValidator StructureValidator
     */
    public void setStructureValidator(StructureValidator structureValidator) {
        this.structureValidator = structureValidator;
    }

    /**
     * Checks if the superterm and the subterm can be swapped.
     * 1) if each ontology selector has the matching ontology
     *
     * @param isRelatedEntity applies to the related entity or the entity
     * @return true if swappable or false otherwise
     */
    public boolean canSwapSupertermAndSubterm(boolean isRelatedEntity) {
        TermEntry superterm;
        if (isRelatedEntity)
            superterm = getTermEntry(EntityPart.RELATED_ENTITY_SUPERTERM);
        else
            superterm = getTermEntry(EntityPart.ENTITY_SUPERTERM);
        OntologyDTO selectedSupertermOntology = superterm.getSelectedOntology();
        TermEntry subterm;
        if (isRelatedEntity)
            subterm = getTermEntry(EntityPart.RELATED_ENTITY_SUBTERM);
        else
            subterm = getTermEntry(EntityPart.ENTITY_SUBTERM);
        OntologyDTO selectedSubtermOntology = subterm.getSelectedOntology();
        if (!superterm.hasOntology(selectedSubtermOntology)) {
            errorElement.setText("Superterm does not have a " + selectedSubtermOntology.getDisplayName() + " Ontology choice.");
            return false;
        }
        if (!subterm.hasOntology(selectedSupertermOntology)) {
            errorElement.setText("Subterm does not have a " + selectedSubtermOntology.getDisplayName() + " Ontology choice.");
            return false;
        }
        return true;
    }

    /**
     * Retrieve the TermEntryUnit object pertaining to the superterm.
     *
     * @param entityPart Entity part
     * @return TermEntry Unit
     */
    private TermEntry getTermEntry(EntityPart entityPart) {
        return termEntryUnitsMap.get(entityPart);
    }

    private EntityPart getPostComposedPart(TermEntry termEntry) {
        for (EntityPart part : termEntryUnitsMap.keySet()) {
            if (termEntryUnitsMap.get(part).equals(termEntry))
                return part;
        }
        return null;
    }

    // ************************* ClickHandler, ChangeHandler, Callbacks, ....

    private class SwapTermsClickListener implements ClickHandler {

        private boolean isRelatedEntity;

        public SwapTermsClickListener(boolean isRelatedEntity) {
            this.isRelatedEntity = isRelatedEntity;
        }

        public void onClick(ClickEvent widget) {
            errorElement.clearAllErrors();
            if (canSwapSupertermAndSubterm(isRelatedEntity)) {
                if (isRelatedEntity)
                    getTermEntry(EntityPart.RELATED_ENTITY_SUPERTERM).swapTerms(getTermEntry(EntityPart.RELATED_ENTITY_SUBTERM));
                else
                    getTermEntry(EntityPart.ENTITY_SUPERTERM).swapTerms(getTermEntry(EntityPart.ENTITY_SUBTERM));

            }

        }

    }


    private class OnAutcompleteFocusHandler implements FocusHandler {

        private TermEntry termEntryUnit;

        private OnAutcompleteFocusHandler(TermEntry termEntryUnit) {
            this.termEntryUnit = termEntryUnit;
        }

        public void onFocus(FocusEvent event) {
            errorElement.clearAllErrors();
            final OntologyDTO ontology = termEntryUnit.getSelectedOntology();
            String termName = termEntryUnit.getTermText();
            if (!termEntryUnit.isSuggestionListShowing() && StringUtils.isNotEmpty(termName)) {
                lookupRPC.getTermByName(ontology, termName, new ZfinAsyncCallback<TermDTO>(
                        "Failed to find term: " + termName + " for ontology: " + ontology.getDisplayName(), null) {
                    @Override
                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof TermNotFoundException){
                            TermNotFoundException exception = (TermNotFoundException) throwable;
                            errorElement.setError(exception.getMessage());
                            termEntryUnit.getTermTextBox().setValidationStyle(false);
                        }else
                            super.onFailure(throwable);
                    }

                    @Override
                    public void onSuccess(TermDTO termDTO) {
                        if (termDTO != null) {
                            lookupRPC.getTermInfo(ontology, termDTO.getZdbID(), new TermInfoCallBack(termInfoTable, termDTO.getZdbID()));
                        }
                    }
                });
            }
        }

    }


    private class AddNewStructureClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            boolean isPhenotype = false;
            if (termEntryMap.containsKey(EntityPart.QUALITY))
                isPhenotype = true;
            PhenotypeStatementDTO termDTO = new PhenotypeStatementDTO();
            // ToDO: unify with FX, i.e. have FX also use entity instead of straight subterm setter
            EntityDTO entityDTO = new EntityDTO();
            termDTO.setEntity(entityDTO);
            EntityDTO relatedEntityDTO = new EntityDTO();
            for (Map.Entry<EntityPart, TermEntry> postComposedPartTermEntryEntry : termEntryUnitsMap.entrySet()) {
                TermEntry termEntry = postComposedPartTermEntryEntry.getValue();
                switch (postComposedPartTermEntryEntry.getKey()) {
                    case ENTITY_SUPERTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            entityDTO.setSuperTerm(getTermDTO(termEntry));
                        }
                        break;
                    case ENTITY_SUBTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            entityDTO.setSubTerm(getTermDTO(termEntry));
                        }
                        break;
                    case QUALITY:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            termDTO.setQuality(getTermDTO(termEntry));
                            // awkward but we need to somehow pass on the relational-type for validation purposes
                            if (isQualityRelational)
                                termDTO.getQuality().addSubset(SubsetDTO.RELATIONAL_SLIM);
                        }
                        break;
                    case RELATED_ENTITY_SUPERTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            relatedEntityDTO.setSuperTerm(getTermDTO(termEntry));
                        }
                        break;
                    case RELATED_ENTITY_SUBTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            relatedEntityDTO.setSubTerm(getTermDTO(termEntry));
                        }
                        break;
                }
            }
            if (relatedEntityDTO != null)
                termDTO.setRelatedEntity(relatedEntityDTO);
            if (isPhenotype) {
                String tag = tagList.getItemText(tagList.getSelectedIndex());
                termDTO.setTag(tag);
            }
            if (structureValidator.isValidNewPileStructure(termDTO)) {
                if (structurePile.hasStructureOnPile(termDTO)) {
                    errorElement.setText("Structure [" + termDTO.getDisplayName() + "] already on pile.");
                    return;
                }
                if (isPhenotype)
                    pileStructureRPCAsync.createPhenotypePileStructure(termDTO, publicationID, new CreatePhenotypePileStructureCallback());
                else
                    pileStructureRPCAsync.createPileStructure(termDTO, publicationID, new CreatePileStructureCallback());
                errorElement.clearAllErrors();
            } else {
                errorElement.setText(structureValidator.getErrorMessage());
            }
        }

        private TermDTO getTermDTO(TermEntry termEntry) {
            TermDTO superterm = new TermDTO();
            superterm.setName(termEntry.getTermText());
            superterm.setOntology(termEntry.getSelectedOntology());
            return superterm;
        }

    }

    private class CreatePileStructureCallback implements AsyncCallback<ExpressionPileStructureDTO> {

        public void onFailure(Throwable throwable) {
            errorElement.setText(throwable.getMessage());
        }

        /**
         * Returns the pile Structure entity
         *
         * @param pileStructure pile Structure
         */
        public void onSuccess(ExpressionPileStructureDTO pileStructure) {
            //Window.alert("Success");
            // call listeners
            for (PileStructureListener listener : pileListener) {
                listener.onPileStructureCreation(pileStructure);
            }
            resetButton.click();
            errorElement.clearAllErrors();
        }
    }

    private class CreatePhenotypePileStructureCallback implements AsyncCallback<PhenotypePileStructureDTO> {

        public void onFailure(Throwable throwable) {
            errorElement.setText(throwable.getMessage());
            if (throwable instanceof RelatedEntityNotFoundException) {
                setVisibilityForRelatedEntityPanel(true);
            }
        }

        /**
         * Returns the pile Structure entity
         *
         * @param pileStructure pile Structure
         */
        public void onSuccess(PhenotypePileStructureDTO pileStructure) {
            //Window.alert("Success");
            // call listeners
            for (PileStructureListener listener : pileListener) {
                listener.onPileStructureCreation(pileStructure);
            }
            resetConstructionValuesZone();
            resetButton.click();
        }
    }

    private class ResetClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            for (TermEntry termEntry : termEntryUnits) {
                termEntry.reset();
            }
            termInfoTable.setToDefault();
            errorElement.clearAllErrors();
            setVisibilityForRelatedEntityPanel(false);
        }

    }

    private class CopyTermToEntryFieldClickListener implements ClickHandler {

        private TermEntry termEntry;

        private CopyTermToEntryFieldClickListener(TermEntry termEntry) {
            this.termEntry = termEntry;
        }

        public void onClick(ClickEvent event) {
            TermDTO termInfo = termInfoTable.getCurrentTermInfoDTO();
            if (!termEntry.setTerm(termInfo)) {
                errorElement.setText("The " + getPostComposedPart(termEntry) + " term does not allow terms from the <" +
                        termInfo.getOntology().getDisplayName() + "> ontology.");
            }
            if (termEntry.getTermPart().equals(EntityPart.QUALITY)) {
                setVisibilityForRelatedEntityPanel(termInfoTable.getCurrentTermInfoDTO().isRelatedTerm());
            }
            // check if the new term name is valid
            if(termInfo.getOntology() == OntologyDTO.QUALITY){
                lookupRPC.getTermByName(termEntry.getSelectedOntology(), termInfo.getTermName(),
                        new ZfinAsyncCallback<TermDTO>(
                                "Failed to find term: " + termInfo.getTermName() + " for ontology: " + termEntry.getSelectedOntology(), null) {
                            @Override
                            public void onFailure(Throwable throwable) {
                                 termEntry.getTermTextBox().setValidationStyle(false);
                            }
                        });
            }
        }

    }

    private class RelatedQualityCheckCallback implements AsyncCallback<Boolean> {

        public void onFailure(Throwable throwable) {
            errorElement.setText(throwable.getMessage());
        }

        /**
         * Returns the pile Structure entity
         *
         * @param isRelational true or false
         */
        public void onSuccess(Boolean isRelational) {
            if (isRelational && isEntity(OntologyDTO.GO_MF)) {
                errorElement.setError("Cannot use a relational term with GO MF. Please choose a different Quality term.");
            } else {
                setVisibilityForRelatedEntityPanel(isRelational);
            }
            isQualityRelational = isRelational;
        }
    }

    /**
     * Check if the entity term is of ontology type given in the argument,
     * i.e. either as a sole super term or as post-composed subterm.
     *
     * @param ontology Ontology
     * @return true / false
     */
    public boolean isEntity(OntologyDTO ontology) {
        TermEntry subTermEntry = getTermEntry(EntityPart.ENTITY_SUBTERM);
        TermEntry superTermEntry = getTermEntry(EntityPart.ENTITY_SUPERTERM);
        OntologyDTO selectedSupertermOntology = superTermEntry.getSelectedOntology();
        if (subTermEntry.getTermText() != null && subTermEntry.getTermText().length() > 2) {
            OntologyDTO selectedSubtermOntology = subTermEntry.getSelectedOntology();
            if (selectedSubtermOntology == ontology)
                return true;
        }
        if (selectedSupertermOntology == ontology) {
            return true;
        }
        return false;
    }
}