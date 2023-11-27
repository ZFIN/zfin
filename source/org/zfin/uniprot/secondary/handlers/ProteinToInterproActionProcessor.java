package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.dto.ProteinToInterproDTO;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Creates actions for adding and deleting protein to interpro information (replaces part of protein_domain_info_load.pl)
 * protein_to_interpro table (select pti_uniprot_id, pti_interpro_id from protein_to_interpro)
 * unipro2interpro.txt was the legacy load file
 * Uses ProteinToInterproDTO
 */
@Log4j2
public class ProteinToInterproActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN_TO_INTERPRO;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            if (action.getType() == SecondaryTermLoadAction.Type.LOAD) {
                processInsert(action);
            } else if (action.getType() == SecondaryTermLoadAction.Type.DELETE) {
                processDelete(action);
            }
        }
        currentSession().flush();
    }

    private void processInsert(SecondaryTermLoadAction action) {
        ProteinToInterproDTO proteinToInterproDTO = ProteinToInterproDTO.fromMap(action.getRelatedEntityFields());
        log.debug("inserting protein to interpro record: " + proteinToInterproDTO.uniprot() + "/" + proteinToInterproDTO.interpro());
        String sqlQuery = """
                insert into protein_to_interpro (pti_uniprot_id, pti_interpro_id)
                select :uniprot, :interpro 
                where exists (select 1 from interpro_protein where ip_interpro_id = :interpro) 
                and exists (select 1 from protein where up_uniprot_id = :uniprot)
                """;
        currentSession().createSQLQuery(sqlQuery)
                .setParameter("uniprot", proteinToInterproDTO.uniprot())
                .setParameter("interpro", proteinToInterproDTO.interpro())
                .executeUpdate();
    }

    private void processDelete(SecondaryTermLoadAction action) {
        ProteinToInterproDTO proteinToInterproDTO = ProteinToInterproDTO.fromMap(action.getRelatedEntityFields());
        String sql = """
                delete from protein_to_interpro
                where pti_uniprot_id = :uniprot
                and pti_interpro_id = :interpro
                """;
        currentSession().createSQLQuery(sql)
                .setParameter("uniprot", proteinToInterproDTO.uniprot())
                .setParameter("interpro", proteinToInterproDTO.interpro())
                .executeUpdate();
    }

}
