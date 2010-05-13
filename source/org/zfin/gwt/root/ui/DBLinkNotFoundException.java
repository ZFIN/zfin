package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Thrown when DBLink is not found.
 */
public class DBLinkNotFoundException extends Exception implements IsSerializable {

    public DBLinkNotFoundException() {
        super();
    }

    public DBLinkNotFoundException(String message) {
        super(message);
    }


}
