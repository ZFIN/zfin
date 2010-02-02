package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.AntibodyTypesDTO;

/**
 */
public interface AntibodyRPCServiceAsync {

    void getAntibodyForZdbID(String zdbID, AsyncCallback<AntibodyDTO> markerEditCallBack);

    void getAntibodyTypes(AsyncCallback<AntibodyTypesDTO> asyncCallback);

    void updateAntibodyData(AntibodyDTO antibodyDTO, AsyncCallback<AntibodyDTO> async);

    void updateAntibodyHeaders(AntibodyDTO dto, AsyncCallback<Void> async);
}