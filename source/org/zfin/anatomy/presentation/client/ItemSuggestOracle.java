package org.zfin.anatomy.presentation.client;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class ItemSuggestOracle extends SuggestOracle {
    private CallbackTimer timer = new CallbackTimer() ;
    private final int DEFAULT_DELAY_TIME = 200 ;
    private int delayTime = DEFAULT_DELAY_TIME ;

    public boolean isDisplayStringHTML() { return true; }

    public void requestSuggestions(SuggestOracle.Request req, SuggestOracle.Callback callback) {
        timer.scheduleCallback(req,callback,delayTime);
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }
}
