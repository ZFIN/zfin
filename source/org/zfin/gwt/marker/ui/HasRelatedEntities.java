package org.zfin.gwt.marker.ui;

import org.zfin.gwt.root.dto.RelatedEntityDTO;


public interface HasRelatedEntities extends CanRemoveReference {
    void removeRelatedEntity(RelatedEntityDTO relatedEntityDTO) ;
    void addAttribution(RelatedEntityDTO relatedEntityDTO) ;
    boolean isEditable(RelatedEntityDTO relatedEntityDTO) ;
}
