package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.MarkerDTO;

/**
 * Event gets fired when a name is changed.
 */
public class MarkerChangeEvent<T extends MarkerDTO> {

    public static final String ADD_PUBLICATION = "ADD_PUBLICATION";
    public static final String REMOVE_PUBLICATION = "REMOVE_PUBLICATION";

    private String previousName ;

    private T dto ;

    public T getDTO() {
        return dto ;
    }

    public MarkerChangeEvent(T dto,String previousName) {
        this.dto = dto ;
        this.previousName= previousName ;
    }


    public MarkerChangeEvent(String previousName) {
        this.previousName= previousName ;
    }

    public String getPreviousName() {
        return previousName;
    }

    public boolean isNameChanged(){
        return false==previousName.equals(getDTO().getName()) ;
    }
}