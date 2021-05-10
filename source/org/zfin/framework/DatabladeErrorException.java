package org.zfin.framework;


public class DatabladeErrorException extends RuntimeException  {

    public DatabladeErrorException() {

    }

    public DatabladeErrorException(String message) {
        super(message);
    }
}
