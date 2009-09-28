package org.zfin.framework.presentation.client;


import com.google.gwt.user.client.rpc.SerializableException;

/**
 */
public class TermNotFoundException extends SerializableException {

    private String term ;
    private String type ;

    public TermNotFoundException(){

    }

    public TermNotFoundException(String term,String type){
        super() ;
        this.term = term ;
        this.type = type ;
    }

    public String getTerm() {
        return term;
    }

    public String getType() {
        return type;
    }
}
