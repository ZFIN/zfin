package org.zfin.gwt.root.ui;

/**
 */
public class StringTextBox extends AbstractTextBox<String> {

    @Override
    public String getBoxValue() {
        String text = super.getText();
        if (text.trim().length() == 0) {
            return null;
        } else {
            return text;
        }
    }

    public boolean isEmpty(){
        return getBoxValue()==null ;
    }


    @Override
    public boolean isFieldEqual(String string) {
        String text = getText();
        if ( string == null && (text != null && text.length()>0) ){
            return false ;
        }
        else
        if( text==null && (string != null && string.length()>0)) {
            return false ;
        }
        else if(string==null && text==null){
            return true ;
        } else if (string == null && text!=null && text.trim().length()==0) {
            return true;
        } else if (text == null && string!=null && string.trim().length()==0) {
            return true;
        } else {
            return string.equals(text) ;
        }
    }

    public void clear() {
        setText("");
    }
}