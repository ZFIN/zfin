package org.zfin.marker.presentation.client;

import org.zfin.marker.presentation.dto.RelatedEntityDTO;

/**
 */
public interface CanRemoveReference<U extends RelatedEntityDTO> {
    void removeAttribution(U relatedEntityDTO) ;
    String getZdbID() ;
}
