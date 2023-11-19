package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.sequence.InterProProtein;
import org.zfin.uniprot.dto.InterProProteinDTO;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Creates actions for adding and deleting protein domain information
 * interpro_protein table for load/delete
 * domain.txt was the legacy load file
 * Uses InterProProteinDTO
 */
@Log4j2
public class InterproDomainActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        for (SecondaryTermLoadAction action : actions) {
            InterProProteinDTO iprDTO = InterProProteinDTO.fromMap(action.getRelatedEntityFields());
            InterProProtein ipr = iprDTO.toInterProProtein();
            if (action.getType() == SecondaryTermLoadAction.Type.LOAD) {
                getInfrastructureRepository().insertActiveDataWithoutValidation(ipr.getIpID());
                currentSession().save(ipr);
            } else if (action.getType() == SecondaryTermLoadAction.Type.DELETE) {
                currentSession().delete(ipr);
            }
        }
        currentSession().flush();
    }

}
