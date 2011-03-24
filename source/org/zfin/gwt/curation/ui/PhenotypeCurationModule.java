package org.zfin.gwt.curation.ui;

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
public class PhenotypeCurationModule implements HandlesError {

    // data
    private String publicationID;

    // gui
    private PileConstructionZoneModule pileConstructionZoneModule;
    private AttributionModule attributionModule = new AttributionModule();
    private CurationFilterModule curationFilterModule;
    private MutantModule mutantExpressionModule;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    public PhenotypeCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    private void init() {
        attributionModule.setPublication(publicationID);
        attributionModule.addHandlesErrorListener(this);

        mutantExpressionModule = new MutantModule(publicationID);
        StructurePile structureModule = new PhenotypeStructureModule(publicationID);
        mutantExpressionModule.setPileStructure(structureModule);
        structureModule.setExpressionSection(mutantExpressionModule);
        Map<EntityPart, List<OntologyDTO>> termEntryMap = getTermEntryMap();

        PileConstructionZoneModule constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new PatoPileStructureValidator(termEntryMap));
        constructionZoneModule.setStructurePile(structureModule);
        curationFilterModule = new CurationFilterModule(null, mutantExpressionModule, structureModule, publicationID);
        structureModule.setPileStructureClickListener(constructionZoneModule);
        pileConstructionZoneModule = constructionZoneModule;
    }

    private Map<EntityPart, List<OntologyDTO>> getTermEntryMap() {
        Map<EntityPart, List<OntologyDTO>> termEntryMap = new TreeMap<EntityPart, List<OntologyDTO>>();
        List<OntologyDTO> superterm = new ArrayList<OntologyDTO>(4);
        superterm.add(OntologyDTO.ANATOMY);
        superterm.add(OntologyDTO.GO_BP_MF);
        superterm.add(OntologyDTO.GO_MF);
        superterm.add(OntologyDTO.GO_BP);
        termEntryMap.put(EntityPart.ENTITY_SUPERTERM, superterm);

        List<OntologyDTO> subterm = new ArrayList<OntologyDTO>(4);
        subterm.add(OntologyDTO.ANATOMY);
        subterm.add(OntologyDTO.GO_CC);
        subterm.add(OntologyDTO.GO_MF);
        subterm.add(OntologyDTO.SPATIAL);
        termEntryMap.put(EntityPart.ENTITY_SUBTERM, subterm);

        List<OntologyDTO> quality = new ArrayList<OntologyDTO>(1);
        quality.add(OntologyDTO.QUALITY_QUALITIES);
        quality.add(OntologyDTO.QUALITY_PROCESSES);

        List<OntologyDTO> relatedSuperterm = new ArrayList<OntologyDTO>(3);
        relatedSuperterm.add(OntologyDTO.ANATOMY);
        relatedSuperterm.add(OntologyDTO.GO_MF);
        relatedSuperterm.add(OntologyDTO.GO_BP);
        termEntryMap.put(EntityPart.RELATED_ENTITY_SUPERTERM, relatedSuperterm);

        List<OntologyDTO> relatedSubterm = new ArrayList<OntologyDTO>(3);
        relatedSubterm.add(OntologyDTO.ANATOMY);
        relatedSubterm.add(OntologyDTO.GO_CC);
        relatedSubterm.add(OntologyDTO.SPATIAL);
        relatedSubterm.add(OntologyDTO.GO_MF);
        termEntryMap.put(EntityPart.RELATED_ENTITY_SUBTERM, relatedSubterm);

        termEntryMap.put(EntityPart.QUALITY, quality);

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
        mutantExpressionModule.updateFish();
        curationFilterModule.setInitialValues();
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