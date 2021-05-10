package org.zfin.gwt.root.ui;

/**
 */
public class IntegerListBox extends AbstractListBox<Integer> {

    public IntegerListBox(boolean multiselect) {
        super(multiselect);
    }

    public IntegerListBox() {
        super();
    }

    public Integer getSelected() {
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
        Integer selectedInteger = getSelected();
        if (
                (integer == null && selectedInteger != null)
                        ||
                        (integer != null && selectedInteger == null)) {
            return false;
        } else
        if ((integer == selectedInteger) || integer.equals(selectedInteger)) {
            return true;
        }
        else
        if (integer.equals(selectedInteger)) {
            return true;
        }
        // values are not equal
        else {
            return false;
        }
    }

    public int setIndexForText(Integer value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (
                    (value != null && value.toString().equals(getItemText(i)))
                            ||
                            (value == null && (getItemText(i) == null || getItemText(i).equals(EMPTY_CHOICE)))
                    ) {
                setItemSelected(i, true);
                return 1;
            }
        }
        return -1 ;
    }

    @Override
    public int setIndexForValue(Integer value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (
                    (value != null && value.equals(Integer.valueOf(getValue(i))))
                            ||
                            (value == null && (getValue(i).equals(EMPTY_CHOICE) || getValue(i).equals(NONE) || getValue(i) == null))
                    ) {
                setItemSelected(i, true);
                return i ;
            }
        }
        return -1 ;
    }

    public boolean isDirty(Integer value) {
        if( isFieldEqual( value)){
            return setDirty(false) ;
        }
        else{
            return setDirty(true) ;
        }
    }
}