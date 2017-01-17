package org.zfin.gwt.curation.ui;

import org.zfin.gwt.curation.event.CurationEvent;

public interface ZfinCurationModule {

    void init();

    void refresh();

    void handleCurationEvent(CurationEvent event);

    void handleTabToggle();
}
