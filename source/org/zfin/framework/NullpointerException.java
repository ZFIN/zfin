package org.zfin.framework;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Exception for structure duplicates.
 */
public class NullpointerException extends RuntimeException implements IsSerializable{

    public NullpointerException() {
    }

    public NullpointerException(String message) {
        super(message);
    }

}