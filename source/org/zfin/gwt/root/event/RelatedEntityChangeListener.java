package org.zfin.gwt.root.event;

import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 * This is related to the RelatedEntityListener.
 * It made sense to separate the data changed portion from
 * the attribution / add / remove, as they seem to be
 * handled in different portions.
 *
 */
public interface RelatedEntityChangeListener<T extends RelatedEntityDTO> {
    void dataChanged(RelatedEntityEvent<T> dataChangedEvent) ;
}
