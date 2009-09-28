package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.dto.CloneDTO;

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
