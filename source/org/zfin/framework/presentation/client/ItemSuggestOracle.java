package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class ItemSuggestOracle extends SuggestOracle {


    private CallbackTimer timer = null ; 
    private final int DEFAULT_DELAY_TIME = 200 ;
    private int delayTime = DEFAULT_DELAY_TIME ;
    private LookupComposite lookup ;
    public final static int NO_LIMIT = -1 ;
    private final static int DEFAULT_LIMIT = 25 ;
    private int limit = DEFAULT_LIMIT ;

    public ItemSuggestOracle(LookupComposite lookup){
        this.lookup = lookup ;
        timer = new CallbackTimer(lookup) ;
    }

    public boolean isDisplayStringHTML() { return true; }

    public void requestSuggestions(SuggestOracle.Request req, SuggestOracle.Callback callback) {
        String query = req.getQuery() ;
        if(limit!=NO_LIMIT){
            req.setLimit(limit);
        }
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

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
