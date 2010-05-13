package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.CuratorSessionDTO;
import org.zfin.gwt.root.ui.SessionSaveService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for the curation module
 */
public class CurationEntryPoint implements EntryPoint {

    // lookup
    public static final String LOOKUP_PUBLICATION_ID = "zdbID";
    public static final String MODULE_TYPE = "moduleType";
    public static final String CURATION_PROPERTIES = "curationProperties";
    public static final String DEBUG = "debug";

    // Publication in question.
    private String publicationID;
    private CurationModuleType type;

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
        exposeCurationJavaScriptMethods(type.initializeModule(publicationID));
    }

    // Load properties from JavaScript.

    private void loadPublicationAndFilterElements() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary(CURATION_PROPERTIES);
            publicationID = transcriptDictionary.get(LOOKUP_PUBLICATION_ID);
            type = CurationModuleType.getType(transcriptDictionary.get(MODULE_TYPE));
            //Window.alert("module Type: " + type.toString());
        } catch (Exception e) {
            // no properties means session save module.
            Window.alert(e.toString());
        }
    }

    private native void exposeSessionSaveMethodsToJavascript()/*-{
      $wnd.storeSession = function (person, publication, field, value) {
      @org.zfin.gwt.curation.ui.CurationEntryPoint::store(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value);
      };

      $wnd.storeSessionAndRefresh = function (person, publication, field, value, anchor) {
      @org.zfin.gwt.curation.ui.CurationEntryPoint::storeRefresh(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value,anchor);
      };

    }-*/;


    /**
     * Used to support the mouseOver Event on the popup suggestion box.
     *
     * @param constructionZoneModule StructureConstructionZoneModule
     */
    private native void exposeCurationJavaScriptMethods(ConstructionZone constructionZoneModule)/*-{
      $wnd.showTermInfoString = function (ontology, termID) {
      constructionZoneModule.@org.zfin.gwt.curation.ui.ConstructionZone::showTermInfoString(Ljava/lang/String;Ljava/lang/String;)(ontology, termID);
      };
    }-*/;

    @SuppressWarnings({"FeatureEnvy"})
    public static void store(String personZdbID, String publicationZdbID, String field, String value) {
        CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO();
        curatorSessionUpdate.setCuratorZdbID(personZdbID);
        curatorSessionUpdate.setPublicationZdbID(publicationZdbID);
        curatorSessionUpdate.setField(field);
        curatorSessionUpdate.setValue(value);
        timer.scheduleCallback(curatorSessionUpdate, delayTime);
    }

    @SuppressWarnings({"FeatureEnvy"})
    public static void storeRefresh(String personZdbID, String publicationZdbID, String field, String value, String anchor) {
        CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO();
        curatorSessionUpdate.setCuratorZdbID(personZdbID);
        curatorSessionUpdate.setPublicationZdbID(publicationZdbID);
        curatorSessionUpdate.setField(field);
        curatorSessionUpdate.setValue(value);
        List<CuratorSessionDTO> sessionList = new ArrayList<CuratorSessionDTO>(1);
        sessionList.add(curatorSessionUpdate);
        AsyncCallback callbackRefresh = new SessionUpdateCallbackWithURL(anchor);
        SessionSaveService.App.getInstance().saveCuratorUpdate(sessionList, callbackRefresh);
    }


}
