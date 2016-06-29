package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RetrieveEnvironmentListCallBack extends ZfinAsyncCallback<List<EnvironmentDTO>> {

    private ListBox environmentList;

    public RetrieveEnvironmentListCallBack(ListBox environmentList, String errorMessage, ErrorHandler errorLabel) {
        super(errorMessage, errorLabel);
        this.environmentList = environmentList;
    }

    public void onSuccess(List<EnvironmentDTO> environments) {
        //Window.alert("brought back: " + experiments.size() );
        environmentList.clear();
        for (EnvironmentDTO environmentDTO : environments) {
            String name = environmentDTO.getName();
            if (name.startsWith("_"))
                name = name.substring(1);
            environmentList.addItem(name, environmentDTO.getZdbID());
        }
    }

    public void onFailureCleanup() {
    }

}
