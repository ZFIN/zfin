package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class PublicationNotFoundException extends Exception implements IsSerializable{

    public PublicationNotFoundException() {
    }

    public PublicationNotFoundException(String zdbID) {
        super("Publication ID : " + zdbID + " not found");
    }
}
