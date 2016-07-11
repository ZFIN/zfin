package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.ValidationException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface ExperimentRPCService extends RemoteService {

    List<ExperimentDTO> createCondition(String publicationID, ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException;

    List<ExperimentDTO> deleteCondition(ConditionDTO conditionDTO) throws ValidationException, TermNotFoundException;

    List<ExperimentDTO> copyConditions(String experimentID, List<String> copyConditionIdList) throws ValidationException, TermNotFoundException;

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

    List<ExperimentDTO> updateExperiment(ExperimentDTO environmentDTO, String exptName) throws ValidationException;

    List<ExperimentDTO> getExperimentList(String publicationID) throws ValidationException;

    List<ExperimentDTO> createExperiment(String publicationID, ExperimentDTO environmentDTO) throws ValidationException;

    List<ExperimentDTO> deleteExperiment(ExperimentDTO environmentDTO) throws ValidationException;


}
