package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.ListBox;

import java.util.List;

/**
 * A generic listbox that is easier to work with and handles isDirty.
 */
public abstract class AbstractListBox<T extends Object> extends ListBox implements IsDirty<T>{

    public static final String EMPTY_CHOICE = "---";
    public static final String NONE = "none";

    AbstractListBox(boolean multiselect) {
        super(multiselect);
    }

    AbstractListBox() {
        super();
    }

    public abstract T getSelected() ;

    public abstract boolean isFieldEqual(T value) ;

    public abstract void setIndexForText(T value) ;

    public abstract void setIndexForValue(T value) ;

    public String getSelectedText() {
        String value = getItemText(getSelectedIndex());
        if (value.equals(NULL_STRING)) {
            return null;
        } else {
            return value;
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


    public boolean containsValue(String value) {
        int size = getItemCount();
        for(int i = 0 ; i < size; i++){
            if(getValue(i).equals(value)){
                return true ;
            }
        }

        return false;
    }

}
