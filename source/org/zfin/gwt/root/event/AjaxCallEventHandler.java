package org.zfin.gwt.root.event;

import com.google.gwt.event.shared.EventHandler;

public interface AjaxCallEventHandler extends EventHandler {
    void onAjaxCall(AjaxCallEvent event);
}
