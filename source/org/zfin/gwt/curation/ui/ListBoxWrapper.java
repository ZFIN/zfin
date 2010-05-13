package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;

import java.util.List;

/**
 */
public class ListBoxWrapper extends ListBox {

    public static final String NULL_STRING = "null";
    public static final String NULL_UPPER_STRING = "NULL";
    public static final String EMPTY_CHOICE = "---";

    public ListBoxWrapper(boolean multiselect) {
        super(multiselect);
    }

    public ListBoxWrapper() {
        super();
    }

    public String getSelectedStringValue() {
        String value;
        try {
            value = getValue(getSelectedIndex());
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
        }
    }

    public String getSelectedText() {
        String value;
        try {
            value = getItemText(getSelectedIndex());
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
        }
    }

    public Integer getSelectedIntegerValue() {
        String value = getValue(getSelectedIndex());
        if (value != null && false == value.equals(NULL_STRING) && value.length() > 0) {
            try {
                return Integer.valueOf(value);
            }
            catch (NumberFormatException nfe) {
//                Window.alert("problem formatting number: "+ value + "\n" + nfe.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isFieldEqual(Integer integer) {
        Integer selectedInteger = getSelectedIntegerValue();
        if (
                (integer == null && selectedInteger != null)
                        ||
                        (integer != null && selectedInteger == null)) {
            return false;
        } else if ((integer == selectedInteger) || integer.equals(selectedInteger)) {
            return true;
        } else if (integer.equals(selectedInteger)) {
            return true;
        }
        // values are not equal
        else {
            return false;
        }
    }

    public boolean isFieldEqual(String value) {
        String selectedString = getSelectedStringValue();
        if (
                (value == null && selectedString != null)
                        ||
                        (value != null && selectedString == null)) {
            return false;
        } else if ((value == selectedString) || value.equals(selectedString)) {
            return true;
        } else if (value.equals(selectedString)) {
            return true;
        }
        // values are not equal
        else {
            return false;
        }
    }

    public int setIndexForValue(String value) {
        int count = getItemCount();
        String itemText;
        for (int i = 0; i < count; i++) {
            itemText = getItemText(i);
            if (
                    (value != null && value.equals(itemText))
                            ||
                            (value == null && (getItemText(0).equals(EMPTY_CHOICE) || getItemText(0) == null))
                    ) {
                setItemSelected(i, true);
                return i;
            }
        }
        return -1;
    }

    public int setIndexForValue(Integer value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (
                    (value != null && value.toString().equals(getItemText(i)))
                            ||
                            (value == null && (getItemText(i) == null || getItemText(i).equals(EMPTY_CHOICE)))
                    ) {
                setItemSelected(i, true);
                return i;
            }
        }
        return -1;
    }

    public int addNullAndItems(List<String> items) {
        clear();
        // have to use the NULL_STRING here instead of null, or else it will use the item text instead
        addItem(EMPTY_CHOICE, NULL_STRING);
        for (String item : items) {
            if (item != null) {
                addItem(item, item);
            }
        }
        return items.size();
    }

    public int addItems(List<String> items) {
        clear();
        for (String item : items) {
            if (item != null) {
                addItem(item, item);
            }
        }
        return items.size();
    }


}
