package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.uniprot.persistence.BatchInserter;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;

import static org.zfin.framework.HibernateUtil.currentSession;

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
    public void processActions(List<SecondaryTermLoadAction> actions, SecondaryTermLoadAction.Type type) {
        if (type.equals(SecondaryTermLoadAction.Type.LOAD)) {
            processInsertQueries(actions);
        } else if (type.equals(SecondaryTermLoadAction.Type.DELETE)) {
            processDeleteQueries(actions);
        }
        currentSession().flush();
    }

    private static void processInsertQueries(List<SecondaryTermLoadAction> actions) {
        List<Tuple2<String, Integer>> tuples = actions.stream()
                .map(action -> new Tuple2<>(
                        action.getAccession(),
                        action.getLength()))
                .toList();
        processInsertsFromTuples(tuples);
    }

    public static void processInsertsFromTuples(List<Tuple2<String, Integer>> tuples) {
        List<String> uniprotIds = tuples.stream()
                .map(Tuple2::v1)
                .toList();

        log.info("Inserting Active Data ZDB IDs for protein table");
        BatchInserter.bulkLoadActiveDataZdbIDs(uniprotIds);

        log.info("Bulk loading protein records into temp table");
        currentSession().createNativeQuery("""
                CREATE TEMP TABLE temp_protein
                as SELECT * FROM protein WHERE false
                """).executeUpdate();

        List<Map<String, Object>> insertionRows = tuples.stream()
                .map(action -> Map.of(
                        "up_uniprot_id", (Object) action.v1(),
                        "up_fdbcont_zdb_id", FDBCONTID,
                        "up_length", action.v2()))
                .toList();
        BatchInserter.bulkInsert("temp_protein", insertionRows);

        log.info("Updating protein records");
        currentSession().createNativeQuery("""
                UPDATE protein
                SET up_length = temp_protein.up_length
                FROM temp_protein
                WHERE protein.up_uniprot_id = temp_protein.up_uniprot_id
                """).executeUpdate();

        int numDeleted = currentSession().createNativeQuery("""
                DELETE FROM temp_protein
                WHERE up_uniprot_id IN (SELECT up_uniprot_id FROM PROTEIN)
                """).executeUpdate();
        log.info(numDeleted + " records from temp_protein already existed in protein table");

        log.info("Inserting protein records");
        currentSession().createNativeQuery("""
                INSERT INTO protein 
                SELECT * FROM temp_protein
                """).executeUpdate();
    }

    private static void processDeleteQueries(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action: actions) {
                currentSession().createNativeQuery("DELETE FROM protein WHERE up_uniprot_id = :uniprot")
                        .setParameter("uniprot", action.getAccession())
                        .executeUpdate();
        }
    }

}
