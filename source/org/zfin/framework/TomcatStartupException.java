package org.zfin.framework;

/**
 */
public class TomcatStartupException extends Exception{
    public TomcatStartupException(String message){
        super(message) ;
    }

    public TomcatStartupException(String message, Exception e) {
        super(message,e) ;
    }
}
