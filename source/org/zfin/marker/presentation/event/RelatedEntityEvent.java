package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.dto.RelatedEntityDTO;

/**
 */
public class RelatedEntityEvent<U extends RelatedEntityDTO> {


    private U relatedEntityDTO;

    public RelatedEntityEvent(U relatedEntityDTO) {
        this.relatedEntityDTO = relatedEntityDTO;
    }

    public U getRelatedEntityDTO() {
        return relatedEntityDTO;
    }

    public void setRelatedEntityDTO(U relatedEntityDTO) {
        this.relatedEntityDTO = relatedEntityDTO;
    }
}
