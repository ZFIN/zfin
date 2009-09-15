package org.zfin.curation.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * All click listener used for the Experiment section on the FX curation page.
 */
public class ExperimentClickListener {

    private FxExperimentModule.ZfinFlexTable table;

    public ExperimentClickListener(FxExperimentModule.ZfinFlexTable table) {
        this.table = table;
    }

    class AntibodyListClickListener implements ClickListener {

        public void onClick(Widget widget) {
            
        }
    }
}
