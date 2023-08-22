package org.zfin.uniprot.handlers;

import org.biojavax.bio.seq.RichSequence;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.sequence.DBLink;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.UniProtTools;
import org.zfin.uniprot.dto.UniProtContextSequenceDTO;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.uniprot.UniProtTools.isGeneAccessionRelationshipSupportedByNonLoadPublication;

/**
 *
 */
public class ReportLostUniProtsHandler implements UniProtLoadHandler {

    @Override
    public void handle(Map<String, RichSequence> uniProtRecords, List<UniProtLoadAction> actions, UniProtLoadContext context) {
        //actions should contain all cases where we have a match based on RefSeq
        List<UniProtLoadAction> actionsMatchedOnRefSeq = actions.stream().filter(action -> action.getTitle().equals(UniProtLoadAction.MatchTitle.MATCH_BY_REFSEQ.getValue())).toList();

        System.out.println("ReportLostUniProtsHandler.handle. Count of actions: " + actions.size());
        System.out.println("ReportLostUniProtsHandler.handle. Filtered count of actions: " + actionsMatchedOnRefSeq.size());

        //all genes with existing uniprot associations
        List<UniProtContextSequenceDTO> sequencesForGenesWithExistingUniprotAssociations = context.getUniprotDbLinks()
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();


        //all genes that get matched based on RefSeqs in load file
        List<String> genesWithMatchesInLoad = actionsMatchedOnRefSeq.stream().map(UniProtLoadAction::getGeneZdbID).toList();

        //no duplicate printouts
        Set<String> alreadyEncounteredThisGeneID = new HashSet<>();

        //build up a list of genes that have existing uniprot associations but are not matched by RefSeq in load file
        List<UniProtContextSequenceDTO> lostUniProts = new ArrayList<>();
        for(UniProtContextSequenceDTO sequenceDTO : sequencesForGenesWithExistingUniprotAssociations) {
            if (!genesWithMatchesInLoad.contains(sequenceDTO.getDataZdbID())) {

                //no duplicates
                if (alreadyEncounteredThisGeneID.contains(sequenceDTO.getDataZdbID())) {
                    continue;
                }
                lostUniProts.add(sequenceDTO);
                alreadyEncounteredThisGeneID.add(sequenceDTO.getDataZdbID());
            }
        }

        //do some filtering based on attributions for lost UniProts
        List<UniProtContextSequenceDTO> filteredLostUniProts = new ArrayList<>();
        for(UniProtContextSequenceDTO lostUniProt: lostUniProts) {
            if (!isGeneAccessionRelationshipSupportedByNonLoadPublication(lostUniProt)) {
                filteredLostUniProts.add(lostUniProt);
            }
        }

        //create actions for lost UniProts
        for(UniProtContextSequenceDTO lostUniProt: filteredLostUniProts) {
            UniProtLoadAction action = new UniProtLoadAction();

            String sequenceDetails = "";
            String sequenceDatFileDetails = "Sequence details: \n=================\n";
            RichSequence richSequence = uniProtRecords.get(lostUniProt.getAccession());

            if (richSequence != null) {
                sequenceDatFileDetails += UniProtTools.sequenceToString(richSequence);

                //gene1
                Set<String> affectedGenes = new HashSet<>();
                affectedGenes.add(lostUniProt.getDataZdbID());

                //genes 2,3,etc.
                affectedGenes.addAll(context
                        .getUniprotDbLinks()
                        .get(lostUniProt.getAccession())
                        .stream()
                        .map(seq -> seq.getDataZdbID())
                        .collect(Collectors.toSet()));

                for(String gene: affectedGenes) {
                    action.addLink(new UniProtLoadLink("ZFIN: " + gene, "https://zfin.org/" + gene));
                }

                sequenceDetails = lostUniProt.getDataZdbID() + " (" + lostUniProt.getMarkerAbbreviation() + ") would lose its UniProt association with " + lostUniProt.getAccession() + ".\n";
                if (affectedGenes.size() > 1) {
                        sequenceDetails += "Compare these affected genes: " + String.join(", ", affectedGenes) + "\n";
                }
                sequenceDetails += "\n";
                sequenceDetails += sequenceDatFileDetails;

            } else {
                sequenceDetails += "No sequence details found for " + lostUniProt.getAccession();
            }

            action.setGeneZdbID(lostUniProt.getDataZdbID());
            action.setTitle(UniProtLoadAction.MatchTitle.LOST_UNIPROT.getValue());
            action.setType(UniProtLoadAction.Type.ERROR);
            action.setAccession(lostUniProt.getAccession());
            action.setDetails("This gene currently has a UniProt association, but when we run the latest\n" +
                    "UniProt release through our matching pipeline, we don't find a match.\n" +
                    "Perhaps this UniProt accession should be removed?\n\n" + sequenceDetails);

            action.addLink(new UniProtLoadLink("ZFIN: " + lostUniProt.getDataZdbID(), "https://zfin.org/" + lostUniProt.getDataZdbID()));
            action.addLink(new UniProtLoadLink("UniProt: " + lostUniProt.getAccession(), "https://www.uniprot.org/uniprot/" + lostUniProt.getAccession()));

            actions.add(action);
        }

    }

}
