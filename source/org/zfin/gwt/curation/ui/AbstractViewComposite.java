package org.zfin.gwt.curation.ui;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import org.zfin.gwt.root.ui.SimpleErrorElement;

public class AbstractViewComposite extends Composite {

    @UiField
    SimpleErrorElement errorLabel;

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }
}
