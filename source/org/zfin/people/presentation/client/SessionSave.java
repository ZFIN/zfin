package org.zfin.people.presentation.client;

import com.google.gwt.core.client.EntryPoint;

import java.util.List;
import java.util.ArrayList;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SessionSave implements EntryPoint {

    // a list of CuratorSessionUpdateObjects
    private static SessionUpdateCallbackTimer timer = new SessionUpdateCallbackTimer() ;
    private static final int DEFAULT_DELAY_TIME = 400 ;
    private static int delayTime = DEFAULT_DELAY_TIME ;


    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        exposeMethodToJavascript();
    }

    public static void store(String personZdbID, String publicationZdbID, String field, String value) {
        CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO() ;
        curatorSessionUpdate.setCuratorZdbID(personZdbID);
        curatorSessionUpdate.setPublicationZdbID(publicationZdbID);
        curatorSessionUpdate.setField(field);
        curatorSessionUpdate.setValue(value);
        timer.scheduleCallback(curatorSessionUpdate,delayTime);
    }

    public static void storeRefresh(String personZdbID, String publicationZdbID, String field, String value, String anchor) {
        CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO() ;
        curatorSessionUpdate.setCuratorZdbID(personZdbID);
        curatorSessionUpdate.setPublicationZdbID(publicationZdbID);
        curatorSessionUpdate.setField(field);
        curatorSessionUpdate.setValue(value);
        List sessionList = new ArrayList() ;
        sessionList.add(curatorSessionUpdate) ;
        SessionUpdateCallbackWithURL callbackRefresh = new SessionUpdateCallbackWithURL(anchor) ;
        SessionSaveService.App.getInstance().saveCuratorUpdate( sessionList, callbackRefresh);
    }



    private native void exposeMethodToJavascript()/*-{
      $wnd.storeSession = function (person, publication, field, value) {
      @org.zfin.people.presentation.client.SessionSave::store(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value);
      };

      $wnd.storeSessionAndRefresh = function (person, publication, field, value, anchor) {
      @org.zfin.people.presentation.client.SessionSave::storeRefresh(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(person, publication, field, value,anchor);
      };

    }-*/;




}
