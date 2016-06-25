package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface ExperimentRPCService extends RemoteService {

    List<EnvironmentDTO> createCondition(String publicationID, ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException;

    List<EnvironmentDTO> deleteCondition(ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException;

    List<EnvironmentDTO> copyConditions(String experimentID, List<String> copyConditionIdList) throws ValidationException, TermNotFoundException;

    Map<String, Set<String>> getChildMap();

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

    List<EnvironmentDTO> updateExperiment(EnvironmentDTO environmentDTO, String exptName) throws ValidationException;

    List<EnvironmentDTO> getExperimentList(String publicationID) throws ValidationException;

    List<EnvironmentDTO> createExperiment(String publicationID, EnvironmentDTO environmentDTO) throws ValidationException;

    List<EnvironmentDTO> deleteExperiment(EnvironmentDTO environmentDTO) throws ValidationException;


}
