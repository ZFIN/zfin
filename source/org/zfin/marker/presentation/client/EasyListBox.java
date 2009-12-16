package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.ListBox;

import java.util.List;

/**
 */
public class EasyListBox extends ListBox {

    public static final String NULL_STRING = "null";
    public static final String NULL_UPPER_STRING = "NULL";
    public static final String EMPTY_CHOICE = "---";
    public static final String NONE = "none";

    public EasyListBox(boolean multiselect) {
        super(multiselect);
    }

    public EasyListBox() {
        super();
    }

    public String getSelectedString() {
        String value = getValue(getSelectedIndex());
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
        }
    }

    public Integer getSelectedInteger() {
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
        Integer selectedInteger = getSelectedInteger();
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
        String selectedString = getSelectedString();
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

    public void setIndexForValue(String value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            String itemText = getValue(i);
            if (
                    (value != null && value.equals(getItemText(i)))
                            ||
                            (value == null && (getItemText(i).equals(EMPTY_CHOICE) || getItemText(i).equals(NONE) || getItemText(i) == null))
                    ) {
                setItemSelected(i, true);
                return;
            }
        }
    }

    public void setIndexForValue(Integer value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (
                    (value != null && value.toString().equals(getItemText(i)))
                            ||
                            (value == null && (getItemText(i) == null || getItemText(i).equals(EMPTY_CHOICE)))
                    ) {
                setItemSelected(i, true);
                return;
            }
        }
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
