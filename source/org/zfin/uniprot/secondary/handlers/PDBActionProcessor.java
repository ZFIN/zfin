package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.hibernate.query.Query;
import org.zfin.uniprot.dto.PdbDTO;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Creates actions for adding and deleting PDB information (replaces part of protein_domain_info_load.pl)
 *  protein_to_pdb table (select ptp_uniprot_id, ptp_pdb_id from protein_to_pdb)
 * unipro2pdb.txt was the legacy load file
 * uses PdbDTO
 */
@Log4j2
public class PDBActionProcessor implements ActionProcessor {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PDB;
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions, SecondaryTermLoadAction.Type type) {
        processInserts(actions);
        processDeletes(actions);
        currentSession().flush();
    }

    private void processInserts(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            if (action.getType() == SecondaryTermLoadAction.Type.LOAD) {
                PdbDTO newRecord = PdbDTO.fromMap(action.getRelatedEntityFields());
                insertProteinToPDB(newRecord.uniprot(), newRecord.pdb());
            }
        }
    }

    private void processDeletes(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            if (action.getType() == SecondaryTermLoadAction.Type.DELETE) {
                PdbDTO dbRecord = PdbDTO.fromMap(action.getRelatedEntityFields());
                deleteProteinToPDB(dbRecord.uniprot());
            }
        }
    }

    public void insertProteinToPDB(String uniprot, String pdb) {
        String sqlQuery = """
                insert into protein_to_pdb(ptp_uniprot_id, ptp_pdb_id)
                select :uniprot, :pdb 
                where exists (select 1 from protein where up_uniprot_id = :uniprot)
                """;
        currentSession().createNativeQuery(sqlQuery)
            .setParameter("uniprot", uniprot)
            .setParameter("pdb", pdb)
            .executeUpdate();
    }

    private void deleteProteinToPDB(String uniprot) {

        //TODO: does this logic need to be preserved?
//        delete from protein_to_pdb
//        where not exists(select 'x' from protein
//        where up_uniprot_id = ptp_uniprot_id);

        String sql = """
        delete from protein_to_pdb
        where ptp_uniprot_id = :uniprot
        """;

        Query query = currentSession().createNativeQuery(sql);
        query.setParameter("uniprot", uniprot);
        query.executeUpdate();
    }

}
