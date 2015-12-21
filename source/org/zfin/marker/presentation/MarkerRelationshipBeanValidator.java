package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;

public class MarkerRelationshipBeanValidator implements Validator {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return MarkerRelationshipBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MarkerRelationshipBean bean = (MarkerRelationshipBean) o;

        MarkerRelationship.Type type = MarkerRelationship.Type.getType(bean.getRelationship());
        if (type == null) {
            errors.rejectValue("relationship", "marker.relationship.type.invalid");
        }

        validateMarker(bean.getFirst(), "first", errors);
        validateMarker(bean.getSecond(), "second", errors);

        if (CollectionUtils.isEmpty(bean.getReferences())) {
            errors.rejectValue("references", "pub.empty");
        } else {
            for (MarkerReferenceBean reference : bean.getReferences()) {
                PublicationValidator.validatePublicationID(reference.getZdbID(), "references", errors);
            }
        }
    }

    private void validateMarker(MarkerDTO marker, String field, Errors errors) {
        if (StringUtils.isBlank(marker.getZdbID()) && StringUtils.isBlank(marker.getName())) {
            errors.rejectValue(field, "marker.relationship.marker.empty");
        }

        if (StringUtils.isNotBlank(marker.getZdbID()) &&
                !markerRepository.markerExistsForZdbID(marker.getZdbID())) {
            errors.rejectValue(field, "marker.relationship.marker.invalid");
        } else if (StringUtils.isNotBlank(marker.getName()) &&
                !markerRepository.isMarkerExists(marker.getName())) {
            errors.rejectValue(field, "marker.relationship.marker.invalid");
        }
    }
}
