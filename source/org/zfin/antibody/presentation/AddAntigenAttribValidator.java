package org.zfin.antibody.presentation;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.presentation.PublicationValidator;
import org.apache.commons.lang.StringUtils;


public class AddAntigenAttribValidator implements Validator {

public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {
        AntibodyUpdateDetailBean formBean = (AntibodyUpdateDetailBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyDefPubZdbID(), formBean.AB_DEFPUB_ZDB_ID, errors);
        if (StringUtils.isEmpty(formBean.getAntibodyAntigenzdbID())) {
                errors.rejectValue("attribAntigen", "code", " Antigen is null..cannot be attributed.");
        }
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution ra=ir.getRecordAttribution(formBean.getAttribAntigen(), formBean.getAntibodyDefPubZdbID(), RecordAttribution.SourceType.STANDARD);
        if (ra!=null) {
             errors.rejectValue("attribAntigen", "code", " This antigen is already associated with this pub.");
        }
}


}
