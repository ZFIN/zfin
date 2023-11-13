package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.uniprot.adapter.CrossRefAdapter;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.interpro.EntryListItemDTO;
import org.zfin.uniprot.interpro.ProteinDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.zfin.uniprot.UniProtTools.AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS;

/**
 * Creates actions for adding and deleting protein domain information (replaces part of protein_domain_info_load.pl)
 * protein table load/delete (select up_uniprot_id, up_length from protein)
 * protein.txt was the legacy load file
 * Uses ProteinDTO
 */
@Log4j2
public class InterproProteinHandler implements SecondaryLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

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
                .build();
    }

}
