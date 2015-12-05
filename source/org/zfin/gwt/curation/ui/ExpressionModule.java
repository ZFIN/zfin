package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.TermInfoComposite;

import java.util.*;

/**
 * Entry point for FX curation module.
 */
public class ExpressionModule implements HandlesError, EntryPoint {

    public static final String EXPRESSION_ZONE = "expressionZone";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExpressionModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExpressionModule> {
    }

    // data
    private String publicationID;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    AttributionModule attributionModule;
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

    private Map<EntityPart, TermEntry> termEntryUnitsMap = new HashMap<>(5);
    private Map<EntityPart, List<OntologyDTO>> termEntryMap = new HashMap<>();
    private Collection<TermEntry> termEntryUnits = new ArrayList<>(3);

    private final HandlerManager eventBus = new HandlerManager(null);
    private DiseaseModelPresenter diseaseModelPresenter;

    public ExpressionModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
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

    }

    @UiHandler("swapTerms")
    void onClickSwap(@SuppressWarnings("unused") ClickEvent event) {
        errorElement.clearAllErrors();
        getTermEntry(EntityPart.ENTITY_SUBTERM).swapTerms(getTermEntry(EntityPart.ENTITY_SUPERTERM));
    }

    @UiHandler("addButton")
    void onClickAdd(@SuppressWarnings("unused") ClickEvent event) {
        TermDTO disease = termInfoBox.getCurrentTermInfoDTO();
        eventBus.fireEvent(new AddNewDiseaseTermEvent(disease));
        diseaseModelPresenter.clearErrorMessages();
    }

    @Override
    public void onModuleLoad() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(EXPRESSION_ZONE).add(outer);

        subTermEntry.setTermInfoTable(termInfoBox);
        superTermEntry.setTermInfoTable(termInfoBox);

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);

        setTermEntryMap();
        bindEventBusHandler();
        addHandlerEvents();
    }

    private void setTermEntryMap() {
        termEntryMap.put(EntityPart.ENTITY_SUPERTERM, superTermEntry.getOntologyList());
        termEntryMap.put(EntityPart.ENTITY_SUBTERM, subTermEntry.getOntologyList());
        createTermEntryUnits();
    }

    private void createTermEntryUnits() {
        termEntryUnits.add(subTermEntry);
        termEntryUnits.add(superTermEntry);
        termEntryUnitsMap.put(EntityPart.ENTITY_SUPERTERM, superTermEntry);
        termEntryUnitsMap.put(EntityPart.ENTITY_SUBTERM, subTermEntry);
    }

    public void addHandlerEvents() {
        attributionModule.addHandlesErrorListener(new HandlesError() {
            @Override
            public void setError(String message) {

            }

            @Override
            public void clearError() {

            }

            @Override
            public void fireEventSuccess() {
                Window.alert("Kui");
                eventBus.fireEvent(new RemoveAttributeEvent());
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {

            }
        });
    }

    private void bindEventBusHandler() {
        eventBus.addHandler(AddNewDiseaseTermEvent.TYPE,
                new AddNewDiseaseTermHandler() {
                    @Override
                    public void onAddDiseaseTerm(AddNewDiseaseTermEvent event) {
                        diseaseModelPresenter.addDiseaseToSelectionBox(event.getDisease());
                    }
                });
        eventBus.addHandler(ClickTermEvent.TYPE,
                new ClickTermEventHandler() {
                    @Override
                    public void onClickOnTerm(ClickTermEvent event) {
                        superTermEntry.setTerm(event.getTermDTO());
                        termInfoBox.reloadTermInfo(event.getTermDTO(), "termInfo");
                    }
                });
        eventBus.addHandler(ClickTermEvent.TYPE,
                new ClickTermEventHandler() {
                    @Override
                    public void onClickOnTerm(ClickTermEvent event) {
                        subTermEntry.setTerm(event.getTermDTO());
                        termInfoBox.reloadTermInfo(event.getTermDTO(), "termInfo");
                    }
                });
        eventBus.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        diseaseModelPresenter.updateFishList();
                    }
                });

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

}
