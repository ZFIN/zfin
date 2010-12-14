package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 * Can remove a reference.
 */
public interface CanRemoveReference<U extends RelatedEntityDTO> {
    void removeAttribution(U relatedEntityDTO) ;
    String getZdbID() ;
}
