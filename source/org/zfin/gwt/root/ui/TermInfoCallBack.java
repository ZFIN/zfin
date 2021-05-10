package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;

/**
 * Callback for term info box.
 */
public class TermInfoCallBack extends ZfinAsyncCallback<TermDTO> {

    protected String historyToken;
    protected TermInfoComposite termInfoComposite;

    public TermInfoCallBack(TermInfoComposite termInfoComposite, String historyToken) {
        super("Error during TermInfo call", null);
        this.termInfoComposite = termInfoComposite;
        this.historyToken = historyToken;
    }

    public TermInfoCallBack(TermInfoComposite termInfoComposite, String historyToken, ZfinModule module) {
        super("Error during TermInfo call", null,
                module, AjaxCallEventType.GET_TERM_INFO_STOP);
        this.termInfoComposite = termInfoComposite;
        this.historyToken = historyToken;
    }

    @Override
    public void onSuccess(TermDTO termInfoDTO) {
        super.onFinish();
        if (termInfoDTO == null) {
            return;
        }

        termInfoComposite.updateTermInfo(termInfoDTO, historyToken);
    }
}

