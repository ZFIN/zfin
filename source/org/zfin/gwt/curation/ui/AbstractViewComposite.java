package org.zfin.gwt.curation.ui;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.ui.NumberTextBox;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.util.StringUtils;

import java.util.Set;

public abstract class AbstractViewComposite extends Composite {

    public static final String NOT_A_NUMBER = "Not a number";

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

    protected void validateNumber(NumberTextBox numberField) {
        String boxValue = numberField.getBoxValue();
        if (boxValue != null && !boxValue.trim().isEmpty()) {
            if (!StringUtils.isNumeric(boxValue.trim())) {
                errorLabel.setError(NOT_A_NUMBER);
                return;
            }
        }
        clearError();
    }


}
