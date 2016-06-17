package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.ValidationException;

import java.util.List;

/**
 */
public interface ExperimentRPCService extends RemoteService {

    List<EnvironmentDTO> createCondition(String publicationID, ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException;

    /**
     * Utility/Convenience class.
     */
    public static class App {
        private static ExperimentRPCServiceAsync ourInstance = null;

        public static synchronized ExperimentRPCServiceAsync getInstance() {
            if (ourInstance == null) {
                ourInstance = GWT.create(ExperimentRPCService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/experimentservice");
            }
            return ourInstance;
        }
    }

    List<EnvironmentDTO> getExperimentList(String publicationID);

}
