package org.zfin.gwt.marker.ui;

import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 */
public interface CanRemoveReference<U extends RelatedEntityDTO> {
    void removeAttribution(U relatedEntityDTO) ;
    String getZdbID() ;
}
