package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.Window;

import java.util.Map;
import java.util.HashMap;

/**
 * This class has a collection of submissions and types.
 */
public class SubmissionHandler {


    /**
     * Map<String,SubmitAction>
     */
    public Map actionMap = new HashMap() ;

    public void addAction(String type,SubmitAction action){
        actionMap.put(type,action) ;
    }

    /**
     * Submits if action is defined, otherwise nothing.
     * @param type
     * @param value
     */
    public void doSubmit(String type,String value) {
        if(actionMap.containsKey(type)){
            ((SubmitAction) actionMap.get(type)).doSubmit(value);
        }
        //
//        else{
//            Window.alert("type not found: "+type);
//        }
    }
}
