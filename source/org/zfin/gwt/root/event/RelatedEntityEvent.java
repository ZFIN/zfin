package org.zfin.gwt.root.event;

import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 */
public class RelatedEntityEvent<U extends RelatedEntityDTO> {


    private U dto;
    private String previousName ;

    public RelatedEntityEvent() { }

    public RelatedEntityEvent(U dto) {
        this.dto = dto;
    }

    public RelatedEntityEvent(U dto,String previousName) {
        this(dto);
        this.previousName = previousName;
    }

    public U getDTO() {
        return dto;
    }

    public String getPreviousName() {
        return previousName;
    }

    public boolean isNameChanged(){
        return false==previousName.equals(getDTO().getName()) ;
    }
}
