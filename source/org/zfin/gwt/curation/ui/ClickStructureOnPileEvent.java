package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.ExpressedTermDTO;

public class ClickStructureOnPileEvent extends GwtEvent<ClickStructureOnPileHandler> {
    public static Type<ClickStructureOnPileHandler> TYPE = new Type<>();

    private ExpressedTermDTO expressedTerm;
    private EntityPart pileEntity;

    @Override
    public Type<ClickStructureOnPileHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ClickStructureOnPileHandler handler) {
        handler.onEvent(this);
    }

    public void setTermAndEntity(ExpressedTermDTO expressedTerm, EntityPart pileEntity) {
        this.expressedTerm = expressedTerm;
        this.pileEntity = pileEntity;
    }

    public ExpressedTermDTO getExpressedTerm() {
        return expressedTerm;
    }

    public EntityPart getPileEntity() {
        return pileEntity;
    }
}
