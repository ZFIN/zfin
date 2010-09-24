package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.TermInfo;

/**
 * Callback for term info box.
 */
public class TermInfoCallBack extends ZfinAsyncCallback<TermInfo> {

    protected String historyToken;
    protected TermInfoComposite termInfoComposite;

    public TermInfoCallBack(TermInfoComposite termInfoComposite, String historyToken) {
        super("Error during TermInfo call", null);
        this.termInfoComposite = termInfoComposite;
        this.historyToken = historyToken;
    }

    @Override
    public void onSuccess(TermInfo termInfo) {
        if (termInfo == null) {
            return;
        }

        termInfoComposite.updateTermInfo(termInfo, historyToken);
    }
}

