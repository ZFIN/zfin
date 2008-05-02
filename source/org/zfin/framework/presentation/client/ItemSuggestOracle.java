package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class ItemSuggestOracle extends SuggestOracle {


    private CallbackTimer timer = null ; 
    private final int DEFAULT_DELAY_TIME = 200 ;
    private int delayTime = DEFAULT_DELAY_TIME ;
    private LookupComposite lookup ;


    public ItemSuggestOracle(LookupComposite lookup){
        this.lookup = lookup ;
        timer = new CallbackTimer(lookup) ;
    }

    public boolean isDisplayStringHTML() { return true; }

    public void requestSuggestions(SuggestOracle.Request req, SuggestOracle.Callback callback) {
        String query = req.getQuery() ;
        if(query.length()>=lookup.getMinLookupLenth()){
            timer.scheduleCallback(req,callback,delayTime);
            lookup.setNoteString("searching . . .");
        }
        else{
            StringBuffer noteString = new StringBuffer(query) ;
            while(noteString.length()<lookup.getMinLookupLenth()){
               noteString.append('-');
            }
            lookup.setNoteString(noteString.toString());
        }
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

}
