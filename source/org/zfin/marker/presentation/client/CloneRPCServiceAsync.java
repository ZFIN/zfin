package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.marker.presentation.dto.CloneDTO;
import org.zfin.marker.presentation.dto.CloneTypesDTO;
import org.zfin.marker.presentation.dto.ReferenceDatabaseDTO;

import java.util.List;

/**
 */
public interface CloneRPCServiceAsync {

    // clone transfer

    void getCloneForZdbID(String zdbID, AsyncCallback<CloneDTO> async);

    void updateCloneData(CloneDTO cloneDTO, AsyncCallback<CloneDTO> async);

    void getCloneTypes(AsyncCallback<CloneTypesDTO> async);


    void getCloneDBLinkAddReferenceDatabases(String markerZdbID, AsyncCallback<List<ReferenceDatabaseDTO>> async);
}
