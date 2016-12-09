package org.zfin.gwt.curation.event;

import com.google.gwt.event.shared.GwtEvent;

public class AddExpressionExperimentEvent extends GwtEvent<AddExpressionExperimentEventHandler> {
    public static Type<AddExpressionExperimentEventHandler> TYPE = new Type<>();

    private int numberOfNewExpressions;

    public AddExpressionExperimentEvent(int numberOfNewExpressions) {
        this.numberOfNewExpressions = numberOfNewExpressions;
    }

    public int getNumberOfNewExpressions() {
        return numberOfNewExpressions;
    }

    @Override
    public Type<AddExpressionExperimentEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AddExpressionExperimentEventHandler handler) {
        handler.onEvent(this);
    }
}
