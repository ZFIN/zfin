package org.zfin.marker.presentation.client;

import org.zfin.marker.presentation.dto.RelatedEntityDTO;


public interface HasRelatedEntities extends CanRemoveReference {
    void removeRelatedEntity(RelatedEntityDTO relatedEntityDTO) ;
    void addAttribution(RelatedEntityDTO relatedEntityDTO) ;
    boolean isEditable(RelatedEntityDTO relatedEntityDTO) ;
}
