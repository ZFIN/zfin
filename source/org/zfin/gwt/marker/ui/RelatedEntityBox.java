package org.zfin.gwt.marker.ui;

import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

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