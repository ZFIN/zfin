package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.fusesource.restygwt.client.Method;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsynchronousCallback;
import org.zfin.gwt.root.ui.ZfinModule;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RetrieveEnvironmentListCallBack extends ZfinAsynchronousCallback<List<ExperimentDTO>> {

    private ListBox environmentList;

    public RetrieveEnvironmentListCallBack(ListBox environmentList, String errorMessage, ErrorHandler errorLabel, ZfinModule module) {
        super(errorMessage, errorLabel, module, AjaxCallEventType.GET_ENVIRONMENT_LIST_STOP);
        this.environmentList = environmentList;
    }

    public void onSuccess(Method method, List<ExperimentDTO> environments) {
        //Window.alert("brought back: " + experiments.size() );
        super.onFinish();
        environmentList.clear();
        for (ExperimentDTO environmentDTO : environments) {
            String name = environmentDTO.getName();
            if (name.startsWith("_"))
                name = name.substring(1);
            environmentList.addItem(name, environmentDTO.getZdbID());
        }
    }

    public void onFailureCleanup() {
    }

}
