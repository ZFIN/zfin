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
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<ProteinDTO> existingProteins = context.getExistingProteinRecords();
        List<ProteinDTO> proteinsToKeep = new ArrayList<>();
        Map<String, Integer> existingProteinsAsMap = existingProteins.stream().collect(Collectors.toMap(ProteinDTO::accession, ProteinDTO::length));

        for(String uniprotKey : uniProtRecords.keySet()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.get(uniprotKey);
            Collection<CrossRefAdapter> zfinCrossRefs = richSequenceAdapter.getCrossRefsByDatabase("ZFIN");
            if (zfinCrossRefs.isEmpty()) {
                continue;
            }

            String accession = richSequenceAdapter.getAccession();
            int length = richSequenceAdapter.getLength();

            if (existingProteinsAsMap.containsKey(accession)) {
                Integer existingLength = existingProteinsAsMap.get(accession);
                if (length > 0 && !existingLength.equals(length)) {
                    existingProteinsAsMap.put(accession, length);
                    createLoadAction(context, actions, accession, length, zfinCrossRefs);
                } else {
                    proteinsToKeep.add(new ProteinDTO(accession, length));
                }
            } else {
                existingProteinsAsMap.put(accession, length);
                createLoadAction(context, actions, accession, length, zfinCrossRefs);
                proteinsToKeep.add(new ProteinDTO(accession, length));
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
        currentSession().doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO protein (up_uniprot_id, up_length) VALUES (?, ?)" +
                    " ON CONFLICT (up_uniprot_id) DO UPDATE SET up_length = EXCLUDED.up_length")) {
                for(SecondaryTermLoadAction action : actions) {
                    if (action.getType().equals(SecondaryTermLoadAction.Type.LOAD)) {
                        statement.setString(1, action.getAccession());
                        statement.setInt(2, action.getLength());
                        statement.execute();
                    }
                }
            }
        });
    }

    private static void processDeleteQueries(List<SecondaryTermLoadAction> actions) {
        currentSession().doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM protein WHERE up_uniprot_id = ?")) {
                for(SecondaryTermLoadAction action : actions) {
                    if (action.getType().equals(SecondaryTermLoadAction.Type.DELETE)) {
                        statement.setString(1, action.getAccession());
                        statement.execute();
                    }
                }
            }
        });
    }

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN;
    }

    private void createLoadAction(SecondaryLoadContext context, List<SecondaryTermLoadAction> actions, String accession, int length, Collection<CrossRefAdapter> zfinCrossRefs) {
        List<String> zdbIDs = zfinCrossRefs.stream().map(ref -> ref.getAccession()).toList();

        List<DBLinkSlimDTO> existingDbLinks = context
                .getGeneByUniprot(accession);
        if (existingDbLinks == null) {
            log.warn("No existing gene association found for UniProt accession: " + accession);
            return;
        }

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
