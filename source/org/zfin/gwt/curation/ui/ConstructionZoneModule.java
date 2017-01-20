package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;

import java.util.*;

/**
 * This class is responsible for the construction zone that creates post-composed structures.
 * Initially, it's used for FX but should be extended to also create annotations for PATO.
 * <p>
 * Requirements:<br/>
 * A) Subterm Field: A subterm field in which either AO or GO terms are auto-completed depending on the ontology selector.
 * This field is optional.<br/>
 * B) Superterm Field: A superterm field in which an AO term is completed.<br/>
 * C) Swap Terms: Swapping terms will exchange the superterm's and the subterm's entry. If the subterm is a GO term no swapping is
 * possible as the superterm has to be an AO term.<br/>
 * D) Reset Button: Clicking this button will empty both term fields and set the subterm ontology to AO.
 * <p>
 * Usage:<br/>
 * 1) Constructor with publication ID and termEntryMap that initializes the term parts being used and the Ontologies
 * used for each term.<br/>
 * 2) inject PileStructure object which is called after successful addition of new post-composed term.<br/>
 * 3) inject StructureValidator that validates combinations of post-composed terms.
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

    private FxCurationPresenter fxCurationPresenter;

    private Map<EntityPart, TermEntry> termEntryUnitsMap = new HashMap<>(5);
    private Collection<TermEntry> termEntryUnits = new ArrayList<>(3);
    private List<CheckBox> qualityCheckBoxList = new ArrayList<>(12);
    protected CheckBox notExpressedCheckBox = new CheckBox("not");

    public ConstructionZoneModule() {
        initWidget(uiBinder.createAndBindUi(this));
        createTermEntryUnits();
        notExpressedCheckBox.setStyleName("small red checkbox-label");
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
        clearQualityChecks();
    }

    @UiHandler("superTermEntry")
    void onClickSuperTerm(@SuppressWarnings("unused") ClickEvent event) {
        fxCurationPresenter.populateTermInfo(superTermEntry);
    }

    @UiHandler("subTermEntry")
    void onClickSubTermEntry(@SuppressWarnings("unused") ClickEvent event) {
        fxCurationPresenter.populateTermInfo(subTermEntry);
    }

    public void updateTermInfo(String termName, String ontologyName) {

    }

    private void clearQualityChecks() {
        for (CheckBox checkBox : qualityCheckBoxList)
            checkBox.setValue(false);
        notExpressedCheckBox.setValue(false);
    }

    private CheckBox getQualityCheckBox(String name) {
        CheckBox box = new CheckBox(name);
        box.setStyleName("small");
        if (name.contains("ok"))
            box.addStyleName("phenotype-normal");
        if (name.equals(EapQualityTermDTO.ABSENT_PHENOTYPIC))
            box.addStyleName("red");
        box.addStyleName("checkbox-label");
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
        errorElement.clearAllErrors();
        fxCurationPresenter.submitStructure();
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

    private void populateEapQuality(ExpressedTermDTO term) {
        EapQualityTermDTO dto = term.getQualityTerm();
        if (dto == null) {
            resetCheckBoxes("");
            return;
        }
        resetCheckBoxes(dto.getNickName());
    }

    private void resetCheckBoxes(String nickname) {
        for (CheckBox checkBox : qualityCheckBoxList) {
            if (checkBox.getText().equals(nickname))
                checkBox.setValue(true);
            else
                checkBox.setValue(false);
        }
    }

    protected void populateFullTerm(ExpressedTermDTO term) {
        populateTermEntryUnits(term);
        notExpressedCheckBox.setValue(false);
        populateEapQuality(term);
        if (!term.isExpressionFound() && !term.isEap())
            notExpressedCheckBox.setValue(true);
        errorElement.clearAllErrors();
    }

    protected void populateTermEntryUnits(ExpressedTermDTO term) {
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


    public void initializeEapQualityList(List<EapQualityTermDTO> eapQualityList) {
        qualityListLeft.clear();
        qualityListRight.clear();
        qualityListLeft.add(notExpressedCheckBox);
        int numOfEntriesFirstCol = eapQualityList.size() / 2;
        int index = 0;
        for (EapQualityTermDTO qualityTerm : eapQualityList) {
            VerticalPanel panel;
            if (index++ < numOfEntriesFirstCol)
                panel = qualityListLeft;
            else
                panel = qualityListRight;
            panel.addStyleName("table table-condensed");
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

    public CheckBox getNotExpressedCheckBox() {
        return notExpressedCheckBox;
    }
}
