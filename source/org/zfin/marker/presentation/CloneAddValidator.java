package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.List;


public class CloneAddValidator implements Validator {

    private static Logger logger = Logger.getLogger(CloneAddValidator.class);

    public boolean supports(Class aClass) {
        logger.info("clss should be supported: " + CloneAddBean.class.equals(aClass));
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