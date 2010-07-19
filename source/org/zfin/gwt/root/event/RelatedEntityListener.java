package org.zfin.gwt.root.event;

import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 */
public interface RelatedEntityListener<U extends RelatedEntityDTO>{

    //essentially, U is defined above, then the same U definition is used below
    public void addRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) ;
    public void addAttribution(RelatedEntityEvent<U> relatedEntityEvent) ;
    public void removeRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) ;
    public void removeAttribution(RelatedEntityEvent<U> relatedEntityEvent) ;
}
