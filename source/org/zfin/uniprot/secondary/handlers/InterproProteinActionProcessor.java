package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Creates actions for adding and deleting protein domain information (replaces part of protein_domain_info_load.pl)
 * protein table load/delete (select up_uniprot_id, up_length from protein)
 * protein.txt was the legacy load file
 * Uses ProteinDTO
 */
@Log4j2
public class InterproProteinActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN;
    }

    private static final String FDBCONTID = "ZDB-FDBCONT-040412-47";

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        processInsertQueries(actions);
        processDeleteQueries(actions);
        currentSession().flush();
    }

    private static void processInsertQueries(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            getInfrastructureRepository().insertActiveDataWithoutValidationIgnoreConflict(action.getAccession());

            currentSession().createSQLQuery("""
            INSERT INTO protein (up_uniprot_id, up_fdbcont_zdb_id, up_length) VALUES (:uniprot, :fdbcont, :length)
            ON CONFLICT (up_uniprot_id) 
            DO UPDATE SET up_length = EXCLUDED.up_length
            """).setParameter("uniprot", action.getAccession())
                    .setParameter("fdbcont", FDBCONTID)
                    .setParameter("length", action.getLength())
                    .executeUpdate();
        }
    }

    private static void processDeleteQueries(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action: actions) {
            if (action.getType().equals(SecondaryTermLoadAction.Type.DELETE)) {
                currentSession().createSQLQuery("DELETE FROM protein WHERE up_uniprot_id = :uniprot")
                        .setParameter("uniprot", action.getAccession())
                        .executeUpdate();
            }
        }
    }

}
