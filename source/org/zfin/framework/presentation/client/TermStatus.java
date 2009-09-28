package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class TermStatus implements IsSerializable {

    public enum Status {
        FOUND_NONE,
        FOUND_MANY,
        FOUND_EXACT,
        FAILURE,
        LOOKING,
        INIT,
    }

    private Status status = Status.INIT ;
    private String term = "" ;
    private String zdbID = "" ;

    public TermStatus() { }

    public TermStatus(Status newStatus){
        this.status = newStatus;
    }

    public TermStatus(Status newStatus,String term){
        this(newStatus)  ;
        this.term = term ;
    }

    public TermStatus(Status newStatus,String term,String zdbID){
        this(newStatus)  ;
        this.term = term ;
        this.zdbID = zdbID;
    }


    public void reset(){
        status = Status.INIT;
    }

    public boolean isExactMatch(){
        return status.equals(Status.FOUND_EXACT) ;
    }

    public boolean isNotFound(){
        return status.equals(Status.FOUND_NONE) ;
    }

    public boolean isFoundMany(){
        return status.equals(Status.FOUND_MANY) ;
    }

    public boolean isLooking(){
        return status.equals(Status.LOOKING) ;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toString(){
        return status.toString() ;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
