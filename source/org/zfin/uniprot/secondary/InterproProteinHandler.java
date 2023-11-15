package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.interpro.ProteinDTO;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Creates actions for adding and deleting protein domain information (replaces part of protein_domain_info_load.pl)
 * protein table load/delete (select up_uniprot_id, up_length from protein)
 * protein.txt was the legacy load file
 * Uses ProteinDTO
 */
@Log4j2
public class InterproProteinHandler implements SecondaryLoadHandler {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN;
    }


    private static final String FDBCONTID = "ZDB-FDBCONT-040412-47";

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<ProteinDTO> existingProteins = context.getExistingProteinRecords();
        List<ProteinDTO> proteinsToKeep = new ArrayList<>();
        Map<String, Integer> existingProteinsAsMap = existingProteins.stream().collect(Collectors.toMap(ProteinDTO::accession, ProteinDTO::length));

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            List<String> zdbIDs = richSequenceAdapter.getCrossRefIDsByDatabase(RichSequenceAdapter.DatabaseSource.ZFIN);
            if (zdbIDs.isEmpty()) {
                continue;
            }

            int length = richSequenceAdapter.getLength();
            if (existingProteinsAsMap.containsKey(uniprotKey)) {
                Integer existingLength = existingProteinsAsMap.get(uniprotKey);
                if (length > 0 && !existingLength.equals(length)) {
                    existingProteinsAsMap.put(uniprotKey, length);
                    createLoadAction(context, actions, uniprotKey, length, zdbIDs);
                } else {
                    proteinsToKeep.add(new ProteinDTO(uniprotKey, length));
                }
            } else {
                existingProteinsAsMap.put(uniprotKey, length);
                createLoadAction(context, actions, uniprotKey, length, zdbIDs);
                proteinsToKeep.add(new ProteinDTO(uniprotKey, length));
            }
        }

        for(ProteinDTO protein : existingProteins) {
            if (!proteinsToKeep.contains(protein)) {
                actions.add(createDeleteAction(protein));
            }
        }
    }

    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        processInsertQueries(actions);
        processDeleteQueries(actions);
    }

    private static void processInsertQueries(List<SecondaryTermLoadAction> actions) {
        for(SecondaryTermLoadAction action : actions) {
            currentSession().createSQLQuery("""
            INSERT INTO zdb_active_data(zactvd_zdb_id) VALUES (:uniprot)
            ON CONFLICT (zactvd_zdb_id)
            DO NOTHING 
            """).setParameter("uniprot", action.getAccession())
                    .executeUpdate();

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

    private void createLoadAction(SecondaryLoadContext context, List<SecondaryTermLoadAction> actions, String accession, int length, List<String> zdbIDs) {

        //if we don't have a gene association, we don't want to add the protein
        if (!context.hasAnyUniprotGeneAssociation(accession, zdbIDs)) {
            log.warn("No existing gene association found for UniProt accession: " + accession);
            return;
        }

        List<DBLinkSlimDTO> existingDbLinks = context.getGeneByUniprot(accession);
        Optional<DBLinkSlimDTO> autoCuratedDbLinks = existingDbLinks
                .stream()
//                .filter(dblink -> dblink.getPublicationIDs().contains(AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS)) //do we need this?
                .filter(dblink -> zdbIDs.contains(dblink.getDataZdbID())) //do we need this?
                .findFirst();

        if (autoCuratedDbLinks.isPresent()) {
            DBLinkSlimDTO existingDbLink = autoCuratedDbLinks.get();
            actions.add(SecondaryTermLoadAction.builder()
                    .type(SecondaryTermLoadAction.Type.LOAD)
                    .subType(SecondaryTermLoadAction.SubType.PROTEIN)
                    .accession(accession)
                    .details(String.join(",", zdbIDs))
                    .length(length)
                    .handlerClass(this.getClass().getName())
                    .build());
        } else {
            log.debug("No auto-curated gene association found for UniProt accession: " + accession);
        }
    }

    private SecondaryTermLoadAction createDeleteAction(ProteinDTO protein) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN)
                .accession(protein.accession())
                .length(protein.length())
                .handlerClass(this.getClass().getName())
                .build();
    }

}
