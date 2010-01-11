package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.dto.CuratorSessionDTO;

import java.util.List;

/**
 */
public interface SessionSaveService extends RemoteService {


    /**
     * Utility/Convenience class.
     * Use SessionSaveService.App.getInstance () to access static instance of SessionSaveServiceAsync
     */
    public static class App {
        private static SessionSaveServiceAsync application = null;

        public static synchronized SessionSaveServiceAsync getInstance() {
            if (application == null) {
                application = (SessionSaveServiceAsync) GWT.create(SessionSaveService.class);
                ((ServiceDefTarget) application).setServiceEntryPoint("/ajax/session-save");
            }
            return application;
        }
    }

    void saveCuratorUpdate(List<CuratorSessionDTO> curationSessionUpdateList);
}
