package org.zfin.gwt.curation.ui;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.SimpleErrorElement;

import java.util.Set;

public abstract class AbstractViewComposite extends Composite {

    public static final String NOT_A_NUMBER = "Not a number";

    protected MutationDetailPresenter presenter;

    @UiField
    SimpleErrorElement errorLabel;

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public abstract Set<Widget> getValueFields();

    public boolean hasEnteredValues() {
        for (Widget widget : getValueFields()) {
            if (widget instanceof ListBox) {
                if (((ListBox) widget).getSelectedIndex() > 0)
                    return true;
            }
            if (widget instanceof TextBox) {
                if (!((TextBox) widget).getText().trim().isEmpty())
                    return true;
            }
        }
        return false;
    }

    protected boolean validateNumber(NumberTextBox numberField) {
        if (!numberField.isValid()) {
            errorLabel.setError(NOT_A_NUMBER);
            return false;
        }
        clearError();
        return true;
    }

    protected void handleChanges() {
        presenter.handleDirty();
    }

    public void setPresenter(MutationDetailPresenter presenter) {
        this.presenter = presenter;
    }

}
