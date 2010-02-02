package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.AntibodyDTO;
import org.zfin.gwt.root.dto.AntibodyTypesDTO;

/**
 */
public interface AntibodyRPCService extends RemoteService {

    /**
     * Utility/Convenience class.
     */
    public static class App {
        private static AntibodyRPCServiceAsync ourInstance = null;

        public static synchronized AntibodyRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (AntibodyRPCServiceAsync) GWT.create(AntibodyRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/antibodyservice");
            }
            return ourInstance;
        }
    }

    AntibodyDTO getAntibodyForZdbID(String zdbID);

    AntibodyTypesDTO getAntibodyTypes();

    AntibodyDTO updateAntibodyData(AntibodyDTO antibodyDTO);

    void updateAntibodyHeaders(AntibodyDTO dto);
}