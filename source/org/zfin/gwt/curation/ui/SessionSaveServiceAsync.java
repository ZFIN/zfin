package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.CuratorSessionDTO;

import java.util.List;

/**
 */
public interface SessionSaveServiceAsync {
    void saveCuratorUpdate(List<CuratorSessionDTO> curationSessionUpdateList, AsyncCallback async);
}
