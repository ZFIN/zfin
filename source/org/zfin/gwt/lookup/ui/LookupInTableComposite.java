package org.zfin.gwt.lookup.ui;

import org.zfin.gwt.root.ui.LookupComposite;

/**
 */
public class LookupInTableComposite extends LookupComposite {

    LookupTable parentTable;

    public LookupInTableComposite(LookupTable parentTable) {
        super();
        this.parentTable = parentTable;
    }

    protected void doSubmit(String text) {
        parentTable.validateLookup();
    }
}
