package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;

import java.util.*;

/**
 * Entry point for FX curation module.
 */
public class ConstructionZoneModule extends Composite implements HandlesError {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ConstructionZoneModule.ui.xml")
    interface MyUiBinder extends UiBinder<HorizontalPanel, ConstructionZoneModule> {
    }

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    TermInfoComposite termInfoBox;
    @UiField
    TermEntry subTermEntry;
    @UiField
    Button addButton;
    @UiField
    Button resetButton;
    @UiField
    SimpleErrorElement errorElement;
    @UiField
    Button swapTerms;
    @UiField
    TermEntry superTermEntry;
    @UiField
    VerticalPanel qualityListLeft;
    @UiField
    VerticalPanel qualityListRight;

    private ZfinListBox tagList;
    public static final String TAG_ABNORMAL = "abnormal";
    public static final String TAG_NORMAL = "normal";
    private String publicationID;
    private FxCurationPresenter fxCurationPresenter;

    private Map<EntityPart, TermEntry> termEntryUnitsMap = new HashMap<>(5);
    private Collection<TermEntry> termEntryUnits = new ArrayList<>(3);

    private final HandlerManager eventBus = new HandlerManager(null);

    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    public ConstructionZoneModule() {
        initWidget(uiBinder.createAndBindUi(this));
        createTermEntryUnits();
    }

    @UiHandler("resetButton")
    void onClickReset(@SuppressWarnings("unused") ClickEvent event) {
        superTermEntry.reset();
        subTermEntry.reset();
        errorElement.clearError();
        for (TermEntry termEntry : termEntryUnits) {
            termEntry.reset();
        }
        termInfoBox.setToDefault();
        fxCurationPresenter.clearQualityChecks();
    }

    private CheckBox getQualityCheckBox(String name) {
        CheckBox box = new CheckBox(name);
        box.setStyleName("small");
        if (name.equals("not"))
            box.addStyleName("red");
        return box;
    }


    @UiHandler("swapTerms")
    void onClickSwap(@SuppressWarnings("unused") ClickEvent event) {
        errorElement.clearAllErrors();
        if (canSwapSupertermAndSubterm(false))
            getTermEntry(EntityPart.ENTITY_SUBTERM).swapTerms(getTermEntry(EntityPart.ENTITY_SUPERTERM));
    }

    @UiHandler("addButton")
    void onClickAdd(@SuppressWarnings("unused") ClickEvent event) {
        fxCurationPresenter.submitStructure();
        //eventBus.fireEvent(new AddNewDiseaseTermEvent(disease));
    }

    private void createTermEntryUnits() {
        termEntryUnits.add(subTermEntry);
        termEntryUnits.add(superTermEntry);
        termEntryUnitsMap.put(EntityPart.ENTITY_SUPERTERM, superTermEntry);
        termEntryUnitsMap.put(EntityPart.ENTITY_SUBTERM, subTermEntry);
    }

    @Override
    public void setError(String message) {

    }

    @Override
    public void clearError() {

    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
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

    /**
     * This method takes an ExpressedTermDTO and pre-populates the construction
     * zone with the given entities. The EntityPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    void prepopulateConstructionZone(ExpressedTermDTO term, EntityPart selectedEntity) {
        switch (selectedEntity) {
            case ENTITY_SUPERTERM:
                lookupRPC.getTermInfo(term.getEntity().getSuperTerm().getOntology(), term.getEntity().getSuperTerm().getZdbID(),
                        new TermInfoCallBack(termInfoBox, term.getEntity().getSuperTerm().getZdbID()));
                break;
            case ENTITY_SUBTERM:
                lookupRPC.getTermInfo(term.getEntity().getSubTerm().getOntology(), term.getEntity().getSubTerm().getZdbID(),
                        new TermInfoCallBack(termInfoBox, term.getEntity().getSubTerm().getZdbID()));
                break;
        }
        populateTermEntryUnits(term);
        errorElement.clearAllErrors();

    }

    private void populateTermEntryUnits(ExpressedTermDTO term) {
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
//        setVisibilityForRelatedEntityPanel(false);
        if (tagList != null)
            tagList.selectEntryByDisplayName(TAG_ABNORMAL);
    }

    public void resetConstructionZone() {
        errorElement.clearAllErrors();
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
/*
                    if (termDTO != null)
                        setVisibilityForRelatedEntityPanel(true);
*/
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

    public SimpleErrorElement getErrorElement() {
        return errorElement;
    }

    List<CheckBox> qualityCheckBoxList = new ArrayList<>();

    public void updateEapQualityList(List<EapQualityTermDTO> eapQualityList) {
        qualityListLeft.clear();
        qualityListRight.clear();
        int numOfEntriesFirstCol = eapQualityList.size() / 2;
        CheckBox not = getQualityCheckBox("not");
        qualityCheckBoxList.add(not);
        qualityListLeft.add(not);
        int index = 0;
        for (EapQualityTermDTO qualityTerm : eapQualityList) {
            VerticalPanel panel;
            if (index++ < numOfEntriesFirstCol)
                panel = qualityListLeft;
            else
                panel = qualityListRight;
            CheckBox qualityCheckBox = getQualityCheckBox(qualityTerm.getNickName());
            qualityCheckBoxList.add(qualityCheckBox);
            panel.add(qualityCheckBox);
        }
    }

    public List<CheckBox> getQualityCheckBoxList() {
        return qualityCheckBoxList;
    }

    public void setFxCurationPresenter(FxCurationPresenter fxCurationPresenter) {
        this.fxCurationPresenter = fxCurationPresenter;
    }

    public Map<EntityPart, TermEntry> getTermEntryUnitsMap() {
        return termEntryUnitsMap;
    }

    public Map<EntityPart, List<OntologyDTO>> getTermEntryMap() {
        Map<EntityPart, List<OntologyDTO>> termEntryMap = new TreeMap<>();
        termEntryMap.put(EntityPart.ENTITY_SUPERTERM, superTermEntry.getOntologyList());
        termEntryMap.put(EntityPart.ENTITY_SUBTERM, subTermEntry.getOntologyList());
        return termEntryMap;
    }

}
