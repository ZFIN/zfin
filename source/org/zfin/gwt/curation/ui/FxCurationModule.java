package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Entry point for FX curation module.
 */
public class FxCurationModule implements HandlesError {

    // data
    private String publicationID;

    // gui
    private PileConstructionZoneModule pileConstructionZoneModule;
    private FxExperimentModule experimentModule;
    private CurationFilterModule experimentFilterModule;
    private AttributionModule attributionModule = new AttributionModule();

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    public FxCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    private void init() {
        attributionModule.setPublication(publicationID);
        attributionModule.addHandlesErrorListener(this);

        experimentModule = new FxExperimentModule(publicationID);
        experimentModule.addHandlesErrorListener(this);
        ExpressionSection expressionModule = new FxExpressionModule(experimentModule, publicationID);
        StructurePile structureModule = new FxStructureModule(publicationID);
        expressionModule.setPileStructure(structureModule);
        structureModule.setExpressionSection(expressionModule);
        experimentModule.setExpressionSection(expressionModule);
        Map<EntityPart, List<OntologyDTO>> termEntryMap = getTermEntryMap();

        PileConstructionZoneModule constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new FxPileStructureValidator(termEntryMap));
        constructionZoneModule.setStructurePile(structureModule);
        experimentFilterModule = new CurationFilterModule(experimentModule, expressionModule, structureModule, publicationID);
        structureModule.setPileStructureClickListener(constructionZoneModule);
        pileConstructionZoneModule = constructionZoneModule;
    }

    private Map<EntityPart, List<OntologyDTO>> getTermEntryMap() {
        Map<EntityPart, List<OntologyDTO>> termEntryMap = new TreeMap<EntityPart, List<OntologyDTO>>();
        List<OntologyDTO> superterm = new ArrayList<OntologyDTO>(1);
        superterm.add(OntologyDTO.ANATOMY);
        termEntryMap.put(EntityPart.ENTITY_SUPERTERM, superterm);

        List<OntologyDTO> subterm = new ArrayList<OntologyDTO>(3);
        subterm.add(OntologyDTO.ANATOMY);
        subterm.add(OntologyDTO.GO_CC);
        subterm.add(OntologyDTO.SPATIAL);
        termEntryMap.put(EntityPart.ENTITY_SUBTERM, subterm);

        return termEntryMap;
    }

    public PileConstructionZoneModule getPileConstructionZoneModule() {
        return pileConstructionZoneModule;
    }

    @Override
    public void setError(String message) {
    }

    @Override
    public void clearError() {
        attributionModule.clearError();
        revertGUI();
    }

    private void revertGUI() {
        experimentModule.updateGenes();
        attributionModule.revertGUI();
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
