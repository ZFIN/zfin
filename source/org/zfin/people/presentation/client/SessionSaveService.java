package org.zfin.people.presentation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import java.util.List;

/**
 */
public interface SessionSaveService extends RemoteService {


    /**
     * Utility/Convenience class.
     * Use SessionSaveService.App.getInstance () to access static instance of SessionSaveServiceAsync
     */
    public static class App {
        private static SessionSaveServiceAsync app = null;

        public static synchronized SessionSaveServiceAsync getInstance() {
            if (app == null) {
                app = (SessionSaveServiceAsync) GWT.create(SessionSaveService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint("/ajax/session-save");
            }
            return app;
        }
    }

    void saveCuratorUpdate(List<CuratorSessionDTO> curationSessionUpdateList);
}
