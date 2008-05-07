package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class TermStatus implements IsSerializable {
    public final static String TERM_STATUS_FOUND_NONE = "TERM_STATUS_FOUND_NONE";
    public final static String TERM_STATUS_FOUND_MANY = "TERM_STATUS_FOUND_MANY";
    public final static String TERM_STATUS_FOUND_EXACT= "TERM_STATUS_FOUND_EXACT";
    public final static String TERM_STATUS_FAILURE= "TERM_STATUS_FAILURE";
    public final static String TERM_STATUS_LOOKING = "TERM_STATUS_LOOKING";

    private String status = TERM_STATUS_LOOKING ;
    private String term = "" ;
    private String oboID = "" ;

    public TermStatus() { }

    public TermStatus(String status){
        if(status.equals(TERM_STATUS_FOUND_MANY)
                ||
                status.equals(TERM_STATUS_FOUND_NONE)
                ||
                status.equals(TERM_STATUS_FOUND_EXACT)
                ||
                status.equals(TERM_STATUS_LOOKING)
                ||
                status.equals(TERM_STATUS_FAILURE)
                ){
            this.status = status ;
        }
        else{
            throw new RuntimeException("Must initialize term status with TERM_STATUS variable not: "+status);
        }
    }

    public TermStatus(String status,String term){
        this(status)  ;
        this.term = term ;
    }

    public TermStatus(String status,String term,String oboID){
        this(status)  ;
        this.term = term ;
        this.oboID = oboID;
    }


    public void reset(){
        status = TERM_STATUS_LOOKING ;
    }

    public boolean isExactMatch(){
        return status.equals(TERM_STATUS_FOUND_EXACT) ;
    }

    public boolean isNotFound(){
        return status.equals(TERM_STATUS_FOUND_NONE) ;
    }

    public boolean isFoundMany(){
        return status.equals(TERM_STATUS_FOUND_MANY) ;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString(){
        return getStatus() ;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }
}
