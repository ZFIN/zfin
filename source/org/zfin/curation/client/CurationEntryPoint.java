package org.zfin.curation.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;

/**
 * Main class for the curation module
 */
public class CurationEntryPoint implements EntryPoint {

    // Not needed until we integrate the expression section
    //private FxFilterTable filterTable = new FxFilterTable(experimentTable);

    public void onModuleLoad() {
        new DisplayExperimentTable();
    }
}
