package org.zfin.gwt.root.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 */
public class ZfinAccessionBox extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ZfinAccessionBox.ui.xml")
    interface MyUiBinder extends UiBinder<HorizontalPanel, ZfinAccessionBox> {
    }

    protected String type;
    protected ZfinAccessionBoxPresenter presenter;

    @UiField
    StringTextBox accessionNumber;
    @UiField
    HTML validSequenceCharacter;
    @UiField
    HTML faultySequenceCharacter;

    @UiConstructor
    public ZfinAccessionBox(String type) {
        initWidget(uiBinder.createAndBindUi(this));
        this.type = type;
        presenter = new ZfinAccessionBoxPresenter(this);
    }

    @UiHandler("accessionNumber")
    void onBlurSequence(@SuppressWarnings("unused") BlurEvent event) {
        if (!accessionNumber.isEmpty())
            presenter.checkValidAccession(accessionNumber.getBoxValue(), type);
        else {
            setFlagVisibility(false);
        }
        presenter.runPostOnSequenceBlurEvent();
    }

    public void setError(String message) {

    }

    public void clear() {
        accessionNumber.clear();
    }

    public void setFlagVisibility(boolean visible) {
        validSequenceCharacter.setVisible(visible);
        faultySequenceCharacter.setVisible(visible);

    }

    public boolean isValid() {
        return validSequenceCharacter.isVisible();
    }

    public StringTextBox getAccessionNumber() {
        return accessionNumber;
    }

    public String getAccession(){
        return accessionNumber.getText();
    }

    public ZfinAccessionBoxPresenter getPresenter() {
        return presenter;
    }
}