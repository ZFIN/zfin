package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.uniprot.dto.ProteinToInterproDTO;
import org.zfin.uniprot.persistence.BatchInserter;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void processActions(List<SecondaryTermLoadAction> actions, SecondaryTermLoadAction.Type type) {
        if (type == SecondaryTermLoadAction.Type.LOAD) {
            processInserts(actions);
        } else if (type == SecondaryTermLoadAction.Type.DELETE) {
            processDeletes(actions);
        }
        currentSession().flush();
    }

    private void processInserts(List<SecondaryTermLoadAction> actions) {
        //collect the tuples to insert using the library
        List<Tuple2<String, String>> tuples = actions.stream()
                .map(action -> ProteinToInterproDTO.fromMap(action.getRelatedEntityFields()))
                .map(proteinToInterproDTO -> new Tuple2<>(proteinToInterproDTO.uniprot(), proteinToInterproDTO.interpro()))
                .toList();

        processInsertsFromTuples(tuples);
    }

    /**
     * This method is used to insert protein_to_interpro records in bulk.
     * It will insert the data in batches of 100.
     * The data to be inserted is a list of tuples. Each tuple is a pair of uniprot and interpro.
     * @param tuples
     */
    public void processInsertsFromTuples(List<Tuple2<String, String>> tuples) {
        log.info("Inserting protein_to_interpro records, count: " + tuples.size());

        //optimize for batch processing -> remove tuples if they don't have corresponding interpro_protein or protein
        String sqlQueryForProteins = "select up_uniprot_id from protein where up_uniprot_id in ('" +
                tuples.stream().map(Tuple2::v1).distinct().collect(Collectors.joining("', '")) + "')";
        List<String> uniprots = currentSession().createNativeQuery(sqlQueryForProteins).list().stream().map(Object::toString).toList();

        String sqlQueryForInterproProteins = "select ip_interpro_id from interpro_protein where ip_interpro_id in ('" +
                tuples.stream().map(Tuple2::v2).distinct().collect(Collectors.joining("', '")) + "')";
        List<String> interpros = currentSession().createNativeQuery(sqlQueryForInterproProteins).list().stream().map(Object::toString).toList();

        tuples = tuples.stream()
                .filter(tuple -> uniprots.contains(tuple.v1()) && interpros.contains(tuple.v2()))
                .toList();

        List<Map<String, Object>> tuplesAsMapEntriesList = tuples.stream().map(
                tuple -> Map.of("pti_uniprot_id", (Object)tuple.v1(), "pti_interpro_id", tuple.v2())
        ).toList();

        log.info("Filtered down to " + tuples.size() + " records");
        BatchInserter inserter = new BatchInserter("protein_to_interpro", tuplesAsMapEntriesList);
        inserter.execute();
    }

    private void processInsertIndividually(SecondaryTermLoadAction action) {
        ProteinToInterproDTO proteinToInterproDTO = ProteinToInterproDTO.fromMap(action.getRelatedEntityFields());
        String sqlQuery = """
                insert into protein_to_interpro (pti_uniprot_id, pti_interpro_id)
                select :uniprot, :interpro
                where exists (select 1 from interpro_protein where ip_interpro_id = :interpro) 
                and exists (select 1 from protein where up_uniprot_id = :uniprot)
                """;
        currentSession().createNativeQuery(sqlQuery)
                .setParameter("uniprot", proteinToInterproDTO.uniprot())
                .setParameter("interpro", proteinToInterproDTO.interpro())
                .executeUpdate();
    }

    private void processDeletes(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            processDelete(action);
        }
    }

    private void processDelete(SecondaryTermLoadAction action) {
        ProteinToInterproDTO proteinToInterproDTO = ProteinToInterproDTO.fromMap(action.getRelatedEntityFields());
        String sql = """
                delete from protein_to_interpro
                where pti_uniprot_id = :uniprot
                and pti_interpro_id = :interpro
                """;
        currentSession().createNativeQuery(sql)
                .setParameter("uniprot", proteinToInterproDTO.uniprot())
                .setParameter("interpro", proteinToInterproDTO.interpro())
                .executeUpdate();
    }

}
