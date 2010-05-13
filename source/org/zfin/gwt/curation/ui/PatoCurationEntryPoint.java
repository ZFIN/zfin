package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PostComposedPart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    private CurationFilterModule filterModule;
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


        Map<PostComposedPart, List<OntologyDTO>> termEntryMap = new TreeMap<PostComposedPart, List<OntologyDTO>>();
        List<OntologyDTO> superterm = new ArrayList<OntologyDTO>();
        superterm.add(OntologyDTO.ANATOMY);
        superterm.add(OntologyDTO.GO);
        termEntryMap.put(PostComposedPart.SUPERTERM, superterm);

        List<OntologyDTO> subterm = new ArrayList<OntologyDTO>();
        subterm.add(OntologyDTO.ANATOMY);
        subterm.add(OntologyDTO.GO);
        termEntryMap.put(PostComposedPart.SUBTERM, subterm);

        List<OntologyDTO> quality = new ArrayList<OntologyDTO>();
        quality.add(OntologyDTO.QUALITY);
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

    public CurationFilterModule getFilterModule() {
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
     * @param constructionZoneModule StructureConstructionZoneModule
     */
    private native void showTermInfo(PileConstructionZoneModule constructionZoneModule)/*-{
      $wnd.showTermInfoString = function (ontology, termID) {
      constructioneZoneModule.@org.zfin.gwt.curation.ui.PileConstructionZoneModule::showTermInfoString(Ljava/lang/String;Ljava/lang/String;)(ontology, termID);
      };
    }-*/;
}