package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.dto.CloneTypesDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;

import java.util.List;

/**
 */
public interface CloneRPCServiceAsync {

    // clone transfer

    void getCloneForZdbID(String zdbID, AsyncCallback<CloneDTO> async);

    void updateCloneData(CloneDTO cloneDTO, AsyncCallback<CloneDTO> async);

    void getCloneTypes(AsyncCallback<CloneTypesDTO> async);

    void getCloneDBLinkAddReferenceDatabases(String markerZdbID, AsyncCallback<List<ReferenceDatabaseDTO>> async);

    void updateCloneHeaders(CloneDTO markerDTO, AsyncCallback<Void> async);

}
