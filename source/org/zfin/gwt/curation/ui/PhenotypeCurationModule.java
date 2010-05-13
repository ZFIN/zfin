package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PostComposedPart;
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
        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = getTermEntryMap();

        PileConstructionZoneModule constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new PatoPileStructureValidator(termEntryMap));
        constructionZoneModule.setStructurePile(structureModule);
        curationFilterModule = new CurationFilterModule(null, mutantExpressionModule, structureModule, publicationID);
        structureModule.setPileStructureClickListener(constructionZoneModule);
        pileConstructionZoneModule = constructionZoneModule;
    }

    private Map<PostComposedPart, List<OntologyDTO>> getTermEntryMap() {
        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = new TreeMap<PostComposedPart, List<OntologyDTO>>();
        List<OntologyDTO> superterm = new ArrayList<OntologyDTO>(4);
        superterm.add(OntologyDTO.ANATOMY);
        superterm.add(OntologyDTO.GO_BP_MF);
        superterm.add(OntologyDTO.GO_MF);
        superterm.add(OntologyDTO.GO_BP);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);

        List<OntologyDTO> subterm = new ArrayList<OntologyDTO>(3);
        subterm.add(OntologyDTO.ANATOMY);
        subterm.add(OntologyDTO.GO_CC);
        subterm.add(OntologyDTO.GO_MF);
        termEntryMap.put(PostComposedPart.SUBTERM, subterm);

        List<OntologyDTO> quality = new ArrayList<OntologyDTO>(1);
        quality.add(OntologyDTO.QUALITY_QUALITIES);
        quality.add(OntologyDTO.QUALITY_PROCESSES);
/*
        quality.add(OntologyDTO.QUALITY_QUALITATIVE);
        quality.add(OntologyDTO.QUALITY_QUALITIES_RELATIONAL);
        quality.add(OntologyDTO.QUALITY_PROCESSES_RELATIONAL);
*/
        quality.add(OntologyDTO.QUALITY);
        termEntryMap.put(PostComposedPart.QUALITY, quality);

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