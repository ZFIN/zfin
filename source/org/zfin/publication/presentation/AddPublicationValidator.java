package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.antibody.presentation.AntibodyBean;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;


public class AddPublicationValidator implements Validator {

    public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {
        AntibodyBean formBean = (AntibodyBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyNewPubZdbID(), AntibodyBean.AB_NEWPUB_ZDB_ID, errors);

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution ra = ir.getRecordAttribution(formBean.getEntityID(), formBean.getAntibodyNewPubZdbID(), RecordAttribution.SourceType.STANDARD);
        if (ra != null) {
            errors.rejectValue("antibody", "code", " This antibody is already associated with pub: " + formBean.getAntibodyNewPubZdbID());
        }
    }
}
