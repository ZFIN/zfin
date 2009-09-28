package org.zfin.people.presentation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.ArrayList;

/**
 */
public interface SessionSaveServiceAsync {
    void saveCuratorUpdate(List<CuratorSessionDTO> curationSessionUpdateList, AsyncCallback async);
}
