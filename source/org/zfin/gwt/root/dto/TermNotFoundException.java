package org.zfin.gwt.root.dto;


/**
 */
public class TermNotFoundException extends Exception {

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
