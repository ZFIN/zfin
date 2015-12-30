package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.EntityPart;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.ExpressionPileStructureDTO;

import java.util.List;

public class ExpressionEvent extends GwtEvent<ExpressionEventHandler> {
    public static Type<ExpressionEventHandler> TYPE = new Type<>();

    private List<ExpressionPileStructureDTO> structureDTOList;
    private ExpressedTermDTO expressedTerm;
    private EntityPart pileEntity;

    public void setStructureDTOList(List<ExpressionPileStructureDTO> structureDTOList) {
        this.structureDTOList = structureDTOList;
    }

    @Override
    public Type<ExpressionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ExpressionEventHandler handler) {
        handler.onAddStructures(this);
    }

    public List<ExpressionPileStructureDTO> getStructureList() {
        return structureDTOList;
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
