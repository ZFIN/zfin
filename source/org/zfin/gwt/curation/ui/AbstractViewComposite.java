package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.ShowHideToggle;
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

    public abstract Set<IsDirtyWidget> getValueFields();

    public boolean hasEnteredValues() {
        for (IsWidget widget : getValueFields()) {
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

    protected boolean validateStartEnd(NumberTextBox start, NumberTextBox end) {
        if (start != null && end != null && start.getBoxValue() != null && end.getBoxValue() != null) {
            if (start.getBoxValue() > end.getBoxValue())
                errorLabel.setError("Start Number is greater than end number");
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
