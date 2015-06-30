package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.TermDTO;

public class AddNewDiseaseTermEvent extends GwtEvent<AddNewDiseaseTermHandler> {
    public static Type<AddNewDiseaseTermHandler> TYPE = new Type<>();

    private TermDTO disease;

    public AddNewDiseaseTermEvent(TermDTO disease) {
        this.disease = disease;
    }

    @Override
    public Type<AddNewDiseaseTermHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddNewDiseaseTermHandler handler) {
        handler.onAddDiseaseTerm(this);
    }

    public TermDTO getDisease() {
        return disease;
    }
}
