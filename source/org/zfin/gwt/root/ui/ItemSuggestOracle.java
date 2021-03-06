package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.SuggestOracle;

/**
 */
public class ItemSuggestOracle extends SuggestOracle {

    public final static int DEFAULT_LIMIT = 25 ;

    private CallbackTimer timer = null ;
    private static final int DEFAULT_DELAY_TIME = 200 ;
    private int delayTime = DEFAULT_DELAY_TIME ;
    private LookupComposite lookup ;
    public final static int NO_LIMIT = -1 ;
    private int limit = DEFAULT_LIMIT ;

    public ItemSuggestOracle(LookupComposite lookup){
        this.lookup = lookup ;
        timer = new CallbackTimer(lookup) ;
    }

    @Override
    public boolean isDisplayStringHTML() { return true; }

    @Override
    public void requestSuggestions(SuggestOracle.Request req, SuggestOracle.Callback callback) {
        String query = req.getQuery() ;
        if(limit!=DEFAULT_LIMIT){
            req.setLimit(limit);
        }
        if(query.length()>=lookup.getMinLookupLength()){
            timer.scheduleCallback(req,callback,delayTime);
            lookup.setNoteString("searching . . .");
        }
        else{
            StringBuilder noteString = new StringBuilder(query);
            while(noteString.length()<lookup.getMinLookupLength()){
               noteString.append('-');
            }
            lookup.setNoteString(noteString.toString());
        }
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
