package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.Window;

/**
 */
public class LookupInTableComposite extends LookupComposite{

    LookupTable parentTable ;

    public LookupInTableComposite(LookupTable parentTable){
        super() ;
        this.parentTable = parentTable ;
    }

    protected void doSubmit(String text) {
        parentTable.addTermToTable(text);
    }
}
