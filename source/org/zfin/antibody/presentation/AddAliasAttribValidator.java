package org.zfin.antibody.presentation;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.presentation.PublicationValidator;
import org.apache.commons.lang.StringUtils;


public class AddAliasAttribValidator implements Validator {

public boolean supports(Class aClass) {
        return true;
    }

    public void validate(Object command, Errors errors) {
        AntibodyUpdateDetailBean formBean = (AntibodyUpdateDetailBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyDefPubZdbID(), formBean.AB_DEFPUB_ZDB_ID, errors);
        if (StringUtils.isEmpty(formBean.getAntibodyAliaszdbID())) {
                errors.rejectValue("attribAlias", "code", " Alias is null..cannot be attributed.");
        }
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution ra=ir.getRecordAttribution(formBean.getAttribAlias(), formBean.getAntibodyDefPubZdbID(), RecordAttribution.SourceType.STANDARD);
        if (ra!=null) {
             errors.rejectValue("attribAlias", "code", " This alias is already associated with this pub.");
        }
}

}