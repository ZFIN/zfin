package org.zfin.gwt.curation.ui;

import org.zfin.gwt.curation.event.ChangeCurationFilterEvent;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Entry Point for Phenotype curation tab module.
 */
public class PhenotypeCurationModule implements ZfinCurationModule, HandlesError {

    // data
    private String publicationID;

    // gui
    private CurationFilterModule curationFilterModule;
    private MutantModule mutantExpressionModule;
    private PileConstructionZoneModule constructionZoneModule;
    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    public PhenotypeCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    public void init() {
        mutantExpressionModule = new MutantModule(publicationID);
        StructurePile structureModule = new PhenotypeStructureModule(publicationID);
        mutantExpressionModule.setPileStructure(structureModule);
        structureModule.setExpressionSection(mutantExpressionModule);
        Map<EntityPart, List<OntologyDTO>> termEntryMap = getTermEntryMap();

        constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new PatoPileStructureValidator(termEntryMap));
        constructionZoneModule.setStructurePile(structureModule);
        curationFilterModule = new CurationFilterModule(mutantExpressionModule, structureModule, publicationID);
        structureModule.setPileStructureClickListener(constructionZoneModule);
    }

    @Override
    public void refresh() {
        mutantExpressionModule.reInit();
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {
        if (event.getEventType().is(EventType.FILTER)) {
            ChangeCurationFilterEvent changeEvent = (ChangeCurationFilterEvent) event;
            //curationFilterModule.readSavedFilterValues();
            if (event.getEventType().is(EventType.FX_TAB)) {
                mutantExpressionModule.setExperimentFilter(changeEvent.getExperimentFilter());
                mutantExpressionModule.setFigureID(changeEvent.getFigureID());
                mutantExpressionModule.runModule();
                curationFilterModule.setFilterValues(changeEvent.getExperimentFilter(), changeEvent.getFigureID());
            }
        }
        if (event.getEventType().is(EventType.FISH)) {
            mutantExpressionModule.retrieveFishList();
            curationFilterModule.refreshFishList();
        }
        if (event.getEventType().is(EventType.CUD_EXPERIMENT_CONDITION)) {
            mutantExpressionModule.retrieveExperimentConditionList();
        }
        if (event.getEventType().is(EventType.ADD_REMOVE_ATTRIBUTION_FEATURE)) {
            curationFilterModule.refreshFeatureList();
        }
        if (event.getEventType().is(EventType.PUSH_TO_PATO)) {
            mutantExpressionModule.retrieveExpressions();
        }
        if (event.getEventType().is(EventType.ADD_FIGURE)) {
            mutantExpressionModule.refreshFigureList();
            curationFilterModule.refreshFigureList();
        }
    }

    @Override
    public void handleTabToggle() {
        mutantExpressionModule.retrieveEaps();
    }

    @Override
    public void updateTermInfo(String termName, String ontologyName) {
        constructionZoneModule.updateTermInfoBox(termName, ontologyName);
    }

    private Map<EntityPart, List<OntologyDTO>> getTermEntryMap() {
        Map<EntityPart, List<OntologyDTO>> termEntryMap = new TreeMap<>();
        List<OntologyDTO> superterm = new ArrayList<>(4);
        superterm.add(OntologyDTO.ANATOMY);
        superterm.add(OntologyDTO.GO_BP_MF);
        superterm.add(OntologyDTO.GO_MF);
        superterm.add(OntologyDTO.GO_BP);
        termEntryMap.put(EntityPart.ENTITY_SUPERTERM, superterm);

        List<OntologyDTO> subterm = new ArrayList<>(4);
        subterm.add(OntologyDTO.ANATOMY);
        subterm.add(OntologyDTO.GO_CC);
        subterm.add(OntologyDTO.GO_MF);
        subterm.add(OntologyDTO.GO_BP);
        subterm.add(OntologyDTO.SPATIAL);
        subterm.add(OntologyDTO.CHEBI);
        subterm.add(OntologyDTO.MPATH_NEOPLASM);
        termEntryMap.put(EntityPart.ENTITY_SUBTERM, subterm);

        List<OntologyDTO> quality = new ArrayList<>(1);
        quality.add(OntologyDTO.QUALITY_QUALITIES);
        quality.add(OntologyDTO.QUALITY_PROCESSES);

        List<OntologyDTO> relatedSuperterm = new ArrayList<>(3);
        relatedSuperterm.add(OntologyDTO.ANATOMY);
        relatedSuperterm.add(OntologyDTO.GO_MF);
        relatedSuperterm.add(OntologyDTO.GO_BP);
        termEntryMap.put(EntityPart.RELATED_ENTITY_SUPERTERM, relatedSuperterm);

        List<OntologyDTO> relatedSubterm = new ArrayList<>(3);
        relatedSubterm.add(OntologyDTO.ANATOMY);
        relatedSubterm.add(OntologyDTO.GO_CC);
        relatedSubterm.add(OntologyDTO.SPATIAL);
        relatedSubterm.add(OntologyDTO.GO_MF);
        relatedSubterm.add(OntologyDTO.CHEBI);
        relatedSubterm.add(OntologyDTO.MPATH_NEOPLASM);
        termEntryMap.put(EntityPart.RELATED_ENTITY_SUBTERM, relatedSubterm);

        termEntryMap.put(EntityPart.QUALITY, quality);

        return termEntryMap;
    }

    @Override
    public void setError(String message) {
    }

    @Override
    public void clearError() {
        mutantExpressionModule.updateFish();
        curationFilterModule.setInitialValues();
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

}