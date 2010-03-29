package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PostComposedPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Entry point for FX curation module.
 */
public class FxCurationModule {

    private String publicationID;
    private PileConstructionZoneModule pileConstructionZoneModule;

    public FxCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    private void init() {
        FxExperimentModule experimentModule = new FxExperimentModule(publicationID);
        ExpressionSection expressionModule = new FxExpressionModule(experimentModule, publicationID);
        StructurePile structureModule = new FxStructureModule(publicationID);
        expressionModule.setPileStructure(structureModule);
        structureModule.setExpressionSection(expressionModule);
        experimentModule.setExpressionSection(expressionModule);
        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = getTermEntryMap();

        PileConstructionZoneModule constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new FxPileStructureValidator(termEntryMap));
        constructionZoneModule.setStructurePile(structureModule);
        new CurationFilterModule(experimentModule, expressionModule, structureModule, publicationID);
        structureModule.setPileStructureClickListener(constructionZoneModule);
        pileConstructionZoneModule = constructionZoneModule;
    }

    private Map<PostComposedPart, List<OntologyDTO>> getTermEntryMap() {
        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = new TreeMap<PostComposedPart, List<OntologyDTO>>();
        List<OntologyDTO> superterm = new ArrayList<OntologyDTO>(1);
        superterm.add(OntologyDTO.ANATOMY);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);

        List<OntologyDTO> subterm = new ArrayList<OntologyDTO>(2);
        subterm.add(OntologyDTO.ANATOMY);
        subterm.add(OntologyDTO.GO_CC);
        termEntryMap.put(PostComposedPart.SUBTERM, subterm);

        return termEntryMap;
    }

    public PileConstructionZoneModule getPileConstructionZoneModule() {
        return pileConstructionZoneModule;
    }
}
