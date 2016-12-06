package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;

public class CurationServiceHelper {

    private CurationService service = GWT.create(CurationService.class);

    private CurationServiceHelper instance = new CurationServiceHelper();

    public CurationServiceHelper getInstance() {
        return instance;
    }


}
