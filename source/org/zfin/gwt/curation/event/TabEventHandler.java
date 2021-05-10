package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.EventHandler;

public interface TabEventHandler extends EventHandler {
    void onEvent(CurationEvent event);
}
