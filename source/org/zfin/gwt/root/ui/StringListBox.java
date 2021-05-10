package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.util.StringUtils;

/**
 */
public class StringListBox extends AbstractListBox<String> {

    public StringListBox(boolean multiselect) {
        super(multiselect);
    }

    public StringListBox() {
        super();
    }

    public boolean isSelectedNull() {
        return getSelected() == null;
    }

    public String getSelected() {
        if (getSelectedIndex() < 0) {
            return null;
        }
        String value = getValue(getSelectedIndex());
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
        }
    }


    public boolean isFieldEqual(String value) {
        if (value == null) {
            return getSelectedIndex() == 0;
        }
        String selectedString = getSelected();
        if (StringUtils.isEmpty(value) && StringUtils.isEmpty(selectedString)) {
            return true;
        }
        if ((StringUtils.isNotEmpty(value) && StringUtils.isEmpty(selectedString)) ||
                (StringUtils.isEmpty(value) && StringUtils.isNotEmpty(selectedString))) {
            return false;
        }
        return value.equals(selectedString);
    }

    public int setIndexForText(String value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (
                    (value != null && value.equals(getItemText(i)))
                            ||
                            (value == null && (getItemText(i).equals(EMPTY_CHOICE) || getItemText(i).equals(NONE) || getItemText(i) == null))
                    ) {
                setItemSelected(i, true);
                return i;
            }
        }
        return -1;
    }

    @Override
    public int setIndexForValue(String value) {
        if (value == null) {
            setSelectedIndex(0);
            return 0;
        }
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (
                    (value != null && value.equals(getValue(i)))
                            ||
                            (value == null && (getValue(i).equals(EMPTY_CHOICE) || getValue(i).equals(NONE) || getValue(i) == null))
                    ) {
                setItemSelected(i, true);
                return i;
            }
        }
        return -1;
    }

    public boolean isDirty(String value) {
        if (getItemCount() == 0 || isFieldEqual(value)) {
            return setDirty(false);
        } else {
            return setDirty(true);
        }
    }
}
