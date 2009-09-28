package org.zfin.marker.presentation.event;

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

}
