package org.zfin.curation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import org.zfin.framework.presentation.client.Ontology;
import org.zfin.framework.presentation.client.PostComposedPart;

import java.util.*;

/**
 * Main class for the curation module
 */
public class PatoCurationEntryPoint implements EntryPoint {

    // Publication in question.
    private String publicationID;
    private boolean debug;
    // lookup
    public static final String LOOKUP_PUBLICATION_ID = "zdbID";
    public static final String CURATION_PROPERTIES = "curationProperties";
    public static final String DEBUG = "debug";

    private FxFilterModule filterModule;
    private FxExperimentModule experimentModule;
    private FxExpressionModule expressionModule;
    private FxStructureModule structureModule;
    private PileConstructionZoneModule constructioneZoneModule;

    public void onModuleLoad() {
        loadPublicationAndFilterElements();
/*
        experimentModule = new FxExperimentModule(this);
        expressionModule = new FxExpressionModule(this);
        structureModule = new FxStructureModule(this);
*/


        Map<PostComposedPart, List<Ontology>> termEntryMap = new TreeMap<PostComposedPart, List<Ontology>>();
        List<Ontology> superterm = new ArrayList<Ontology>();
        superterm.add(Ontology.ANATOMY);
        superterm.add(Ontology.GO);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);

        List<Ontology> subterm = new ArrayList<Ontology>();
        subterm.add(Ontology.ANATOMY);
        subterm.add(Ontology.GO);
        termEntryMap.put(PostComposedPart.SUBTERM, subterm);

        List<Ontology> quality = new ArrayList<Ontology>();
        quality.add(Ontology.QUALITY);
        termEntryMap.put(PostComposedPart.QUALITY, quality);

        constructioneZoneModule = new PileConstructionZoneModule(publicationID, termEntryMap);
        constructioneZoneModule.setStructureValidator(new PatoPileStructureValidator(termEntryMap));
/*
        filterModule = new FxFilterModule(this);
*/
        constructioneZoneModule.addCreatePileChangeListener(structureModule);
        constructioneZoneModule.setStructurePile(structureModule);
        //structureModule.setPileStructureClickListener(constructioneZoneModule);
        //showTermInfo(constructioneZoneModule);
    }

    public FxExpressionModule getExpressionModule() {
        return expressionModule;
    }

    public FxExperimentModule getExperimentModule() {
        return experimentModule;
    }

    public FxStructureModule getStructureModule() {
        return structureModule;
    }

    public FxFilterModule getFilterModule() {
        return filterModule;
    }

    public String getPublicationID() {
        return publicationID;
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
     *
     * @param constructioneZoneModule StructureConstructionZoneModule
     */
    private native void showTermInfo(PileConstructionZoneModule constructioneZoneModule)/*-{
      $wnd.showTermInfoString = function (ontology, termID) {
      constructioneZoneModule.@org.zfin.curation.client.PileConstructionZoneModule::showTermInfoString(Ljava/lang/String;Ljava/lang/String;)(ontology, termID);
      };
    }-*/;
}