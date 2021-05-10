package org.zfin.gwt.root.util;

import com.google.gwt.event.shared.HandlerManager;
import org.zfin.gwt.root.event.AjaxCallEvent;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ZfinModule;

public class AppUtils {

    public static HandlerManager EVENT_BUS = new HandlerManager(null);

    public static void fireAjaxCall(ZfinModule module, AjaxCallEventType callEventType) {
        EVENT_BUS.fireEvent(new AjaxCallEvent(callEventType, module));
    }

    public static native void displayLoadingStatus(String tabName, boolean loading) /*-{
        $wnd.displayLoadingStatus(tabName, loading);
    }-*/;


}
