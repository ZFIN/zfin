package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.CloneDTO;

public class CloneChangeEvent extends MarkerChangeEvent<CloneDTO>{

    public CloneChangeEvent(CloneDTO cloneDTO,String previousName){
        super(cloneDTO,previousName);
    }
}
