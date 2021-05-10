package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 */
public class RelatedEntityNotFoundException extends Exception implements IsSerializable {

    public RelatedEntityNotFoundException() {
    }

    public RelatedEntityNotFoundException(String message) {
        super(message);
    }
}
