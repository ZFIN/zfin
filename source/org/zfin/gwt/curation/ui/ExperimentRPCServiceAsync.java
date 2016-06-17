package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

public interface ExperimentRPCServiceAsync {

    void getExperimentList(String publicationID, AsyncCallback<List<EnvironmentDTO>> callback);

    void createCondition(String publicationID, ConditionDTO conditionDTO, AsyncCallback<List<EnvironmentDTO>> callback);

    void deleteCondition(ConditionDTO conditionDTO, AsyncCallback<List<EnvironmentDTO>> callBack);
}

