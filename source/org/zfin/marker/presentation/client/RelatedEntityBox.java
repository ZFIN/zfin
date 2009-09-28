package org.zfin.marker.presentation.client;

import org.zfin.marker.presentation.dto.RelatedEntityDTO;
import org.zfin.marker.presentation.event.RelatedEntityEvent;

/**
 */
public class RelatedEntityBox extends AbstractRelatedEntityBox<RelatedEntityDTO> {


    public void addRelatedEntity(final String name,final String pubZdbID)  {
        // do client check
        String validationError = validateNewRelatedEntity(name);
        if(validationError!=null){
            setError(validationError);
            return ;
        }
        RelatedEntityDTO dto = new RelatedEntityDTO(getZdbID(), name,pubZdbID);
        fireRelatedEntityAdded(new RelatedEntityEvent<RelatedEntityDTO>(dto));
    }

    public void removeAttribution(RelatedEntityDTO relatedEntityDTO) {
        fireAttributionRemoved(new RelatedEntityEvent<RelatedEntityDTO>(relatedEntityDTO));
    }

    public boolean isEditable(RelatedEntityDTO relatedEntityDTO) {
        return true ; 
    }
}