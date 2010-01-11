package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.CloneDTO;

/**
 */
public class CloneDataChangedEvent {
    private CloneDTO cloneDTO;

    public CloneDataChangedEvent(CloneDTO cloneDTO){
        this.cloneDTO = cloneDTO;
    }

    public CloneDTO getCloneDTO() {
        return cloneDTO;
    }
}
