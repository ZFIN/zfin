package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.TextBox;

/**
 */
public class TextBoxWrapper extends TextBox {

    public String getText() {
        String text = super.getText();
        if (text.trim().length() == 0) {
            return null;
        } else {
            return text;
        }
    }

    public Integer getInteger() throws NumberFormatException {
        String text = super.getText();
        if (text == null || text.trim().length() == 0) {
            return null;
        } else {
            return Integer.valueOf(text);
        }
    }

    public boolean isFieldEqual(String string) {
        String text = getText();
        if (
                (string == null && text != null)
                        ||
                        (string != null && text == null)) {
            return false;
        } else if ((string == text) || string.equals(text)) {
            return true;
        } else if (string.equals(text)) {
            return true;
        }
        // values are not equal
        else {
            return false;
        }
    }

    public boolean isFieldEqual(Integer integer) {
        Integer thisInteger = getInteger();
        if (
                (integer == null && thisInteger != null)
                        ||
                        (integer != null && thisInteger == null)) {
            return false;
        } else if (
                (integer == null && thisInteger == null)
                        ||
                        (integer.equals(thisInteger))
                ) {
            return true;
        } else {
            return false;
        }
    }
}
