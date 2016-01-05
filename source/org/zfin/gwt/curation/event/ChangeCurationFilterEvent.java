package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;
import org.zfin.gwt.root.dto.ExperimentDTO;

public class ChangeCurationFilterEvent extends GwtEvent<ChangeCurationFilterEventHandler> {
    public static Type<ChangeCurationFilterEventHandler> TYPE = new Type<>();

    private ExperimentDTO experimentFilter;
    private String figureID;


    public ChangeCurationFilterEvent(ExperimentDTO experimentFilter, String figureID) {
        this.experimentFilter = experimentFilter;
        this.figureID = figureID;
    }

    @Override
    public Type<ChangeCurationFilterEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ChangeCurationFilterEventHandler handler) {
        handler.onChange(this);
    }

    public ExperimentDTO getExperimentFilter() {
        return experimentFilter;
    }

    public String getFigureID() {
        return figureID;
    }
}
