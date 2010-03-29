package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.CuratorSessionDTO;

import java.util.List;

/**
 */
public interface SessionSaveService extends RemoteService {

    /**
     * Update a single curator session object.
     *
     * @param curatorSessionUpdate Curator session DTO
     */
    void updateCuratorSession(CuratorSessionDTO curatorSessionUpdate);

    /**
     * Retrieve session variable box size.
     *
     * @param publicationID publication
     * @param boxDivID      name of div element
     */
    public CuratorSessionDTO readBoxSizeFromSession(String publicationID, String boxDivID);

    /**
     * Retrieve the stage selector mode.
     *
     * @param publicationID publication ID
     */
    boolean isStageSelectorSingleMode(String publicationID);

    /**
     * Set the stage selector mode.
     *
     * @param isSingleMode  single - multi
     * @param publicationID publication
     */
    void setStageSelectorSingleMode(boolean isSingleMode, String publicationID);


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
