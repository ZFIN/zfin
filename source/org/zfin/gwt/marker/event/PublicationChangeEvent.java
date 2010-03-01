package org.zfin.gwt.marker.event;

import org.zfin.gwt.marker.ui.PublicationValidator;

/**
 */
public class PublicationChangeEvent {

    private String publication ;

    public PublicationChangeEvent(String publication){
        this.publication = publication ;
    }

    public String getPublication() {
        return publication;
    }

    public boolean isNotEmpty() {
        return new PublicationValidator().validate(publication,null) ; 
    }
}
