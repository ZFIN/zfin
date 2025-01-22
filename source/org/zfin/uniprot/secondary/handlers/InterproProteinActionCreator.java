package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.DBLinkSlimDTO;
import org.zfin.uniprot.dto.ProteinDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates actions for adding and deleting protein domain information (replaces part of protein_domain_info_load.pl)
 * protein table load/delete (select up_uniprot_id, up_length from protein)
 * protein.txt was the legacy load file
 * Uses ProteinDTO
 */
@Log4j2
public class InterproProteinActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {

        List<ProteinDTO> existingProteins = (context.getExistingProteinRecords() != null) ? context.getExistingProteinRecords() : new ArrayList<>();
        List<ProteinDTO> proteinsToKeep = new ArrayList<>();
        List<SecondaryTermLoadAction> newActions = new ArrayList<>();

        Map<String, Integer> existingProteinsAsMap = existingProteins.stream().collect(Collectors.toMap(ProteinDTO::accession, ProteinDTO::length));

        for(String uniprotKey : uniProtRecords.getAccessions()) {
            RichSequenceAdapter richSequenceAdapter = uniProtRecords.getByAccession(uniprotKey);
            List<String> zdbIDs = context.getGeneZdbIDsByUniprot(uniprotKey);
            if (zdbIDs.isEmpty()) {
                continue;
            }

            int length = richSequenceAdapter.getLength();
            if (existingProteinsAsMap.containsKey(uniprotKey)) {
                Integer existingLength = existingProteinsAsMap.get(uniprotKey);
                if (length > 0 && !existingLength.equals(length)) {
                    existingProteinsAsMap.put(uniprotKey, length);
                    Optional<SecondaryTermLoadAction> maybeLoadAction = createLoadAction(context, uniprotKey, length, zdbIDs, uniProtRecords.getByAccession(uniprotKey));
                    maybeLoadAction.ifPresent(newActions::add);
                } else {
                    proteinsToKeep.add(new ProteinDTO(uniprotKey, length));
                }
            } else {
                existingProteinsAsMap.put(uniprotKey, length);
                Optional<SecondaryTermLoadAction> maybeLoadAction = createLoadAction(context, uniprotKey, length, zdbIDs, uniProtRecords.getByAccession(uniprotKey));
                maybeLoadAction.ifPresent(newActions::add);
                proteinsToKeep.add(new ProteinDTO(uniprotKey, length));
            }
        }

        for(ProteinDTO protein : existingProteins) {
            if (!proteinsToKeep.contains(protein)) {
                newActions.add(createDeleteAction(protein));
            }
        }
        return newActions;
    }

    private Optional<SecondaryTermLoadAction> createLoadAction(SecondaryLoadContext context, String accession, int length, List<String> zdbIDs, RichSequenceAdapter uniprotRecord) {
        //if we don't have a gene association, we don't want to add the protein
        if (!context.hasAnyUniprotGeneAssociation(accession, zdbIDs)) {
            return Optional.empty();
        }

        List<DBLinkSlimDTO> existingDbLinks = context.getGenesByUniprot(accession);
        Optional<DBLinkSlimDTO> autoCuratedDbLinks = existingDbLinks
                .stream()
//                .filter(dblink -> dblink.getPublicationIDs().contains(AUTOMATED_CURATION_OF_UNIPROT_DATABASE_LINKS)) //do we need this?
                .filter(dblink -> zdbIDs.contains(dblink.getDataZdbID())) //do we need this?
                .findFirst();

        if (autoCuratedDbLinks.isPresent()) {
            DBLinkSlimDTO existingDbLink = autoCuratedDbLinks.get();
            return Optional.of(SecondaryTermLoadAction.builder()
                    .type(SecondaryTermLoadAction.Type.LOAD)
                    .subType(SecondaryTermLoadAction.SubType.PROTEIN)
                    .accession(accession)
                    .length(length)
                    .uniprotAccessions(Set.of(uniprotRecord.getAccession()))
                    .build());
        } else {
            log.info("No auto-curated gene association found for UniProt accession: " + accession);
        }
        return Optional.empty();
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
