package org.zfin.datatransfer;

/**
 * Error that we throw when we can not connect to a service.
 */
public class ServiceConnectionException extends Exception{

    public ServiceConnectionException(String message){
        super(message);
    }
}
