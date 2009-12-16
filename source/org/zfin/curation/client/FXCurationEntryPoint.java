package org.zfin.curation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import org.zfin.framework.presentation.client.PostComposedPart;
import org.zfin.framework.presentation.client.Ontology;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Main class for the curation module
 */
public class FXCurationEntryPoint implements EntryPoint {

    // Publication in question.
    private String publicationID;
    private boolean debug;
    // lookup
    public static final String LOOKUP_PUBLICATION_ID = "zdbID";
    public static final String CURATION_PROPERTIES = "curationProperties";
    public static final String DEBUG = "debug";

    public void onModuleLoad() {
        loadPublicationAndFilterElements();
        FxExperimentModule experimentModule = new FxExperimentModule(publicationID);
        ExpressionSection expressionModule = new FxExpressionModule(experimentModule, publicationID);
        StructurePile structureModule = new FxStructureModule(publicationID);
        expressionModule.setPileStructure(structureModule);
        structureModule.setExpressionSection(expressionModule);
        Map<PostComposedPart, List<Ontology>> termEntryMap = getTermEntryMap();

        PileConstructionZoneModule constructionZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructionZoneModule.setStructureValidator(new FxPileStructureValidator(termEntryMap));
        FxFilterModule filterModule = new FxFilterModule(publicationID);
        filterModule.setExperimentSection(experimentModule);
        filterModule.setExpressionSection(expressionModule);
        filterModule.setPileStructure(structureModule);
        constructionZoneModule.addCreatePileChangeListener(structureModule);
        constructionZoneModule.setStructurePile(structureModule);
        structureModule.setPileStructureClickListener(constructionZoneModule);
        showTermInfo(constructionZoneModule);
    }

    private Map<PostComposedPart, List<Ontology>> getTermEntryMap() {
        Map<PostComposedPart, List<Ontology>> termEntryMap = new TreeMap<PostComposedPart, List<Ontology>>();
        List<Ontology> superterm = new ArrayList<Ontology>();
        superterm.add(Ontology.ANATOMY);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);

        List<Ontology> subterm = new ArrayList<Ontology>();
        subterm.add(Ontology.ANATOMY);
        subterm.add(Ontology.GO_CC);
        termEntryMap.put(PostComposedPart.SUBTERM, subterm);

        return termEntryMap;
    }

    public boolean isDebug() {
        return debug;
    }

    // Load properties from JavaScript.
    private void loadPublicationAndFilterElements() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary(CURATION_PROPERTIES);
            publicationID = transcriptDictionary.get(LOOKUP_PUBLICATION_ID);
            String debugStr = transcriptDictionary.get(DEBUG);
            if (debugStr != null && debugStr.equals(Boolean.TRUE.toString()))
                debug = true;
        } catch (Exception e) {
            Window.alert(e.toString());
        }
    }

    /**
     * Used to support the mouseOver Event on the popup suggestion box.
     * @param constructionZoneModule  StructureConstructionZoneModule
     */
    private native void showTermInfo(PileConstructionZoneModule constructionZoneModule)/*-{
      $wnd.showTermInfoString = function (ontology, termID) {
      constructionZoneModule.@org.zfin.curation.client.PileConstructionZoneModule::showTermInfoString(Ljava/lang/String;Ljava/lang/String;)(ontology, termID);
      };
    }-*/;
}
