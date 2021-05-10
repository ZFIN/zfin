package org.zfin.gwt.root.event;

import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 * A convenience class.
 */
public class RelatedEntityAdapter<T extends RelatedEntityDTO> implements RelatedEntityListener<T> {

    @Override
    public void addRelatedEntity(RelatedEntityEvent<T> relatedEntityEvent) {
    }

    @Override
    public void addAttribution(RelatedEntityEvent<T> relatedEntityEvent) {
    }

    @Override
    public void removeRelatedEntity(RelatedEntityEvent<T> relatedEntityEvent) {
    }

    @Override
    public void removeAttribution(RelatedEntityEvent<T> relatedEntityEvent) {
    }
}
