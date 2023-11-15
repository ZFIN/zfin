package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.hibernate.query.Query;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.MarkerToProteinDTO;
import org.zfin.uniprot.interpro.PdbDTO;
import org.zfin.uniprot.interpro.ProteinToInterproDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Creates actions for adding and deleting PDB information (replaces part of protein_domain_info_load.pl)
 *  protein_to_pdb table (select ptp_uniprot_id, ptp_pdb_id from protein_to_pdb)
 * unipro2pdb.txt was the legacy load file
 * uses PdbDTO
 */
@Log4j2
public class PDBHandler implements SecondaryLoadHandler {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PDB;
    }

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<PdbDTO> existingRecords = context.getExistingPdbRecords();
        List<PdbDTO> keepRecords = new ArrayList<>(); //all the records to keep (not delete) includes new records too

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            Collection<CrossRefAdapter> zfinCrossRefs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
            if (zfinCrossRefs.isEmpty()) {
                continue;
            }
            List<String> pdbs = richSequenceAdapter.getCrossRefsByDatabase(RichSequenceAdapter.DatabaseSource.PDB)
                    .stream()
                    .map(ref -> ref.getAccession()).toList();

            for(String pdb : pdbs) {
                PdbDTO newRecord = new PdbDTO(uniprotKey, pdb);
                if (!existingRecords.contains(newRecord)) {
                    actions.add(createLoadAction(newRecord));
                }
                keepRecords.add(new PdbDTO(uniprotKey, pdb));
            }
        }

        for(PdbDTO existingRecord : existingRecords) {
            if (!keepRecords.contains(existingRecord)) {
                actions.add(createDeleteAction(existingRecord));
            }
        }

    }

    private SecondaryTermLoadAction createLoadAction(PdbDTO newRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PDB)
                .relatedEntityFields(newRecord.toMap())
                .handlerClass(this.getClass().getName())
                .build();
    }

    private SecondaryTermLoadAction createDeleteAction(PdbDTO dbRecord) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PDB)
                .relatedEntityFields(dbRecord.toMap())
                .handlerClass(this.getClass().getName())
                .build();
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        processInserts(actions);
        processDeletes(actions);
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
        currentSession().createSQLQuery(sqlQuery)
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
