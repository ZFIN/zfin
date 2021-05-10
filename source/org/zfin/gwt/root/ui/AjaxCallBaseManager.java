package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import org.zfin.gwt.root.event.AjaxCallEvent;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjaxCallBaseManager {

    private Map<String, List<AjaxCallEventType>> eventMap = new HashMap<>();
    private List<AjaxCallEventType> allEventTypeList = new ArrayList<>(100);

    public void handleAjaxCallEvent(AjaxCallEvent event) {
        //GWT.log("Tab Name: " + tabName);
        String tabName = event.getModule().getTabName();
        AjaxCallEventType eventType = event.getEventType();
        if (tabName.equals("experiment") )
            GWT.log(tabName + ": " + eventType);

        allEventTypeList.add(eventType);
        List<AjaxCallEventType> currentEventTypeList = eventMap.get(tabName);
        if (currentEventTypeList == null) {
            currentEventTypeList = new ArrayList<>(30);
            eventMap.put(tabName, currentEventTypeList);
        }
        if (eventType.isStart()) {
            currentEventTypeList.add(eventType);
        }
        if (eventType.isStop()) {
            if (!currentEventTypeList.contains(eventType.getStartMate()))
                GWT.log("Could not find start event: " + tabName + ": " + eventType.getStartMate());
            else {
                currentEventTypeList.remove(eventType.getStartMate());
            }
        }
        AppUtils.displayLoadingStatus(tabName, currentEventTypeList.size() > 0);
    }

}
