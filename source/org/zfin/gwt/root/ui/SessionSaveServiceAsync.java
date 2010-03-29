package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.CuratorSessionDTO;

import java.util.List;

/**
 */
public interface SessionSaveServiceAsync {
    void saveCuratorUpdate(List<CuratorSessionDTO> curationSessionUpdateList, AsyncCallback async);

    /**
     * Update a single curator session object.
     *
     * @param curatorSessionUpdate Curator session DTO
     * @param callbackRefresh      callback
     */
    void updateCuratorSession(CuratorSessionDTO curatorSessionUpdate, AsyncCallback callbackRefresh);

    /**
     * Retrieve session variable box size.
     *
     * @param publicationID publication
     * @param boxDivID      name of div element
     * @param callback      callback
     */
    void readBoxSizeFromSession(String publicationID, String boxDivID, AsyncCallback callback);

    /**
     * Retrieve the stage selector mode.
     *
     * @param publicationID publication ID
     * @param callback callback
     */
    void isStageSelectorSingleMode(String publicationID, AsyncCallback<Boolean> callback);

    /**
     * Set the stage selector mode.
     * @param isSingleMode single - multi
     * @param publicationID publication
     * @param callback callback
     */
    void setStageSelectorSingleMode(boolean isSingleMode, String publicationID, AsyncCallback<Void> callback);
}
