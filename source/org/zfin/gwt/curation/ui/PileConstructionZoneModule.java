package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
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
    public static final String ERRORS = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-errors";
    public static final String TERMINFO = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-terminfo";
    public static final String TAG = "tag";

    // GUI elements
    private Map<PostComposedPart, TermEntry> termEntryUnitsMap = new HashMap<PostComposedPart, TermEntry>(3);
    private Button swapTermsButton = new Button("Swap Terms &uarr;&darr;");
    private Button submitButton = new Button("Add");
    private Button resetButton = new Button("Reset");
    private SimpleErrorElement errorElement = new SimpleErrorElement(ERRORS);
    private TermInfoComposite termInfoTable;
    private ZfinListBox tagList;
    private Label historyLabelTermInfo = new Label();

    private Map<PostComposedPart, List<OntologyDTO>> termEntryMap;
    private Collection<TermEntry> termEntryUnits = new ArrayList<TermEntry>(3);

    private Collection<PileStructureListener> pileListener = new ArrayList<PileStructureListener>(2);

    // injected variables
    private StructurePile structurePile;
    private StructureValidator structureValidator;
    private String publicationID;

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();

    public PileConstructionZoneModule(String publicationID, Map<PostComposedPart, List<OntologyDTO>> termEntryMap) {
        this.publicationID = publicationID;
        this.termEntryMap = termEntryMap;
        initGUI();
    }

    private void initGUI() {
        RootPanel.get(SWAP_TERMS).add(swapTermsButton);
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
    }

    private void createTermEntryUnits() {
        for (Map.Entry<PostComposedPart, List<OntologyDTO>> postComposedEntry : termEntryMap.entrySet()) {
            List<OntologyDTO> ontologies = postComposedEntry.getValue();
            TermEntry termEntry = new TermEntry(ontologies, postComposedEntry.getKey(),termInfoTable);
            termEntry.getCopyFromTerminfoToTextButton().addClickHandler(
                    new CopyTermToEntryFieldClickListener(termEntry));
            String divName = getDivName(postComposedEntry.getKey());
            termEntry.addOnFocusHandler(new OnAutcompleteFocusHandler(termEntry));
            termEntryUnits.add(termEntry);
            termEntryUnitsMap.put(postComposedEntry.getKey(), termEntry);
            RootPanel.get(divName).add(termEntry);
        }
        // add dependency handler to super term changes
        OntologyDependencyHandler handler = new OntologyDependencyHandler(getSuperterm(), getQualityTerm());
        getSuperterm().addOnOntologyChangeHandler(handler);
        if (termEntryMap.containsKey(PostComposedPart.QUALITY)) {
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
        swapTermsButton.addClickHandler(new SwapTermsClickListener());
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
     * zone with the given entities. The PostComposedPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    public void prepopulateConstructionZone(ExpressedTermDTO term, PostComposedPart selectedEntity) {
        // ToDo: go through each TermEntryPart in a more scalable way. Requires ExpressedTermDTO to
        // to make use of PostComposedPart object
        //Window.alert(selectedEntity.name());
        switch (selectedEntity) {
            case SUPERTERM:
                lookupRPC.getTermInfo(OntologyDTO.ANATOMY, term.getSuperterm().getTermID(),
                        new TermInfoCallBack(termInfoTable, term.getSuperterm().getTermID()));
                break;
            case SUBTERM:
                lookupRPC.getTermInfo(term.getSubterm().getOntology(), term.getSubterm().getTermID(),
                        new TermInfoCallBack(termInfoTable, term.getSubterm().getTermID()));
                break;
        }
        populateTermEntryUnits(term);
        errorElement.clearAllErrors();
    }

    public void prepopulateConstructionZoneWithPhenotype(PhenotypeTermDTO term, PostComposedPart selectedEntity) {
        // ToDo: go through each TermEntryPart in a more scalable way. Requires ExpressedTermDTO to
        // to make use of PostComposedPart object
        //Window.alert(selectedEntity.name());
        switch (selectedEntity) {
            case SUPERTERM:
                lookupRPC.getTermInfo(term.getSuperterm().getOntology(), term.getSuperterm().getTermID(),
                        new TermInfoCallBack(termInfoTable, term.getSuperterm().getTermID()));
                break;
            case SUBTERM:
                lookupRPC.getTermInfo(term.getSubterm().getOntology(), term.getSubterm().getTermID(),
                        new TermInfoCallBack(termInfoTable, term.getSubterm().getTermID()));
                break;
            case QUALITY:
                lookupRPC.getTermInfo(term.getQuality().getOntology(), term.getQuality().getTermID(),
                        new TermInfoCallBack(termInfoTable, term.getQuality().getTermID()));
        }
        errorElement.clearAllErrors();
        populateTermEntryUnitsPhenotype(term);
    }

    private void populateTermEntryUnitsPhenotype(PhenotypeTermDTO term) {
        populateTermEntryUnits(term);
        TermEntry quality = getQualityTerm();
        if (quality != null) {
            quality.getTermTextBox().setText(term.getQuality().getTermName());
            ZfinListBox selector = quality.getOntologySelector();
            if (selector != null && selector.getItemCount() > 0){
                selector.selectEntryByDisplayName(term.getQuality().getOntology().getDisplayName());
            }
            LookupComposite lookupEntryBox = quality.getTermTextBox();
            lookupEntryBox.setOntology(term.getQuality().getOntology());
        }
        String tag = term.getTag();
        if (tag != null) {
            tagList.selectEntryByDisplayName(tag);
        }
    }

    private void populateTermEntryUnits(ExpressedTermDTO term) {
        TermEntry superterm = getSuperterm();
        if (superterm != null) {
            superterm.getTermTextBox().setText(term.getSuperterm().getTermName());
            ZfinListBox selector = superterm.getOntologySelector();
            if (selector != null && selector.getItemCount() > 0){
                selector.selectEntryByDisplayName(term.getSuperterm().getOntology().getDisplayName());
            }
            LookupComposite lookupEntryBox = superterm.getTermTextBox();
            lookupEntryBox.setOntology(term.getSuperterm().getOntology());
        }

        TermEntry subterm = getSubterm();
        if (subterm != null) {
            LookupComposite lookupEntryBox = subterm.getTermTextBox();
            TermDTO subtermDTO = term.getSubterm();
            if (subtermDTO != null) {
                lookupEntryBox.setText(subtermDTO.getTermName());
                lookupEntryBox.setType(LookupComposite.GDAG_TERM_LOOKUP);
                lookupEntryBox.setOntology(subtermDTO.getOntology());
                ZfinListBox selector = subterm.getOntologySelector();
                if (selector != null && selector.getItemCount() > 0) {
                    selector.selectEntryByDisplayName(subtermDTO.getOntology().getDisplayName());
                }
            } else {
                // set to default if no subterm available to clear out any previous entries
                lookupEntryBox.setText("");
            }
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
     * @return true if swappable or false otherwise
     */
    public boolean canSwapSupertermAndSubterm() {
        TermEntry superterm = getSuperterm();
        OntologyDTO selectedSupertermOntology = getSuperterm().getSelectedOntology();
        TermEntry subterm = getSubterm();
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
     * @return TermEntryUnit
     */
    private TermEntry getSuperterm() {
        return termEntryUnitsMap.get(PostComposedPart.SUPERTERM);
    }

    /**
     * Retrieve the TermEntryUnit object pertaining to the subterm.
     *
     * @return TermEntryUnit
     */
    private TermEntry getSubterm() {
        return termEntryUnitsMap.get(PostComposedPart.SUBTERM);
    }

    /**
     * Retrieve the TermEntryUnit object pertaining to the quality.
     *
     * @return TermEntryUnit
     */
    private TermEntry getQualityTerm() {
        return termEntryUnitsMap.get(PostComposedPart.QUALITY);
    }

    private PostComposedPart getPostComposedPart(TermEntry termEntry) {
        for (PostComposedPart part : termEntryUnitsMap.keySet()) {
            if (termEntryUnitsMap.get(part).equals(termEntry))
                return part;
        }
        return null;
    }

    // ************************* ClickHandler, ChangeHandler, Callbacks, ....

    private class SwapTermsClickListener implements ClickHandler {

        public void onClick(ClickEvent widget) {
            errorElement.clearAllErrors();
            if (canSwapSupertermAndSubterm())
                getSuperterm().swapTerms(getSubterm());

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
            if (!termEntryUnit.isSuggestionListShowing() && StringUtils.isNotEmpty(termName)){
                lookupRPC.getTermByName(ontology,termName,new ZfinAsyncCallback<TermDTO>(
                        "Failed to find term: " + termName + " for ontology: " +ontology.getDisplayName(),null){
                    @Override
                    public void onSuccess(TermDTO termDTO) {
                        if(termDTO!=null){
                            lookupRPC.getTermInfo(ontology, termDTO.getTermID(), new TermInfoCallBack(termInfoTable, termDTO.getTermID()));
                        }
                    }
                });
            }
        }

    }


    private class AddNewStructureClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            boolean isPhenotype = false;
            if (termEntryMap.containsKey(PostComposedPart.QUALITY))
                isPhenotype = true;
            PhenotypeTermDTO termDTO = new PhenotypeTermDTO();
            for (Map.Entry<PostComposedPart, TermEntry> postComposedPartTermEntryEntry : termEntryUnitsMap.entrySet()) {
                TermEntry termEntry = postComposedPartTermEntryEntry.getValue();
                switch (postComposedPartTermEntryEntry.getKey()) {
                    case SUPERTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            termDTO.setSuperterm(getTermDTO(termEntry));
                        }
                        break;
                    case SUBTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            termDTO.setSubterm(getTermDTO(termEntry));
                        }
                        break;
                    case QUALITY:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            termDTO.setQuality(getTermDTO(termEntry));
                        }
                        break;
                }
            }
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
            superterm.setTermName(termEntry.getTermText());
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
            resetButton.click();
            errorElement.clearAllErrors();
        }
    }

    private class ResetClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            for (TermEntry termEntry : termEntryUnits) {
                termEntry.reset();
            }
            termInfoTable.setToDefault();
            errorElement.clearAllErrors();
        }

    }

    private class CopyTermToEntryFieldClickListener implements ClickHandler {

        private TermEntry termEntry;

        private CopyTermToEntryFieldClickListener(TermEntry termEntry) {
            this.termEntry = termEntry;
        }

        public void onClick(ClickEvent event) {
            if (!termEntry.setTerm(termInfoTable.getCurrentTermInfo()))
                errorElement.setText("The " + getPostComposedPart(termEntry) + " term does not allow terms from the <" +
                        termInfoTable.getCurrentTermInfo().getOntology().getDisplayName() + "> ontology.");
        }

    }

}