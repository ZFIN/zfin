package org.zfin.construct.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


public class ConstructAddValidator implements Validator {

    private static Logger logger = LogManager.getLogger(ConstructAddValidator.class);

    public boolean supports(Class aClass) {
        return ConstructAddBean.class.equals(aClass);
    }

    public void validate(Object command, Errors errors) {
        ConstructAddBean formBean = (ConstructAddBean) command;
     /*   if (!formBean.getConstructPublicationZdbID().startsWith("ZDB-PUB-")) {
            errors.rejectValue("constructPublicationZdbID", "code", "Please enter the full publication id starting with ZDB-PUB.");
        }*/

        //PublicationValidator.validatePublicationID(formBean.getConstructPublicationZdbID(), ConstructAddBean.CONSTRUCT_PUBLICATION_ZDB_ID, errors);
       if (StringUtils.length(formBean.getConstructName())<=3){
           errors.rejectValue("name", "code", "Construct cannot be blank.");
       }

        List<Marker> markerList = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(formBean.getConstructName());

        if (markerList.size() > 0) {
            errors.rejectValue("name", "code", "Construct already in use.");
        }
    }

}