package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.TermDTO;

public class ClickTermEvent extends GwtEvent<ClickTermEventHandler> {
    public static Type<ClickTermEventHandler> TYPE = new Type<>();

    private TermDTO termDTO;


    public ClickTermEvent(TermDTO termDTO) {
        this.termDTO = termDTO;
    }

    @Override
    public Type<ClickTermEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ClickTermEventHandler handler) {
        handler.onClickOnTerm(this);
    }

    public TermDTO getTermDTO() {
        return termDTO;
    }
}
