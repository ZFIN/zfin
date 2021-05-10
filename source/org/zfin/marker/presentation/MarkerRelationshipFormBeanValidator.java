package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;

public class MarkerRelationshipFormBeanValidator implements Validator {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    @Override
    public boolean supports(Class<?> aClass) {
        return MarkerRelationshipFormBean.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MarkerRelationshipFormBean bean = (MarkerRelationshipFormBean) o;

        try {
            // this method throws an exception if the relationship type is invalid
            MarkerRelationship.Type.getType(bean.getMarkerRelationshipType().getName());
        } catch (Exception e) {
            errors.rejectValue("relationship", "marker.relationship.type.invalid");
        }

        validateMarker(bean.getFirstMarker(), "firstMarker", errors);
        validateMarker(bean.getSecondMarker(), "secondMarker", errors);

        if (CollectionUtils.isEmpty(bean.getReferences())) {
            errors.rejectValue("references", "pub.empty");
        } else {
            for (Publication reference : bean.getReferences()) {
                PublicationValidator.validatePublicationID(reference.getZdbID(), "references", errors);
            }
        }
    }

    private void validateMarker(Marker marker, String field, Errors errors) {
        if (StringUtils.isBlank(marker.getZdbID()) && StringUtils.isBlank(marker.getAbbreviation())) {
            errors.rejectValue(field, "marker.relationship.marker.empty");
        }

        if (StringUtils.isNotBlank(marker.getZdbID()) &&
                !markerRepository.markerExistsForZdbID(marker.getZdbID())) {
            errors.rejectValue(field + ".zdbID", "marker.relationship.marker.invalid");
        } else if (StringUtils.isNotBlank(marker.getAbbreviation()) &&
                !markerRepository.isMarkerExists(marker.getAbbreviation())) {
            errors.rejectValue(field + ".abbreviation", "marker.relationship.marker.invalid");
        }
    }

    public static void validateMarkerRelationshipType(Marker firstMarker, Marker secondMarker, MarkerRelationshipType markerRelationshipType, Errors errors) {
        Marker.Type firstType = firstMarker.getType();
        Marker.Type secondType = secondMarker.getType();
        boolean firstTypeCorrect = markerRelationshipType.getFirstMarkerTypeGroup().hasType(firstType);
        boolean secondTypeCorrect = markerRelationshipType.getSecondMarkerTypeGroup().hasType(secondType);
        if (!firstTypeCorrect || !secondTypeCorrect) {
            errors.reject("marker.relationship.incompatibleTypes",
                    new String[] { markerRelationshipType.getName(), firstType.toString(), secondType.toString() },
                    "");
        }
    }
}
