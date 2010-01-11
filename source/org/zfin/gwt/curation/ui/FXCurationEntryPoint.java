package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import org.zfin.gwt.curation.dto.CuratorSessionDTO;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.gwt.root.dto.PostComposedPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main class for the curation module
 */
public class FXCurationEntryPoint implements EntryPoint {

    // lookup
    public static final String LOOKUP_PUBLICATION_ID = "zdbID";
    public static final String CURATION_PROPERTIES = "curationProperties";
    public static final String DEBUG = "debug";

    // Publication in question.
    private String publicationID;
    private boolean debug;

    // a list of CuratorSessionUpdateObjects
    private static SessionUpdateCallbackTimer timer = new SessionUpdateCallbackTimer();
    private static final int DEFAULT_DELAY_TIME = 400;
    private static int delayTime = DEFAULT_DELAY_TIME;

    public void onModuleLoad() {
        loadPublicationAndFilterElements();
        exposeSessionSaveMethodsToJavascript();
        // use only the session save module if no pub id is provided.
        if (publicationID == null)
            return;

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
        exposeCurationJavaScriptMethods(constructionZoneModule);
    }

    private Map<PostComposedPart, List<Ontology>> getTermEntryMap() {
        Map<PostComposedPart, List<Ontology>> termEntryMap = new TreeMap<PostComposedPart, List<Ontology>>();
        List<Ontology> superterm = new ArrayList<Ontology>(5);
        superterm.add(Ontology.ANATOMY);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);

        List<Ontology> subterm = new ArrayList<Ontology>(5);
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
	    // no properties means session save module.
            //Window.alert(e.toString());
        }
    }

    private native void exposeSessionSaveMethodsToJavascript()/*-{
      $wnd.storeSession = function (person, publication, field, value) {
      @org.zfin.gwt.curation.ui.FXCurationEntryPoint::store(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value);
      };

      $wnd.storeSessionAndRefresh = function (person, publication, field, value, anchor) {
      @org.zfin.gwt.curation.ui.FXCurationEntryPoint::storeRefresh(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value,anchor);
      };

    }-*/;


    /**
     * Used to support the mouseOver Event on the popup suggestion box.
     *
     * @param constructionZoneModule StructureConstructionZoneModule
     */
    private native void exposeCurationJavaScriptMethods(PileConstructionZoneModule constructionZoneModule)/*-{
      $wnd.showTermInfoString = function (ontology, termID) {
      constructionZoneModule.@org.zfin.gwt.curation.ui.PileConstructionZoneModule::showTermInfoString(Ljava/lang/String;Ljava/lang/String;)(ontology, termID);
      };
    }-*/;

    public static void store(String personZdbID, String publicationZdbID, String field, String value) {
        CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO();
        curatorSessionUpdate.setCuratorZdbID(personZdbID);
        curatorSessionUpdate.setPublicationZdbID(publicationZdbID);
        curatorSessionUpdate.setField(field);
        curatorSessionUpdate.setValue(value);
        timer.scheduleCallback(curatorSessionUpdate, delayTime);
    }

    public static void storeRefresh(String personZdbID, String publicationZdbID, String field, String value, String anchor) {
        CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO();
        curatorSessionUpdate.setCuratorZdbID(personZdbID);
        curatorSessionUpdate.setPublicationZdbID(publicationZdbID);
        curatorSessionUpdate.setField(field);
        curatorSessionUpdate.setValue(value);
        ArrayList<CuratorSessionDTO> sessionList = new ArrayList<CuratorSessionDTO>(1);
        sessionList.add(curatorSessionUpdate);
        SessionUpdateCallbackWithURL callbackRefresh = new SessionUpdateCallbackWithURL(anchor);
        SessionSaveService.App.getInstance().saveCuratorUpdate(sessionList, callbackRefresh);
    }


}
