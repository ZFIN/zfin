package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class ItemSuggestOracle extends SuggestOracle {


    private CallbackTimer timer = null ; 
    private final int DEFAULT_DELAY_TIME = 200 ;
//    private final int DEFAULT_DELAY_TIME = 100 ;
    private int delayTime = DEFAULT_DELAY_TIME ;
    private LookupComposite lookup ;


    public ItemSuggestOracle(LookupComposite lookup){
        this.lookup = lookup ;
        timer = new CallbackTimer(lookup) ;
    }

    public boolean isDisplayStringHTML() { return true; }

    public void requestSuggestions(SuggestOracle.Request req, SuggestOracle.Callback callback) {
        timer.scheduleCallback(req,callback,delayTime);
        lookup.setErrorString("working . . .");
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }
}
