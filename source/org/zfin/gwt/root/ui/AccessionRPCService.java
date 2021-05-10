package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.ui.FeatureRPCServiceAsync;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 */
public interface AccessionRPCService extends RemoteService {

    public static class App {
        private static AccessionRPCServiceAsync ourInstance = null;

        public static synchronized AccessionRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = (AccessionRPCServiceAsync) GWT.create(AccessionRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/accessionservice");
            }
            return ourInstance;
        }
    }

    String isValidAccession(String accessionNumber, String type);

}
