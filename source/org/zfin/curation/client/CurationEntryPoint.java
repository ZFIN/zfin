package org.zfin.curation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;

/**
 * Main class for the curation module
 */
public class CurationEntryPoint implements EntryPoint {

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

    public void onModuleLoad() {
        loadPublicationAndFilterElements();
        experimentModule = new FxExperimentModule(this);
        expressionModule = new FxExpressionModule(this);
        structureModule = new FxStructureModule(this);
        filterModule = new FxFilterModule(this);
        exposeMethodToJavascript(structureModule);
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

    private native void exposeMethodToJavascript(FxStructureModule structureModule)/*-{
      $wnd.addNewComposedTerm = function (publication, superterm, subterm, ontology) {
      structureModule.@org.zfin.curation.client.FxStructureModule::addNewPileStructure(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(publication, superterm, subterm, ontology);
      structureModule.@org.zfin.curation.client.FxStructureModule::clearErrorMessages()();
      };

    }-*/;
}
