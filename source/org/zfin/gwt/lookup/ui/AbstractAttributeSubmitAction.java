package org.zfin.gwt.lookup.ui;

import org.zfin.gwt.root.ui.SubmitAction;

/**
 * Base class with attribution actions.
 */
public abstract class AbstractAttributeSubmitAction implements SubmitAction {

    protected String OID = null ;

    AbstractAttributeSubmitAction(String OID){
        this.OID = OID ;
    }

    protected native void setCookieInPage(String name,String value,String prefix) /*-{
        try{
            $wnd.setCookie(name,value,prefix) ;
        }
        catch(e){
            alert("problem setting cookie: "+e) ;
        }
    }-*/ ;
}
