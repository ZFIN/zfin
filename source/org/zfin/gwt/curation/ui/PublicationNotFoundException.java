package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PublicationNotFoundException extends RuntimeException implements IsSerializable{

    public PublicationNotFoundException() {
    }

    public PublicationNotFoundException(String zdbID) {
        super("Publication ID : " + zdbID + " not found");
    }
}
