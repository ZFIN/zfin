package org.zfin.antibody.presentation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Prita Mani
 * Date: Jun 27, 2008
 * Time: 1:46:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateAntibodyNotesValidator implements Validator {


    private PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    public boolean supports(Class aClass) {
        return true;
    }


    public void validate(Object command, Errors errors) {
        AntibodyUpdateDetailBean formBean = (AntibodyUpdateDetailBean) command;
        PublicationValidator.validatePublicationID(formBean.getAntibodyDefPubZdbID(), formBean.AB_DEFPUB_ZDB_ID, errors);


    }


}
