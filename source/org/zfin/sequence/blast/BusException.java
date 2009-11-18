package org.zfin.sequence.blast;

/**
 */
public class BusException extends Exception{

    private String returnXML ;

    public BusException(String message, String xml) {
        super(message) ;
        this.returnXML = xml ;
    }

    public String getReturnXML() {
        return returnXML;
    }
}
