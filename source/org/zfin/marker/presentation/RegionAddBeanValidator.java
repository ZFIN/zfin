package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.CreateAntibodyFormBean;
import org.zfin.marker.presentation.RegionAddBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;

public class RegionAddBeanValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        RegionAddBean formBean = (RegionAddBean) command;
        PublicationValidator.validatePublicationID(formBean.getRegionPublicationZdbID(), RegionAddBean.REGION_PUBLICATION_ZDB_ID, errors);

        String regionName = formBean.getRegionName();
        if (StringUtils.isEmpty(regionName)) {
            errors.rejectValue("regionName", "code", "Engineered region name cannot be null.");
        }

        if (!regionName.equals(StringUtils.upperCase(regionName))) {
            errors.rejectValue("regionName", "code", "Engineered region name must be all upper case.");
        }

        if (!StringUtils.isEmpty(regionName)) {
            if (mr.isMarkerExists(regionName)) {
                errors.rejectValue("regionName", "code", "The marker abbreviation [" + regionName + "] is already taken by another marker");
                return;
            }
            Marker marker = mr.getMarkerByName(regionName);
            if (marker != null) {
                errors.rejectValue("regionName", "code", "The marker name [" + regionName + "] is already taken by another marker");
            }
        }

    }
}

