package org.zfin.gwt.root.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.OntologyDTO;

public class SelectAutoCompleteEvent extends GwtEvent<SelectAutoCompleteEventHandler> {
    public static Type<SelectAutoCompleteEventHandler> TYPE = new Type<>();

    private String termID, termName;
    private OntologyDTO ontology;

    public SelectAutoCompleteEvent(String termID, String termName, OntologyDTO ontology) {
        this.termID = termID;
        this.termName = termName;
        this.ontology = ontology;
    }

    @Override
    public Type<SelectAutoCompleteEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SelectAutoCompleteEventHandler handler) {
        handler.onSelect(this);
    }

    public String getTermID() {
        return termID;
    }

    public String getTermName() {
        return termName;
    }

    public OntologyDTO getOntology() {
        return ontology;
    }
}
