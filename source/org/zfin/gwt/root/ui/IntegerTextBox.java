package org.zfin.gwt.root.ui;

/**
 */
public class IntegerTextBox extends AbstractTextBox<Integer> {

    @Override
    public Integer getBoxValue() throws NumberFormatException {
        String text = super.getText();
        if (text == null || text.trim().length() == 0) {
            return null;
        } else {
            return Integer.valueOf(text);
        }
    }

    @Override
    public boolean isFieldEqual(Integer integer) {
        Integer thisInteger = getBoxValue();
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