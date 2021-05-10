package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.EventHandler;

public interface DirtyValueEventHandler extends EventHandler {
    void onDirtyEvent(DirtyValueEvent event);
}
