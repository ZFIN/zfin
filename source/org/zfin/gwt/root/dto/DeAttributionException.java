package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 */
public class DeAttributionException extends Exception implements IsSerializable {

    public DeAttributionException() {
    }

    public DeAttributionException(String message) {
        super(message);
    }

}
