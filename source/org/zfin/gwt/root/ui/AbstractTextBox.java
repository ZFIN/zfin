package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.TextBox;

/**
 * A generic textbox that can be "dirty".
 */
public abstract class AbstractTextBox<T> extends TextBox implements IsDirty<T>{

    public abstract T getBoxValue() ;

    protected abstract boolean isFieldEqual(T value) ;


    @Override
    public boolean isDirty(T value) {
        if(isFieldEqual(value)){
            setStyleName(CLEAN_STYLE);
            return false ;
        }
        else{
            setStyleName(DIRTY_STYLE);
            return true ;
        }
    }

    public boolean setDirty(boolean dirty){
        if(dirty){
            setStyleName(DIRTY_STYLE);
        }
        else{
            setStyleName(CLEAN_STYLE);
        }
        return dirty ;
    }
}
