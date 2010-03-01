package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.TextArea;


/**
 */
public class RevertibleTextArea<T extends String> extends TextArea implements IsDirty<T> {

    public RevertibleTextArea() {
        super();
    }

    public boolean isSelectedNull(){
        return getSelected()==null ;
    }

    public String getSelected() {
        String value = getValue();
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
        }
    }



    public boolean isFieldEqual(String value) {
        String selectedString = getSelected();
        if ( (value == null && selectedString != null)){
            return selectedString.trim().length()==0;
        }
        else
        if (value != null && selectedString == null) {
            return value.trim().length()==0;
        }
        else
        if ((value == selectedString) || value.equals(selectedString)) {
            return true;
        }
        else
        if (value.equals(selectedString)) {
            return true;
        }
        else
        if(value==null && selectedString==null) {
            return true ;
        }
        // values are not equal
        else {
            return false;
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