package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.RelatedEntityDTO;


/**
 * Components that have related entities.
 */
public interface HasRelatedEntities extends CanRemoveReference {
    void removeRelatedEntity(RelatedEntityDTO relatedEntityDTO) ;
    void addAttribution(RelatedEntityDTO relatedEntityDTO) ;
    boolean isEditable(RelatedEntityDTO relatedEntityDTO) ;
}
