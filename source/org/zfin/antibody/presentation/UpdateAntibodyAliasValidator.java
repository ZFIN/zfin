package org.zfin.antibody.presentation;

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.DataAlias;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.antibody.Antibody;
import org.apache.commons.lang.StringUtils;




public class UpdateAntibodyAliasValidator implements Validator {
    private MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private PublicationRepository pr = RepositoryFactory.getPublicationRepository();
     public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        AntibodyUpdateDetailBean formBean = (AntibodyUpdateDetailBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyDefPubZdbID(), formBean.AB_DEFPUB_ZDB_ID, errors);

        if (StringUtils.isEmpty(formBean.getNewAlias())) {
                errors.rejectValue("newAlias", "code", " Empty alias is not allowed.");
        }
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(formBean.getAntibody().getZdbID());
        DataAlias da=mr.getSpecificDataAlias(antibodytoUpdate,formBean.getNewAlias());
        if (da!=null) {
             errors.rejectValue("newAlias", "code", " This alias for this antibody already exists.");
        }
    }


}
