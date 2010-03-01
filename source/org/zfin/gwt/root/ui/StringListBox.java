package org.zfin.gwt.root.ui;

/**
 */
public class StringListBox extends AbstractListBox<String>{

    public StringListBox(boolean multiselect) {
        super(multiselect);
    }

    public StringListBox() {
        super();
    }

    public boolean isSelectedNull(){
        return getSelected()==null ; 
    }

    public String getSelected() {
        String value = getValue(getSelectedIndex());
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
        }
    }



    public boolean isFieldEqual(String value) {
        String selectedString = getSelected();
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
        else if(value==null && selectedString==null) {
            return true ;
        }
        // values are not equal
        else {
            return false;
        }
    }

    public void setIndexForValue(String value) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
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

    public boolean isDirty(String value) {
        if(isFieldEqual(value)){
            setStyleName(CLEAN_STYLE);
            return false ;
        }
        else{
            setStyleName(DIRTY_STYLE);
            return true ;
        }
    }
}