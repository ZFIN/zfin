package org.zfin.gwt.marker.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.CloneDTO;
import org.zfin.gwt.root.dto.CloneTypesDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;

import java.util.List;

/**
 */
public interface CloneRPCService extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use CloneRPCService.App.getInstance() to access static instance of CloneRPCServiceAsync
     */
    public static class App {
        private static CloneRPCServiceAsync ourInstance = null;

        public static synchronized CloneRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (CloneRPCServiceAsync) GWT.create(CloneRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/cloneservice");
            }
            return ourInstance;
        }
    }

    // clone transfer

    CloneDTO getCloneForZdbID(String zdbID);

    CloneTypesDTO getCloneTypes();

    CloneDTO updateCloneData(CloneDTO cloneDTO);

    List<ReferenceDatabaseDTO> getCloneDBLinkAddReferenceDatabases(String markerZdbID);

    void updateCloneHeaders(CloneDTO cloneDTO);


}
