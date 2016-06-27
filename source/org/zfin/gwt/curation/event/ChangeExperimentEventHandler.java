package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.EventHandler;

public interface ChangeExperimentEventHandler extends EventHandler {
    void onAdd(ChangeExperimentEvent event);
    void onUpdate(ChangeExperimentEvent event);
    void onDelete(ChangeExperimentEvent event);
}
