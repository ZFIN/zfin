package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


public class CloneAddValidator implements Validator {

    private static Logger logger = LogManager.getLogger(CloneAddValidator.class);

    public boolean supports(Class aClass) {
        return CloneAddBean.class.equals(aClass);
    }

    public void validate(Object command, Errors errors) {
        CloneAddBean formBean = (CloneAddBean) command;

        List<Marker> markerList = RepositoryFactory.getMarkerRepository().getMarkersByAbbreviation(formBean.getName());

        if (markerList.size() > 0) {
            errors.rejectValue("name", "code", "Name already in use.");
        }
    }

}