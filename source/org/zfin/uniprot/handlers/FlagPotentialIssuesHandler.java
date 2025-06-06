package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.sequence.ForeignDB.AvailableName.ZFIN;

/**
 * This handler reports the accessions that we encounter that were previously in the legacy problem files.
 * Eventually, this class should become unnecessary.
 */
@Log4j2
public class FlagPotentialIssuesHandler implements UniProtLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        //look for duplicates: (accession only)
        Map<String, List<UniProtLoadAction>> duplicates = actions.stream().collect(Collectors.groupingBy(UniProtLoadAction::getAccession))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.warn("Found " + duplicates.size() + " duplicate accessions in actions. These may be legitimate actions, but they are logged here for review.");
        duplicates.entrySet().stream().map(
                kvp -> "Duplicate accession actions: " + kvp.getKey() + ": [" + kvp.getValue().stream().map(action -> action.getType() + " " + action.getSubType() + " " + action.getAccession() + " " + action.getGeneZdbID()).collect(Collectors.joining(",")) + "]"
        ).forEach(log::warn);

        //look for duplicates: (accession + gene)
        duplicates = actions.stream().collect(Collectors.groupingBy(action -> action.getAccession() + "/" + action.getGeneZdbID()))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.warn("Found " + duplicates.size() + " duplicate accessions + gene in actions.  These may be legitimate actions, but they are logged here for review.");
        duplicates.entrySet().forEach(entry -> {
                    log.warn("Duplicate accession + gene: " + entry.getKey());
                    entry.getValue().forEach(action -> log.warn(action.getType() + " " + action.getSubType() + " " + action.getAccession() + " " + action.getGeneZdbID()));
                });
        createInfoActionForGenesLosingAllAccessions(actions, context);
        createInfoActionForGenesGainingFirstAccessions(actions, context);
        tagActionsForGenesLosingAllAccessions(actions, context);
        tagActionsForNewGenes(actions, context);
    }

    /**
     * If a gene is gaining its first uniprot accession, tag the relevant actions with that info
     * @param actions (all actions)
     * @param context (existing context of DB)
     */
    private void tagActionsForNewGenes(Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        List<String> genesThatLostAllUniprots = actions.stream().filter(action -> action.getType().equals(UniProtLoadAction.Type.INFO))
                .filter(action -> action.getSubType().equals(UniProtLoadAction.SubType.GENE_GAINS_FIRST_UNIPROT))
                .map(a -> a.getGeneZdbID())
                .toList();

        actions.stream().filter(action -> action.getType().equals(UniProtLoadAction.Type.DELETE))
                .filter(action -> genesThatLostAllUniprots.contains(action.getGeneZdbID()))
                .forEach(action -> action.addTag(UniProtLoadAction.CategoryTag.NEW_GENE));
    }

    /**
     * If a gene is losing all of its uniprot accessions, tag the relevant actions with that info
     * @param actions (all actions)
     * @param context (existing context of DB)
     */
    private void tagActionsForGenesLosingAllAccessions(Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        List<String> genesThatLostAllUniprots = actions.stream().filter(action -> action.getType().equals(UniProtLoadAction.Type.INFO))
                .filter(action -> action.getSubType().equals(UniProtLoadAction.SubType.GENE_LOST_ALL_UNIPROTS))
                .map(a -> a.getGeneZdbID())
                .toList();

        actions.stream().filter(action -> action.getType().equals(UniProtLoadAction.Type.DELETE))
                .filter(action -> genesThatLostAllUniprots.contains(action.getGeneZdbID()))
                .forEach(action -> action.addTag(UniProtLoadAction.CategoryTag.LOST_ALL_UNIPROTS));
    }

    /**
     * Using the existing actions, figure out how many genes will lose *all* of their accessions.
     *
     * @param actions existing actions that were generated by other handlers
     * @param context existing state of database
     */
    private void createInfoActionForGenesLosingAllAccessions(Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        //get summary of number of genes with uniprot links and how many genes lose ALL uniprot links

        //start with the number of genes with uniprot links
        Map<String, List<DBLinkSlimDTO>> allGenesWithDBLinks = context.getUniprotDbLinksByGeneID();

        //If a gene has x number of uniprot links, and we are loading y new links, and deleting z links, then the gene will have x+y-z links
        //If x + y - z = 0, then the gene will have no uniprot links

        //get the number of uniprot links for each gene before the load (x
        Map<String, Integer> preExistingGeneToUniprotLinkCount = new HashMap<>();
        allGenesWithDBLinks.forEach((gene, dbLinks) -> preExistingGeneToUniprotLinkCount.put(gene, dbLinks.size()));

        //get the number of uniprot links for each gene that are being loaded (y)
        Map<String, Integer> newLoadGeneToUniprotLinkCount = new HashMap<>();
        actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.LOAD) &&
                a.getSubType().equals(UniProtLoadAction.SubType.MATCH_BY_REFSEQ)).forEach(a -> {
            String gene = a.getGeneZdbID();
            newLoadGeneToUniprotLinkCount.put(gene, newLoadGeneToUniprotLinkCount.getOrDefault(gene, 0) + 1);
        });

        //get the number of uniprot links for each gene that are being deleted (z)
        Map<String, Integer> deletedGeneToUniprotLinkCount = new HashMap<>();
        actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.DELETE) &&
                a.getSubType().equals(UniProtLoadAction.SubType.LOST_UNIPROT)).forEach(a -> {
            String gene = a.getGeneZdbID();
            deletedGeneToUniprotLinkCount.put(gene, deletedGeneToUniprotLinkCount.getOrDefault(gene, 0) + 1);
        });

        //calculate the number of uniprot links for each gene after the load
        Map<String, Integer> postExistingGeneToUniprotLinkCount = new HashMap<>();
        for (String gene : preExistingGeneToUniprotLinkCount.keySet()) {
            int preExistingCount = preExistingGeneToUniprotLinkCount.get(gene);
            int newLoadCount = newLoadGeneToUniprotLinkCount.getOrDefault(gene, 0);
            int deletedCount = deletedGeneToUniprotLinkCount.getOrDefault(gene, 0);
            int postExistingCount = preExistingCount + newLoadCount - deletedCount;
            postExistingGeneToUniprotLinkCount.put(gene, postExistingCount);
            if (postExistingCount == 0) {
                actions.add(UniProtLoadAction.builder()
                        .type(UniProtLoadAction.Type.INFO)
                        .subType(UniProtLoadAction.SubType.GENE_LOST_ALL_UNIPROTS)
                        .details("This gene has lost all UniProt accessions in this load (" + preExistingCount + " before load).  Lost uniprots: " + allGenesWithDBLinks.get(gene).stream().map(DBLinkSlimDTO::getAccession).collect(Collectors.joining(", ")))
                        .geneZdbID(gene)
                        .links(Set.of(UniProtLoadLink.create(ZFIN, gene)))
                        .build());
            }
        }
    }

    /**
     * Using the existing actions, figure out how many genes there are that previously had no accessions and will gain their first accession(s).
     * @param actions existing actions that were generated by other handlers
     * @param context existing state of database
     */
    private void createInfoActionForGenesGainingFirstAccessions(Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        //get summary of number of genes that previously had no uniprot links and now have at least one

        //start with the number of genes with uniprot links
        Map<String, List<DBLinkSlimDTO>> allGenesWithDBLinks = context.getUniprotDbLinksByGeneID();

        ArrayList<UniProtLoadAction> newActions = new ArrayList<>();
        //iterate over all new gene/uniprot link actions
        actions.stream().filter(a -> a.getType().equals(UniProtLoadAction.Type.LOAD) &&
                a.getSubType().equals(UniProtLoadAction.SubType.MATCH_BY_REFSEQ)).forEach(a -> {
            String gene = a.getGeneZdbID();
            if (allGenesWithDBLinks.get(gene) == null || allGenesWithDBLinks.get(gene).isEmpty()) {
                newActions.add(UniProtLoadAction.builder()
                        .type(UniProtLoadAction.Type.INFO)
                        .subType(UniProtLoadAction.SubType.GENE_GAINS_FIRST_UNIPROT)
                        .details("This gene has gained UniProt accession(s) in this load and previously had none.")
                        .geneZdbID(gene)
                        .links(Set.of(UniProtLoadLink.create(ZFIN, gene)))
                        .build());
            }
        });
        actions.addAll(newActions);
    }

}
