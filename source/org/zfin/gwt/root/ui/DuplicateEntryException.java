package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class DuplicateEntryException extends Exception implements IsSerializable {

    public DuplicateEntryException(){}

    public DuplicateEntryException(String message) {
        super(message) ;
    }
}
