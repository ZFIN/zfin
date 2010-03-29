package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PostComposedPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Entry Point for Phenotype curation tab module.
 */
public class PhenotypeCurationModule {

    private String publicationID;
    private PileConstructionZoneModule pileConstructionZoneModule;

    public PhenotypeCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    private void init() {
        ExpressionSection expressionModule = new MutantModule(publicationID);
        StructurePile structureModule = new PhenotypeStructureModule(publicationID);
        expressionModule.setPileStructure(structureModule);
        structureModule.setExpressionSection(expressionModule);
        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = getTermEntryMap();

        PileConstructionZoneModule constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new PatoPileStructureValidator(termEntryMap));
        constructionZoneModule.setStructurePile(structureModule);
        new CurationFilterModule(null, expressionModule, structureModule, publicationID);
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
}