package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.LookupComposite;
import org.zfin.gwt.root.util.LookupService;
import org.zfin.gwt.root.util.LookupServiceAsync;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for the construction zone that creates post-composed structures.
 * Initially, it's used for FX but should be extended to also create annotations for PATO.
 * <p/>
 * Requirements:
 * A) Subterm Field: A subterm field in which either AO or GO terms are autocompleted depending on the ontology selector.
 * This field is optional.
 * B) Superterm Field: A superterm field in which an AO term is completed.
 * C) Swap Terms: Swapping terms will exhcange the superterm's and the subterm's entry. If the subterm is a GO term no swapping is
 * possible as the superterm has to be an AO term.
 * D) Reset Button: Clicking this button will empty both term fields and set the subterm ontology to AO.
 * <p/>
 * Usage:
 * 1) Constructor with publication ID and termEntryMap that initializes the term parts being used and the Ontologies
 * used for each term.
 * 2) inject PileStructure object which is called after successful addition of new post-composed term.
 * 3) inject StructureValidator that validates combinations of post-composed terms.
 */
public class PileConstructionZoneModule extends Composite implements ConstructionZone {

    // div-elements
    public static final String CONSTRUCTION_ZONE = "construction-zone";
    public static final String STRUCTURE_PILE_CONSTRUCTION_ZONE = "structure-pile-construction-zone";
    public static final String SUBTERM_INFO = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-subterm-info";
    public static final String SUPERTERM_INFO = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-superterm-info";
    public static final String SUBMIT_RESET = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-submit-reset";
    public static final String SWAP_TERMS = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-swap-terms";
    public static final String ERRORS = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-errors";
    public static final String TERMINFO = STRUCTURE_PILE_CONSTRUCTION_ZONE + "-terminfo";

    // GUI elements
    private Map<PostComposedPart, TermEntry> termEntryUnitsMap = new HashMap<PostComposedPart, TermEntry>();
    private Button swapTermsButton = new Button("Swap Terms &uarr;&darr;");
    private Button submitButton = new Button("Add");
    private Button resetButton = new Button("Reset");
    private Label errorMessages = new Label();
    private ZFlexTable termInfoTable;
    private Label historyLabelTermInfo = new Label();


    private Map<String, TermInfo> historyMap = new HashMap<String, TermInfo>();
    private Map<PostComposedPart, List<Ontology>> termEntryMap;
    private List<TermEntry> termEntryUnits = new ArrayList<TermEntry>();
    // RPC class being used for this section.

    private List<PileStructureListener> pileListener = new ArrayList<PileStructureListener>();

    private TermInfo currentTermInfo;

    // RPC classes being used for this section.
    private LookupServiceAsync lookupRPCAsync = LookupService.App.getInstance();
    private CurationExperimentRPCAsync curationRPCAsync = CurationExperimentRPC.App.getInstance();

    // injected variables
    private StructurePile structurePile;
    private StructureValidator structureValidator;
    private String publicationID;

    private static final Map<String, String> ontologyMap = new HashMap<String, String>();

    static {
        ontologyMap.put(Ontology.ANATOMY.getDisplayName(), LookupComposite.TYPE_ANATOMY_ONTOLOGY);
        ontologyMap.put(Ontology.GO_CC.getDisplayName(), LookupComposite.GDAG_TERM_LOOKUP);
    }

    public PileConstructionZoneModule(String publicationID, Map<PostComposedPart, List<Ontology>> termEntryMap) {
        this.publicationID = publicationID;
        this.termEntryMap = termEntryMap;
        initGUI();
    }

    private void initGUI() {
        createTermEntryUnits();
        RootPanel.get(SWAP_TERMS).add(swapTermsButton);
        HorizontalPanel submitResetPanel = new HorizontalPanel();
        submitButton.addClickHandler(new AddNewStructureClickListener());
        submitResetPanel.add(submitButton);
        submitResetPanel.add(resetButton);
        RootPanel.get(SUBMIT_RESET).add(submitResetPanel);
        errorMessages.addStyleName("error");
        termInfoTable = new ZFlexTable();
        VerticalPanel termInfoPanel = new VerticalPanel();
        termInfoPanel.add(historyLabelTermInfo);
        termInfoPanel.add(termInfoTable);
        RootPanel.get(TERMINFO).add(termInfoPanel);
        RootPanel.get(ERRORS).add(errorMessages);
        addClickListener();
    }

    private void createTermEntryUnits() {
        for (PostComposedPart termPart : termEntryMap.keySet()) {
            List<Ontology> ontologies = termEntryMap.get(termPart);
            TermEntry termEntry = new TermEntry(ontologies, termPart);
            termEntry.getCopyFromTerminfoToTextButton().addClickHandler(
                    new CopyTermToEntryFieldClickListener(termEntry.getTermTextBox(), termEntry.getOntologySelector()));
            String divName = getDivName(termPart);
            termEntry.addOnFocusHandler(new OnAutcompleteFocusHandler(termEntry));
            termEntryUnits.add(termEntry);
            termEntryUnitsMap.put(termPart, termEntry);
            RootPanel.get(divName).add(termEntry);
        }
    }

    /**
     * STRUCTURE_PILE_CONSTRUCTION_ZONE-<superterm>-info
     *
     * @param termPart postcomposed part
     * @return id of <div> element
     */
    private String getDivName(PostComposedPart termPart) {
        StringBuilder builder = new StringBuilder();
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

    private void clearErrorMessages() {
        errorMessages.setText("");
    }

    /**
     * Display the term info for a given term ID in a given ontology.
     *
     * @param ontology Ontology
     * @param termID   term ID: zdb ID or obo ID
     */
    public void showTermInfo(Ontology ontology, String termID) {
        //Window.alert("Show Term:: " + ontology + ":" + termID);
        lookupRPCAsync.getTermInfo(ontology, termID, new TermInfoCallback(termID));
    }

    /**
     * Display the term info for a given term name in a given ontology.
     *
     * @param ontology Ontology
     * @param termName term name: zdb ID or obo ID
     */
    public void showTermInfoByName(Ontology ontology, String termName) {
        //Window.alert("Show Term:: " + ontology + ":" + termID);
        lookupRPCAsync.getTermInfoByName(ontology, termName, new TermInfoCallback(termName));
    }

    /**
     * Convenience method: Needed as we expose this to an external JS.
     *
     * @param ontology ontology
     * @param termID   term ID
     */
    public void showTermInfoString(String ontology, String termID) {
        showTermInfo(Ontology.getOntologyByDisplayName(ontology), termID);
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
                lookupRPCAsync.getTermInfo(Ontology.ANATOMY, term.getSupertermID(), new TermInfoCallback(term.getSupertermID()));
                break;
            case SUBTERM:
                lookupRPCAsync.getTermInfo(term.getSubtermOntology(), term.getSubtermID(), new TermInfoCallback(term.getSubtermID()));
                break;
            case QUALITY:
                lookupRPCAsync.getTermInfo(term.getSubtermOntology(), term.getSubtermID(), new TermInfoCallback(term.getSubtermID()));
                break;
        }
        populateTermEntryUnits(term);
        clearErrorMessages();
    }

    private void populateTermEntryUnits(ExpressedTermDTO term) {
        TermEntry superterm = getSuperterm();
        if (superterm != null) {
            superterm.getTermTextBox().setText(term.getSupertermName());
            ZfinListBox selector = superterm.getOntologySelector();
            if (selector != null && selector.getItemCount() > 0)
                selector.selectEntryByDisplayName(term.getSupertermOntology().getDisplayName());
        }

        TermEntry subterm = getSubterm();
        if (subterm != null) {
            LookupComposite lookupEntryBox = subterm.getTermTextBox();
            lookupEntryBox.setText(term.getSubtermName());
            lookupEntryBox.setType(TermEntry.getLookupOntologyName(term.getSubtermOntology()));
            lookupEntryBox.setGoOntology(term.getSubtermOntology());
            ZfinListBox selector = subterm.getOntologySelector();
            if (selector != null && selector.getItemCount() > 0) {
                selector.selectEntryByDisplayName(term.getSubtermOntology().getDisplayName());
            }
        }

        TermEntry quality = getQualityTerm();
        if (quality != null) {
            quality.getTermTextBox().setText(term.getSubtermName());
            ZfinListBox selector = quality.getOntologySelector();
            if (selector != null && selector.getItemCount() > 0)
                selector.selectEntryByDisplayName(term.getSubtermOntology().getDisplayName());
        }
    }

    /**
     * Sets the construction zone to the default setting:
     * 1. all postcomposed parts set to an emtpy string
     * 2. set ontology selectors to AO
     * 3. displays the default structure in the term info box (currently AO:anatomical structure)
     */
    public void resetConstructionZone() {
        clearErrorMessages();
    }

    /**
     * Injected variable on which logic is performed.
     *
     * @param structurePile Structure Pile
     */
    public void setStructurePile(StructurePile structurePile) {
        this.structurePile = structurePile;
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
        Ontology selectedSupertermOntology = getSuperterm().getSelectedOntology();
        TermEntry subterm = getSubterm();
        Ontology selectedSubtermOntology = subterm.getSelectedOntology();
        if (!superterm.hasOntology(selectedSubtermOntology)) {
            errorMessages.setText("Superterm does not have a " + selectedSubtermOntology.getDisplayName() + " Ontology choice.");
            return false;
        }
        if (!subterm.hasOntology(selectedSupertermOntology)) {
            errorMessages.setText("Subterm does not have a " + selectedSubtermOntology.getDisplayName() + " Ontology choice.");
            return false;
        }
        return true;
    }

    /**
     * Retrieve the TermEntryUnit object pertainging to the superterm.
     *
     * @return TermEntryUnit
     */
    private TermEntry getSuperterm() {
        return termEntryUnitsMap.get(PostComposedPart.SUPERTERM);
    }

    /**
     * Retrieve the TermEntryUnit object pertainging to the subterm.
     *
     * @return TermEntryUnit
     */
    private TermEntry getSubterm() {
        return termEntryUnitsMap.get(PostComposedPart.SUBTERM);
    }

    /**
     * Retrieve the TermEntryUnit object pertainging to the quality.
     *
     * @return TermEntryUnit
     */
    private TermEntry getQualityTerm() {
        return termEntryUnitsMap.get(PostComposedPart.QUALITY);
    }

    // ************************* ClickHandler, ChangeHandler, Callbacks, ....

    private class SwapTermsClickListener implements ClickHandler {

        public void onClick(ClickEvent widget) {
            clearErrorMessages();
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
            clearErrorMessages();
            Ontology ontology = termEntryUnit.getSelectedOntology();
            String termName = termEntryUnit.getTermText();
            if (StringUtils.isNotEmpty(termName))
                showTermInfoByName(ontology, termName);
        }

    }


    private class AddNewStructureClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            ExpressedTermDTO expressedTerm = new ExpressedTermDTO();
            for (PostComposedPart termPart : termEntryUnitsMap.keySet()) {
                TermEntry termEntry = termEntryUnitsMap.get(termPart);
                switch (termPart) {
                    case SUPERTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            expressedTerm.setSupertermName(termEntry.getTermText());
                            expressedTerm.setSupertermOntology(termEntry.getSelectedOntology());
                        }
                        break;
                    case SUBTERM:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            expressedTerm.setSubtermName(termEntry.getTermText());
                            expressedTerm.setSubtermOntology(termEntry.getSelectedOntology());
                        }
                        break;
                    case QUALITY:
                        if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                            expressedTerm.setQualityName(termEntry.getTermText());
                            expressedTerm.setQualityOntology(termEntry.getSelectedOntology());
                        }
                        break;
                }
            }
            if (structureValidator.isValidNewPileStructure(expressedTerm)) {
                if (structurePile.hasStructureOnPile(expressedTerm)) {
                    errorMessages.setText("Structure [" + expressedTerm.getDisplayName() + "] already on pile.");
                    return;
                }

                curationRPCAsync.createPileStructure(expressedTerm, publicationID, new CreatePileStructureCallback());
                clearErrorMessages();
                resetButton.click();
            } else {
                errorMessages.setText(structureValidator.getErrorMessage());
            }
        }

    }

    class CreatePileStructureCallback implements AsyncCallback<PileStructureDTO> {

        public void onFailure(Throwable throwable) {
            if (throwable instanceof PileStructureExistsException) {
                errorMessages.setText(throwable.getMessage());
            }
            errorMessages.setText(throwable.getMessage());
        }

        /**
         * Returns the pile Structure entity
         *
         * @param pileStructure pile Structure
         */
        public void onSuccess(PileStructureDTO pileStructure) {
            //Window.alert("Success");
            // call listeners
            for (PileStructureListener listener : pileListener) {
                listener.onPileStructureCreation(pileStructure);
            }
            clearErrorMessages();
        }
    }

    private class ResetClickListener implements ClickHandler {

        public void onClick(ClickEvent event) {
            for (TermEntry termEntry : termEntryUnits) {
                termEntry.reset();
            }
            termInfoTable.setDefaultTermInfo();
            clearErrorMessages();
        }

    }

    private class TermInfoClickListener implements ClickHandler {

        private Hyperlink hyperlink;
        private TermInfo termInfo;

        private TermInfoClickListener(Hyperlink hyperlink, TermInfo info) {
            this.hyperlink = hyperlink;
            this.termInfo = info;
        }

        public void onClick(ClickEvent event) {
            lookupRPCAsync.getTermInfo(termInfo.getOntology(), termInfo.getID(), new TermInfoCallback(termInfo.getID()));
            termInfoTable.setUsedHyperlinkClickListener(true);
            clearErrorMessages();
        }

    }

    private class CopyTermToEntryFieldClickListener implements ClickHandler {

        private LookupComposite lookupCompositeField;
        private ZfinListBox listBox;

        public void setLookupCompositeField(LookupComposite lookupCompositeField) {
            this.lookupCompositeField = lookupCompositeField;
        }

        public void setListBox(ZfinListBox listBox) {
            this.listBox = listBox;
        }

        private CopyTermToEntryFieldClickListener(LookupComposite lookupCompositeField, ZfinListBox listBox) {
            this.lookupCompositeField = lookupCompositeField;
            this.listBox = listBox;
        }

        public void onClick(ClickEvent event) {
            lookupCompositeField.setText(currentTermInfo.getName());
            if (listBox != null) {
                listBox.selectEntryByDisplayName(currentTermInfo.getOntology().getDisplayName());
            }
        }

    }

    public enum TerminfoTableHeader implements TableHeader {
        TERM(0, "TERM:"),
        ID(1, "ID:"),
        SYNONYMS(2, "Synonyms:"),
        DEFINITION(3, "Definition:"),
        PARENTS(4, "PARENTS:"),
        CHILDREN(5, "CHILDREN:"),
        START_STAGE(6, "Start Stage:"),
        END_STAGE(7, "End Stage:"),
        COMMENT(8, "COMMENTS:");

        private int index;
        private String value;

        private TerminfoTableHeader(int index, String value) {
            this.index = index;
            this.value = value;
        }

        public String getName() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public static TerminfoTableHeader getHeaderName(String name) {
            for (TerminfoTableHeader info : TerminfoTableHeader.values()) {
                if (info.getName().equals(name))
                    return info;
            }
            return null;
        }

        public static TerminfoTableHeader[] getHeaderNames() {
            return TerminfoTableHeader.values();
        }

    }

    private class TermInfoCallback extends ZfinAsyncCallback<TermInfo> {
        private String historyToken;

        private TermInfoCallback(String historyToken) {
            super("Error during Terminfo call", errorMessages);
            this.historyToken = historyToken;
        }

        protected void onFailureCleanup() {

        }

        public void onSuccess(TermInfo termInfo) {
            if (termInfo == null)
                return;

            termInfoTable.updateTermInfo(termInfo);
            historyMap.put(historyToken, termInfo);
        }
    }

    private class ZFlexTable extends FlexTable implements ValueChangeHandler {

        private boolean usedHyperlinkClickListener;

        ZFlexTable() {
            super();
            History.addValueChangeHandler(this);
            setDefaultTermInfo();
        }

        protected void setDefaultTermInfo() {
            String defaultAOTermID = "ZFA:0000037";
            lookupRPCAsync.getTermInfo(Ontology.ANATOMY, defaultAOTermID, new TermInfoCallback(defaultAOTermID));
        }

        public void updateTermInfo(TermInfo termInfo) {
            clear();
            currentTermInfo = termInfo;
            int rowIndex = 0;
            int headerColumn = 0;
            int dataColumn = 1;
            addHeaderEntry(TerminfoTableHeader.TERM.getName(), rowIndex);
            setWidget(rowIndex, dataColumn, new Label(termInfo.getName()));
            getCellFormatter().addStyleName(rowIndex++, headerColumn, "bold");

            addHeaderEntry(TerminfoTableHeader.ID.getName(), rowIndex);
            setWidget(rowIndex++, dataColumn, new Label(termInfo.getID()));

            if (StringUtils.isNotEmpty(termInfo.getSynonyms())) {
                addHeaderEntry(TerminfoTableHeader.SYNONYMS.getName(), rowIndex);
                setWidget(rowIndex++, 1, new Label(termInfo.getSynonyms()));
            }

            if (StringUtils.isNotEmpty(termInfo.getDefinition())) {
                addHeaderEntry(TerminfoTableHeader.DEFINITION.getName(), rowIndex);
                setWidget(rowIndex++, 1, new HTML(termInfo.getDefinition()));
            }

            Map<String, List<TermInfo>> relatedTermsMap = termInfo.getRelatedTermInfos();
//            Window.alert("Related Terms: " + relatedTermsMap.size());
            if (relatedTermsMap != null) {
                for (String type : relatedTermsMap.keySet()) {
                    FlowPanel panel = new FlowPanel();
                    addHeaderEntry(type, rowIndex);
                    List<TermInfo> relatedTerms = relatedTermsMap.get(type);
                    for (TermInfo info : relatedTerms) {
                        panel.add(createHyperlink(info));
                        panel.add(new HTML("&nbsp;&nbsp;"));
                    }
                    setWidget(rowIndex++, 1, panel);
                }
            }
            if (termInfo.getOntology() == Ontology.ANATOMY) {
                addHeaderEntry(TerminfoTableHeader.START_STAGE.getName(), rowIndex);
                getCellFormatter().addStyleName(rowIndex, headerColumn, "nowrap");
                setWidget(rowIndex++, 1, new Label(termInfo.getStartStage()));

                addHeaderEntry(TerminfoTableHeader.END_STAGE.getName(), rowIndex);
                setWidget(rowIndex++, 1, new Label(termInfo.getEndStage()));
            }
            // comments
            addHeaderEntry(TerminfoTableHeader.COMMENT.getName(), rowIndex);
            getCellFormatter().addStyleName(rowIndex, headerColumn, "nowrap");
            setWidget(rowIndex++, 1, new Label(termInfo.getComment()));
        }

        private Hyperlink createHyperlink(TermInfo info) {
            Hyperlink link = new Hyperlink(info.getName(), info.getID());
            link.addStyleName("nowrap");
            link.addClickHandler(new TermInfoClickListener(link, info));
            return link;
        }

        private void addHeaderEntry(String name, int rowIndex) {
            setWidget(rowIndex, 0, new Label(name));
            getCellFormatter().addStyleName(rowIndex, 0, "bold left-top-aligned nowrap");
        }

        public void setUsedHyperlinkClickListener(boolean usedHyperlinkClickListener) {
            this.usedHyperlinkClickListener = usedHyperlinkClickListener;
        }

        public void onValueChange(ValueChangeEvent event) {
            String historyToken = (String) event.getValue();
            //Window.alert("value "+historyToken);
            TermInfo info = historyMap.get(historyToken);
            if (info == null)
                return;
            if (!usedHyperlinkClickListener)
                updateTermInfo(info);
            usedHyperlinkClickListener = false;
        }
    }
}