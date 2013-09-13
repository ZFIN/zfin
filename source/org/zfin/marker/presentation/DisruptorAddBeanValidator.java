package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ZfinStringUtils;

public class DisruptorAddBeanValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();

    public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {
        DisruptorAddBean formBean = (DisruptorAddBean) command;

        if (!formBean.getDisruptorPublicationZdbID().startsWith("ZDB-PUB-")) {
            errors.rejectValue("disruptorPublicationZdbID", "code", "Please enter the full publication id starting with ZDB-PUB.");
        }

        PublicationValidator.validatePublicationID(formBean.getDisruptorPublicationZdbID(), DisruptorAddBean.DISRUPTOR_PUBLICATION_ZDB_ID, errors);

        String disruptorName = formBean.getDisruptorName();
        if (StringUtils.isEmpty(disruptorName)) {
            errors.rejectValue("disruptorName", "code", "The name of the new sequence-targeting reagent cannot be null.");
        }

        if (!StringUtils.isEmpty(disruptorName)) {
            if (mr.isMarkerExists(disruptorName)) {
                errors.rejectValue("disruptorName", "code", "The sequence-targeting reagent [" + disruptorName + "] is already at ZFIN");
                return;
            }
            Marker marker = mr.getMarkerByName(disruptorName);
            if (marker != null) {
                errors.rejectValue("disruptorName", "code", "The sequence-targeting reagent [" + disruptorName + "] is already at ZFIN");
            }
        }

        String disruptorSequence = formBean.getDisruptorSequence();
        if (StringUtils.isEmpty(disruptorSequence)) {
            errors.rejectValue("disruptorSequence", "code", "The sequence of [" + disruptorName + "]  cannot be null.");
        }

        if(ZfinStringUtils.containsWhiteSpaceOrNoneATGC(disruptorSequence)) {
            errors.rejectValue("disruptorSequence", "code", "The sequence of [" + disruptorName + "]  cannot contain white space or character other than ATGC.");
        }

        String disruptorType = formBean.getDisruptorType();

        if (disruptorType.equalsIgnoreCase("TALEN")) {
            String disruptorSecondSequence = formBean.getDisruptorSecondSequence();
            if (StringUtils.isEmpty(disruptorSecondSequence)) {
                errors.rejectValue("disruptorSecondSequence", "code", "The second sequence of [" + disruptorName + "]  cannot be null.");
            }
            if(ZfinStringUtils.containsWhiteSpaceOrNoneATGC(disruptorSecondSequence)) {
                errors.rejectValue("disruptorSecondSequence", "code", "The second sequence of [" + disruptorName + "]  cannot contain white space or character other than ATGC.");
            }

        }
    }
}




