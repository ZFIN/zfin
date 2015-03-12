package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.ZfinStringUtils;

public class SequenceTargetingReagentAddBeanValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private ProfileRepository pr = RepositoryFactory.getProfileRepository();

    public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {
        SequenceTargetingReagentAddBean formBean = (SequenceTargetingReagentAddBean) command;

        if (!formBean.getSequenceTargetingReagentPublicationID().startsWith("ZDB-PUB-")) {
            errors.rejectValue("sequenceTargetingReagentPublicationID", "code", "Please enter the full publication id starting with ZDB-PUB.");
        }

        PublicationValidator.validatePublicationID(formBean.getSequenceTargetingReagentPublicationID(), SequenceTargetingReagentAddBean.STR_PUBLICATION_ZDB_ID, errors);

        String strName = formBean.getSequenceTargetingReagentName();
        if (StringUtils.isEmpty(strName)) {
            errors.rejectValue("sequenceTargetingReagentName", "code", "The name of the new sequence-targeting reagent cannot be null.");
        }

        if (!StringUtils.isEmpty(strName)) {
            if (mr.isMarkerExists(strName)) {
                errors.rejectValue("sequenceTargetingReagentName", "code", "The sequence-targeting reagent [" + strName + "] is already at ZFIN");
                return;
            }
            Marker marker = mr.getMarkerByName(strName);
            if (marker != null) {
                errors.rejectValue("sequenceTargetingReagentName", "code", "The sequence-targeting reagent [" + strName + "] is already at ZFIN");
            }
        }

        String strSequence = formBean.getSequenceTargetingReagentSequence();
        if (StringUtils.isEmpty(strSequence)) {
            errors.rejectValue("sequenceTargetingReagentSequence", "code", "The sequence of [" + strName + "] cannot be null.");
        }

        String targetGeneSymbol = formBean.getTargetGeneSymbol();
        if (StringUtils.isEmpty(targetGeneSymbol)) {
            errors.rejectValue("targetGeneSymbol", "code", "The target gene of [" + strName + "] cannot be null.");
        } else if (mr.getGeneByAbbreviation(targetGeneSymbol) == null) {
            errors.rejectValue("targetGeneSymbol", "code", targetGeneSymbol + " cannot be found.");
        }

        String strSupplier = formBean.getSequenceTargetingReagentSupplierName();
        if (!StringUtils.isEmpty(strSupplier)) {
            Organization supplier = pr.getOrganizationByName(strSupplier);
            if (supplier == null) {
                errors.rejectValue("sequenceTargetingReagentSupplierName", "code", strSupplier + " cannot be found.");
            }
        }

        if(ZfinStringUtils.containsWhiteSpaceOrNoneATGC(strSequence)) {
            errors.rejectValue("sequenceTargetingReagentSequence", "code", "The sequence of [" + strName + "] cannot contain white space or character other than ATGC.");
        }

        String strType = formBean.getSequenceTargetingReagentType();

        if (strType.equalsIgnoreCase("TALEN")) {
            String strSecondSequence = formBean.getSequenceTargetingReagentSecondSequence();
            if (StringUtils.isEmpty(strSecondSequence)) {
                errors.rejectValue("sequenceTargetingReagentSecondSequence", "code", "The second sequence of [" + strName + "] cannot be null.");
            }
            if(ZfinStringUtils.containsWhiteSpaceOrNoneATGC(strSecondSequence)) {
                errors.rejectValue("sequenceTargetingReagentSecondSequence", "code", "The second sequence of [" + strName + "] cannot contain white space or character other than ATGC.");
            }

        }
    }
}

