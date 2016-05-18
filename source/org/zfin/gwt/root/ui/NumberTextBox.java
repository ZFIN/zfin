package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.util.StringUtils;

public class NumberTextBox extends AbstractTextBox<Integer> {

    @Override
    public Integer getBoxValue() {
        String text = super.getText();
        if (text.trim().length() == 0) {
            return null;
        } else {
            if (StringUtils.isNumeric(text))
                return Integer.parseInt(text);
            else
                return null;
        }
    }

    public void setNumber(Integer number) {
        if (number == null)
            setText("");
        else
            setText(number.toString());
    }

    public boolean isValid() {
        String text = super.getText();
        if (text == null)
            return true;
        if (text.trim().length() == 0)
            return true;
        return StringUtils.isNumeric(text);
    }

    @Override
    protected boolean isFieldEqual(Integer number) {

        String text = getText();
        if (number == null && (text == null || text.trim().length() == 0 || text.trim().equals("0")))
            return true;
        if (number == null)
            return false;
        if (text == null) {
            return false;
        }
        if (!StringUtils.isNumeric(text))
            return false;
        if (text.trim().length() == 0) {
            return number.equals(0);
        }
        Integer textValue = Integer.parseInt(text);
        return textValue.equals(number);
    }

    public void clear() {
        setText("");
    }

    public boolean isEmpty() {
        return getBoxValue() == null;
    }


}
