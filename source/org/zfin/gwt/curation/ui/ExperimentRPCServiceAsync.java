package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExperimentRPCServiceAsync {

    void getExperimentList(String publicationID, AsyncCallback<List<ExperimentDTO>> callback);

    void createCondition(String publicationID, ConditionDTO conditionDTO, AsyncCallback<List<ExperimentDTO>> callback);
    void createExperiment(String publicationID, ExperimentDTO environmentDTO, AsyncCallback<List<ExperimentDTO>> callback);

    void deleteCondition(ConditionDTO conditionDTO, AsyncCallback<List<ExperimentDTO>> callBack);

    void deleteExperiment(ExperimentDTO environmentDTO, AsyncCallback<List<ExperimentDTO>> callBack);
    void updateExperiment(ExperimentDTO environmentDTO, String exptName,AsyncCallback<List<ExperimentDTO>> callBack);

    void copyConditions(String experimentID, List<String> copyConditionIdList, AsyncCallback<List<ExperimentDTO>> callback);

    void getChildMap(AsyncCallback<Map<String,Set<String>>> callback);
}

