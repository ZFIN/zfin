package org.zfin.people.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 */
public interface SessionSaveServiceAsync {
    void saveCuratorUpdate(List<CuratorSessionDTO> curationSessionUpdateList, AsyncCallback async);
}
