package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.event.TabEventHandler;
import org.zfin.gwt.root.dto.CuratorSessionDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.AjaxCallEvent;
import org.zfin.gwt.root.event.AjaxCallEventHandler;
import org.zfin.gwt.root.ui.AjaxCallBaseManager;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private boolean debug;
    private CurationModuleType type;
    private AttributionModule attributionModule = new AttributionModule();

    private Map<String, ZfinCurationModule> allModules = new HashMap<>();
    private String moduleName = CurationModuleType.FEATURE_CURATION.getValue();
    // a list of CuratorSessionUpdateObjects
    private static SessionUpdateCallbackTimer timer = new SessionUpdateCallbackTimer();
    private static final int DEFAULT_DELAY_TIME = 400;
    private static int delayTime = DEFAULT_DELAY_TIME;
    private HistoryModule historyModule = new HistoryModule();
    private AjaxCallBaseManager callBaseManager = new AjaxCallBaseManager();

    public void onModuleLoad() {
        loadPublicationAndFilterElements();
        exposeSessionSaveMethodsToJavascript();

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        bindEventBusHandler();

        // use only the session save module if no pub id is provided.
        if (publicationID != null) {
            if (type != null) {
                ZfinCurationModule module = type.initializeModule(publicationID);
                if (module != null)
                    allModules.put(moduleName, module);
            }
            // let the entry tab (module) load first and then all others...
            Scheduler.get().scheduleDeferred(new Command() {
                @Override
                public void execute() {
                    for (CurationModuleType otherType : CurationModuleType.getOtherTypes(type)) {
                        ZfinCurationModule model = otherType.initializeModule(publicationID);
                        if (model != null)
                            allModules.put(otherType.getValue(), model);
                    }
                }
            });

        }
        exposeRefreshTabMethodsToJavascript(this);
/*
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                ensureNotUmbrellaError(e);
            }
        });
*/
    }

    private static void ensureNotUmbrellaError(Throwable e) {
        for (Throwable th : ((UmbrellaException) e).getCauses()) {
            if (th instanceof UmbrellaException) {
                ensureNotUmbrellaError(th);
            } else {
                th.printStackTrace();
            }
        }
    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(CurationEvent.TYPE,
                new TabEventHandler() {
                    @Override
                    public void onEvent(CurationEvent event) {
                        if (event.getEventType().is(EventType.MARKER_ATTRIBUTION) ||
                                event.getEventType().is(EventType.MARKER_DEATTRIBUTION))
                            refreshOrthologyGeneList();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(CurationEvent.TYPE,
                new TabEventHandler() {
                    @Override
                    public void onEvent(CurationEvent event) {
                        notifyModules(event);
                        logEvent(event);
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AjaxCallEvent.TYPE,
                new AjaxCallEventHandler() {
                    @Override
                    public void onAjaxCall(AjaxCallEvent event) {
                        callBaseManager.handleAjaxCallEvent(event);
                    }
                });

    }

    private void logEvent(CurationEvent event) {
        historyModule.addItem(event);
    }

    private static native void refreshOrthologyGeneList() /*-{
        $wnd.refreshOrthologyGeneList();
    }-*/;

    public void notifyModules(CurationEvent event) {
        for (ZfinCurationModule module : allModules.values()) {
            module.handleCurationEvent(event);
        }
        attributionModule.handleCurationEvent(event);
    }


    // Load properties from JavaScript.

    private void loadPublicationAndFilterElements() {

        try {
            Dictionary transcriptDictionary = Dictionary.getDictionary(CURATION_PROPERTIES);
            publicationID = transcriptDictionary.get(LOOKUP_PUBLICATION_ID);
            if (!StringUtils.isEmpty(transcriptDictionary.get(DEBUG)))
                debug = Boolean.parseBoolean(transcriptDictionary.get(DEBUG));
            String moduleNameClient = transcriptDictionary.get(MODULE_TYPE).toUpperCase();
            if (StringUtils.isNotEmpty(moduleNameClient))
                moduleName = moduleNameClient;
            type = CurationModuleType.getType(moduleName);
            //Window.alert("module Type: " + type.toString());
        } catch (Exception e) {
            // no properties means session save module.
            GWT.log(e.toString());
        }
    }

    private native void exposeRefreshTabMethodsToJavascript(CurationEntryPoint curationModule)/*-{
        $wnd.refreshTab = function (tabName) {
            curationModule.@org.zfin.gwt.curation.ui.CurationEntryPoint::refreshTab(Ljava/lang/String;)(tabName);
        };

        $wnd.handleTabToggle = function (tabName) {
            curationModule.@org.zfin.gwt.curation.ui.CurationEntryPoint::handleTabToggle(Ljava/lang/String;)(tabName);
        };

        $wnd.showHistory = function () {
            curationModule.@org.zfin.gwt.curation.ui.CurationEntryPoint::showHistory()();
        };


        $wnd.updateTermInfoBox = function (termName, ontologyName, tabName) {
            curationModule .@org.zfin.gwt.curation.ui.CurationEntryPoint::updateTermInfo(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(termName, ontologyName, tabName);
        };
    }-*/;

    private native void exposeSessionSaveMethodsToJavascript()/*-{
        $wnd.storeSession = function (person, publication, field, value) {
            @org.zfin.gwt.curation.ui.CurationEntryPoint::store(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value);
        };

        $wnd.storeSessionAndRefresh = function (person, publication, field, value, anchor) {
            @org.zfin.gwt.curation.ui.CurationEntryPoint::storeRefresh(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value, anchor);
        };

        $wnd.refreshFigures = function () {
            @org.zfin.gwt.curation.ui.CurationEntryPoint::refreshFigureLists()();
        };
    }-*/;

    public void showHistory() {
        historyModule.popupPanel.show();
    }

    public void refreshTab(String tabName) {
        ZfinCurationModule module = allModules.get(tabName.toUpperCase());
        if (module == null) {
            return;
        }
        module.refresh();
        attributionModule.refresh();
    }

    public void updateTermInfo(String termName, String ontologyName, String tabName) {
        ZfinCurationModule module = allModules.get(tabName.toUpperCase());
        if (module == null) {
            return;
        }
        module.updateTermInfo(termName, ontologyName);
    }



    public void handleTabToggle(String tabName) {
        ZfinCurationModule module = allModules.get(tabName.toUpperCase());
        if (module == null) {
            Window.alert("Could not find tab " + tabName);
            return;
        }
        module.handleTabToggle();
    }

    public static void refreshFigureLists() {
        AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.ADD_FIGURE));
    }

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
