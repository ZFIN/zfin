package org.zfin.gwt.root.ui;


import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.server.DTOConversionService;


/**
 * This is a composite for related entities.
 */
public class RelatedEntityBox extends AbstractRelatedEntityBox<RelatedEntityDTO> {


    public void addRelatedEntity(final String name, final String pubZdbID) {
        // do client check
        String validationError = validateNewRelatedEntity(name);
        if (validationError != null) {
            setError(validationError);
            return;
        }
        RelatedEntityDTO dto = new RelatedEntityDTO();
        dto.setDataZdbID(getZdbID());
        dto.setName(name);
        dto.setPublicationZdbID(pubZdbID);
        fireRelatedEntityAdded(new RelatedEntityEvent<RelatedEntityDTO>(dto));
    }

    public void removeAttribution(RelatedEntityDTO relatedEntityDTO) {
        fireAttributionRemoved(new RelatedEntityEvent<RelatedEntityDTO>(relatedEntityDTO));
    }

    public boolean isEditable(RelatedEntityDTO relatedEntityDTO) {
        return true;
    }


}