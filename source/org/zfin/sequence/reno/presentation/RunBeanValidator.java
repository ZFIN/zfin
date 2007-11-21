package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.repository.RenoRepository;


public class RunBeanValidator implements Validator {

    private static Logger LOG = Logger.getLogger(RunBeanValidator.class);
    private RenoRepository rr = RepositoryFactory.getRenoRepository();

    public boolean supports(Class clazz) {
        return clazz.equals(RunBean.class);
    }

    public void validate(Object command, Errors errors) {

        RunBean runBean = (RunBean) command;
        Run run = rr.getRunByID(runBean.getZdbID());
        LOG.info("start validating");

        // validate the nomenclature publication zdb id
        PublicationValidator.validatePublicationID(runBean.getNomenclaturePublicationZdbID(), RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID, errors);

        // if nomenclature, also validate orthology attribution zdb id
        if (run.isNomenclature()) {
            PublicationValidator.validatePublicationID(runBean.getOrthologyPublicationZdbID(), RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID, errors);
        }

        // if Redundancy, also validate Relations attribution zdb id
        if (run.isRedundancy()) {
            PublicationValidator.validatePublicationID(runBean.getRelationPublicationZdbID(), RunBean.RELATION_PUBLICATION_ZDB_ID, errors);
        }
    }

}
