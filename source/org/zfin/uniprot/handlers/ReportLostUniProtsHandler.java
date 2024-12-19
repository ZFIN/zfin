package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.sequence.ForeignDB.AvailableName.*;
import static org.zfin.uniprot.UniProtLoadAction.SubType.LOST_UNIPROT_PREV_MATCH_BY_GB;

/**
 * This handler creates DELETE actions that let curators know about any genes that currently have uniprot associations,
 * but are not matched by RefSeq in the load file -- therefore will lose their uniprot associations.
 *
 * Exceptions:
 * 1.   If the uniprot association is attributed to a non-load publication, then it is not considered a lost uniprot--so don't delete it.
 * 2.   If the uniprot association is due to a previous match by GP or GB, then it is not considered a lost uniprot--so don't delete it.
 *      (Note: these are still recorded as INFO action items so curators can review them if they'd like)
 */
@Log4j2
public class ReportLostUniProtsHandler implements UniProtLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        //actions should contain all cases where we have a match based on RefSeq
        List<UniProtLoadAction> actionsMatchedOnRefSeq = actions.stream()
                .filter(action -> UniProtLoadAction.Type.LOAD.equals(action.getType()))
                .filter(
                    action -> action.getSubType().equals(UniProtLoadAction.SubType.MATCH_BY_REFSEQ)
                            || action.getSubType().equals(UniProtLoadAction.SubType.ADD_ATTRIBUTION)
        ).toList();

        List<UniProtLoadAction> actionsMatchedWithMultipleGenes = actions.stream().filter(
                action -> action.getSubType().equals(UniProtLoadAction.SubType.MULTIPLE_GENES_PER_ACCESSION) ||
                        action.getSubType().equals(UniProtLoadAction.SubType.MULTIPLE_GENES_PER_ACCESSION_BUT_APPROVED)
        ).toList();

        log.info("ReportLostUniProtsHandler - Count of actions: " + actions.size());
        log.info("ReportLostUniProtsHandler - Filtered count of actions: " + actionsMatchedOnRefSeq.size());

        //all genes with existing uniprot associations
        List<DBLinkSlimDTO> sequencesForGenesWithExistingUniprotAssociations = context.getUniprotDbLinks()
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();


        //all genes that get matched based on RefSeqs in load file
        List<String> genesWithMatchesInLoad = new ArrayList<>(actionsMatchedOnRefSeq.stream().map(UniProtLoadAction::getGeneZdbID).toList());

        //include in the list of genes with matches in load: genes that get matched based on RefSeqs in load file, but have multiple genes per accession
        List<String> genesWithMultipleMatches = actionsMatchedWithMultipleGenes.stream().flatMap(
                action -> Arrays.stream(action.getGeneZdbID().split(";"))
        ).toList();
        genesWithMatchesInLoad.addAll(genesWithMultipleMatches);

        Set<String> alreadyEncounteredThisGeneID = new HashSet<>();

        //build up a list of genes that have existing uniprot associations but are not matched by RefSeq in load file
        List<DBLinkSlimDTO> genesThatLostUniProts = new ArrayList<>();
        for(DBLinkSlimDTO sequenceDTO : sequencesForGenesWithExistingUniprotAssociations) {
            if (!genesWithMatchesInLoad.contains(sequenceDTO.getDataZdbID())) {

                //no duplicates
                if (alreadyEncounteredThisGeneID.contains(sequenceDTO.getDataZdbID())) {
                    continue;
                }
                genesThatLostUniProts.add(sequenceDTO);
                alreadyEncounteredThisGeneID.add(sequenceDTO.getDataZdbID());
            }
        }

        //do some filtering based on attributions for lost UniProts
        List<DBLinkSlimDTO> filteredLostUniProts = new ArrayList<>();
        List<DBLinkSlimDTO> removeAttributionOnly = new ArrayList<>();

        for(DBLinkSlimDTO lostUniProt: genesThatLostUniProts) {
            if (lostUniProt.containsNonLoadPublication()) {
                if (lostUniProt.containsLoadPublication()) {
                    removeAttributionOnly.add(lostUniProt);
                }
            } else {
                filteredLostUniProts.add(lostUniProt);
            }
        }

        //create actions for lost UniProts
        for(DBLinkSlimDTO lostUniProt: filteredLostUniProts) {
            UniProtLoadAction action = createDeleteAction(uniProtRecords, context, lostUniProt);
            actions.add(action);
        }

        //create actions for removing load attribution if associated with non-load publication
        for(DBLinkSlimDTO lostUniProt: removeAttributionOnly) {
            UniProtLoadAction action = createRemoveAttributionAction(uniProtRecords, context, lostUniProt);
            actions.add(action);
        }

    }

    private UniProtLoadAction createDeleteAction(Map<String, RichSequenceAdapter> uniProtRecords, UniProtLoadContext context, DBLinkSlimDTO lostUniProt) {
        UniProtLoadAction action = new UniProtLoadAction();

        String sequenceDetails = "";
        RichSequenceAdapter richSequence = uniProtRecords.get(lostUniProt.getAccession());

        if (richSequence != null) {
            Set<String> refSeqIDs = richSequence.getRefSeqs();
            refSeqIDs.forEach(refSeqID -> {
                action.addLink(UniProtLoadLink.create(REFSEQ, refSeqID));
            });

            //gene1
            Set<String> affectedGenes = new HashSet<>();
            affectedGenes.add(lostUniProt.getDataZdbID());

            //genes 2,3,etc.
            affectedGenes.addAll(context
                    .getUniprotDbLinks()
                    .get(lostUniProt.getAccession())
                    .stream()
                    .map(DBLinkSlimDTO::getDataZdbID)
                    .collect(Collectors.toSet()));

            for(String gene: affectedGenes) {
                action.addLink(UniProtLoadLink.create(ZFIN, gene, "#sequences"));
            }

            sequenceDetails = lostUniProt.getDataZdbID() + " (" + lostUniProt.getMarkerAbbreviation() + ") would lose its UniProt association with " + lostUniProt.getAccession() + ".\n";
            if (affectedGenes.size() > 1) {
                    sequenceDetails += "These genes currently have links to " + lostUniProt.getAccession() + " : " + String.join(", ", affectedGenes) + "\n";
            }

        } else {
            sequenceDetails += "No sequence details found for " + lostUniProt.getAccession();
        }

        action.setGeneZdbID(lostUniProt.getDataZdbID());
        action.setSubType(UniProtLoadAction.SubType.LOST_UNIPROT);
        action.setType(UniProtLoadAction.Type.DELETE);
        action.setAccession(lostUniProt.getAccession());
        action.setDetails("""
                This gene currently has a UniProt association, but when we run the latest
                UniProt release through our matching pipeline, we don't find a match.
                """);

        action.addLink(UniProtLoadLink.create(ZFIN, lostUniProt.getDataZdbID(), "#sequences"));
        action.addLink(UniProtLoadLink.create(UNIPROTKB, lostUniProt.getAccession()));


        setActionTitleAndDetailsForGenPeptGenBank(lostUniProt, action, context);
        if (action.getType().equals(UniProtLoadAction.Type.DELETE)) {
            action.setDetails(action.getDetails() + "\n" + "This association will be removed.");
        }
        action.setDetails(action.getDetails() + "\n\n" + sequenceDetails);
        return action;
    }

    private UniProtLoadAction createRemoveAttributionAction(Map<String, RichSequenceAdapter> uniProtRecords, UniProtLoadContext context, DBLinkSlimDTO lostUniProt) {
        UniProtLoadAction action = new UniProtLoadAction();

        String sequenceDetails = "";
        RichSequenceAdapter richSequence = uniProtRecords.get(lostUniProt.getAccession());

        if (richSequence != null) {
            //Add refseqs
            Set<String> refSeqIDs = richSequence.getRefSeqs();
            refSeqIDs.forEach(refSeqID -> {
                action.addLink(UniProtLoadLink.create(REFSEQ, refSeqID));
            });
        } else {
            sequenceDetails += "No sequence details found for " + lostUniProt.getAccession();
        }

        action.setGeneZdbID(lostUniProt.getDataZdbID());
        action.setSubType(UniProtLoadAction.SubType.REMOVE_ATTRIBUTION);
        action.setType(UniProtLoadAction.Type.DELETE);
        action.setAccession(lostUniProt.getAccession());
        action.setDetails("This gene currently has a UniProt association, but when we run the latest\n" +
                "UniProt release through our matching pipeline, we don't find a match.\n\n\n" + sequenceDetails);

        action.addLink(UniProtLoadLink.create(ZFIN, lostUniProt.getDataZdbID(), "#sequences"));
        action.addLink(UniProtLoadLink.create(UNIPROTKB, lostUniProt.getAccession()));

        return action;
    }

    /**
     * This is a special case where we have a UniProt association that is based on a GenPept or GenBank sequence (based on old load logic).
     * In the future, we may want to remove this because over time they should get manually attributed.
     *
     * @param lostUniProt
     * @param action
     * @param context
     */
    private void setActionTitleAndDetailsForGenPeptGenBank(DBLinkSlimDTO lostUniProt, UniProtLoadAction action, UniProtLoadContext context) {
        Map<String, String> genBankMap = new HashMap<>();

        genBankMap.put("A8E587","ZDB-GENE-980526-285");
        genBankMap.put("A8KBH9","ZDB-GENE-041008-120");

        String accession = lostUniProt.getAccession();
        String gene = lostUniProt.getDataZdbID();

        if (genBankMap.containsKey(accession) && genBankMap.get(accession).equals(gene)) {
            action.setSubType(LOST_UNIPROT_PREV_MATCH_BY_GB);
            if(context.hasExistingUniprotWithNonLoadAttributions(accession, gene)) {
                action.setType(UniProtLoadAction.Type.INFO);
                action.setDetails("UniProt accession " + accession + " previously matched gene " + gene + " by GenBank. \nThis also has a non-load attribution.\n" + action.getDetails());
            } else {
                action.setType(UniProtLoadAction.Type.WARNING);
                action.setDetails("UniProt accession " + accession + " previously matched gene " + gene + " by GenBank.\n" + action.getDetails());
            }
        }

    }

}
