package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.datfiles.DatFileWriter;
import org.zfin.uniprot.dto.DBLinkSlimDTO;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.uniprot.UniProtLoadAction.MatchTitle.LOST_UNIPROT_PREV_MATCH_BY_GB;
import static org.zfin.uniprot.UniProtLoadAction.MatchTitle.LOST_UNIPROT_PREV_MATCH_BY_GP;

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
        List<UniProtLoadAction> actionsMatchedOnRefSeq = actions.stream().filter(action -> action.getTitle().equals(UniProtLoadAction.MatchTitle.MATCH_BY_REFSEQ.getValue())).toList();

        log.debug("ReportLostUniProtsHandler - Count of actions: " + actions.size());
        log.debug("ReportLostUniProtsHandler - Filtered count of actions: " + actionsMatchedOnRefSeq.size());

        //all genes with existing uniprot associations
        List<DBLinkSlimDTO> sequencesForGenesWithExistingUniprotAssociations = context.getUniprotDbLinks()
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();


        //all genes that get matched based on RefSeqs in load file
        List<String> genesWithMatchesInLoad = actionsMatchedOnRefSeq.stream().map(UniProtLoadAction::getGeneZdbID).toList();

        //no duplicate printouts
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
        for(DBLinkSlimDTO lostUniProt: genesThatLostUniProts) {
            if (!lostUniProt.containsNonLoadPublication()) {
                filteredLostUniProts.add(lostUniProt);
            }
        }

        //create actions for lost UniProts
        for(DBLinkSlimDTO lostUniProt: filteredLostUniProts) {
            UniProtLoadAction action = new UniProtLoadAction();

            String sequenceDetails = "";
            String sequenceDatFileDetails = "Sequence details: \n=================\n";
            RichSequenceAdapter richSequence = uniProtRecords.get(lostUniProt.getAccession());

            if (richSequence != null) {
                sequenceDatFileDetails += DatFileWriter.sequenceToString(richSequence);

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
                    action.addLink(new UniProtLoadLink("ZFIN: " + gene, "https://zfin.org/" + gene + "#sequences"));
                }

                sequenceDetails = lostUniProt.getDataZdbID() + " (" + lostUniProt.getMarkerAbbreviation() + ") would lose its UniProt association with " + lostUniProt.getAccession() + ".\n";
                if (affectedGenes.size() > 1) {
                        sequenceDetails += "These genes currently have links to " + lostUniProt.getAccession() + " : " + String.join(", ", affectedGenes) + "\n";
                }
                sequenceDetails += "\n";
                sequenceDetails += sequenceDatFileDetails;

            } else {
                sequenceDetails += "No sequence details found for " + lostUniProt.getAccession();
            }

            action.setGeneZdbID(lostUniProt.getDataZdbID());
            action.setTitle(UniProtLoadAction.MatchTitle.LOST_UNIPROT.getValue());
            action.setType(UniProtLoadAction.Type.DELETE);
            action.setAccession(lostUniProt.getAccession());
            action.setDetails("This gene currently has a UniProt association, but when we run the latest\n" +
                    "UniProt release through our matching pipeline, we don't find a match.\n" +
                    "This UniProt link will be removed.\n\n" + sequenceDetails);

            action.addLink(new UniProtLoadLink("ZFIN: " + lostUniProt.getDataZdbID(), "https://zfin.org/" + lostUniProt.getDataZdbID() + "#sequences" ));
            action.addLink(new UniProtLoadLink("UniProt: " + lostUniProt.getAccession(), "https://www.uniprot.org/uniprot/" + lostUniProt.getAccession()));

            setActionTitleAndDetailsForGenPeptGenBank(lostUniProt, action);

            actions.add(action);
        }

    }

    /**
     * This is a special case where we have a UniProt association that is based on a GenPept or GenBank sequence (based on old load logic).
     * In the future, we may want to remove this because over time they should get manually attributed.
     *
     * @param lostUniProt
     * @param action
     */
    private void setActionTitleAndDetailsForGenPeptGenBank(DBLinkSlimDTO lostUniProt, UniProtLoadAction action) {
        Map<String, String> genBankMap = new HashMap<>();
        genBankMap.put("A0PGL6","ZDB-GENE-060818-12");
        genBankMap.put("A4GT83","ZDB-GENE-060131-1");
        genBankMap.put("A4IGB0","ZDB-GENE-041014-76");
        genBankMap.put("A4QN70","ZDB-GENE-060531-147");
        genBankMap.put("A4ZNR4","ZDB-GENE-071130-1");
        genBankMap.put("A4ZNR5","ZDB-GENE-081013-6");
        genBankMap.put("A7MC74","ZDB-GENE-071004-51");
        genBankMap.put("A8E587","ZDB-GENE-980526-285");
        genBankMap.put("A8KBH9","ZDB-GENE-041008-120");
        genBankMap.put("A8KBI4","ZDB-GENE-060929-816");
        genBankMap.put("A9UL45","ZDB-GENE-080206-2");
        genBankMap.put("D3XD84","ZDB-GENE-100402-1");
        genBankMap.put("D3XD87","ZDB-GENE-100402-2");
        genBankMap.put("I6LC08","ZDB-GENE-050609-7");
        genBankMap.put("I6LC25","ZDB-GENE-041118-19");
        genBankMap.put("I6LC28","ZDB-GENE-050609-5");
        genBankMap.put("I6LD70","ZDB-GENE-050610-25");
        genBankMap.put("P51028","ZDB-GENE-980526-332");
        genBankMap.put("Q1RLY7","ZDB-GENE-060421-7397");
        genBankMap.put("Q5YCZ2","ZDB-GENE-020225-2");
        genBankMap.put("Q60H65","ZDB-GENE-041118-15");
        genBankMap.put("Q60H66","ZDB-GENE-041118-14");
        genBankMap.put("Q6NTI0","ZDB-GENE-040426-1433");
        genBankMap.put("Q6P962","ZDB-GENE-040426-2720");
        genBankMap.put("Q7ZSY6","ZDB-GENE-030131-4174");
        genBankMap.put("S5TZ97","ZDB-GENE-041008-189");

        Map<String, String> genPeptMap = new HashMap<>();
        genPeptMap.put("A0A1L2F565","ZDB-GENE-180611-1");
        genPeptMap.put("A6P6V3","ZDB-GENE-070917-6");
        genPeptMap.put("A6P6V6","ZDB-GENE-070917-7");
        genPeptMap.put("A7MCR6","ZDB-GENE-071004-74");
        genPeptMap.put("A8E5C4","ZDB-GENE-060103-1");
        genPeptMap.put("A8E5D2","ZDB-GENE-071004-120");
        genPeptMap.put("A8E5F8","ZDB-GENE-040801-211");
        genPeptMap.put("A8KB23","ZDB-GENE-080215-16");
        genPeptMap.put("A8KB82","ZDB-GENE-080220-41");
        genPeptMap.put("A8KBB5","ZDB-GENE-080219-50");
        genPeptMap.put("A8WFV9","ZDB-GENE-080220-13");
        genPeptMap.put("A8WGC1","ZDB-GENE-080220-7");
        genPeptMap.put("B0R024","ZDB-GENE-030616-587");
        genPeptMap.put("B3DIC7","ZDB-GENE-060503-136");
        genPeptMap.put("Q05AK7","ZDB-GENE-061027-337");
        genPeptMap.put("Q08C32","ZDB-GENE-070209-1");
        genPeptMap.put("Q1LXQ2","ZDB-GENE-030131-7777");
        genPeptMap.put("Q2AB30","ZDB-GENE-070917-5");
        genPeptMap.put("Q2PRM1","ZDB-GENE-070806-89");
        genPeptMap.put("Q2YDR7","ZDB-GENE-020225-4");
        genPeptMap.put("Q502S6","ZDB-GENE-050522-264");
        genPeptMap.put("Q5ICW5","ZDB-GENE-050214-1");
        genPeptMap.put("Q5RGS7","ZDB-GENE-041014-353");
        genPeptMap.put("Q6NUW4","ZDB-GENE-040426-2446");
        genPeptMap.put("Q6P2V0","ZDB-GENE-040426-1844");
        genPeptMap.put("Q6PC28","ZDB-GENE-040426-1687");
        genPeptMap.put("Q6T937","ZDB-GENE-040625-1");
        genPeptMap.put("Q7SZX7","ZDB-GENE-131126-30");
        genPeptMap.put("Q7T3J0","ZDB-GENE-030707-2");
        genPeptMap.put("Q7ZT21","ZDB-GENE-980526-80");
        genPeptMap.put("Q7ZZ76","ZDB-GENE-020430-1");
        genPeptMap.put("Q8AW62","ZDB-GENE-030616-180");
        genPeptMap.put("Q8AW68","ZDB-GENE-020225-34");
        genPeptMap.put("Q9MIY0","ZDB-GENE-011205-12");
        genPeptMap.put("Q9MIY1","ZDB-GENE-011205-10");
        genPeptMap.put("Q9MIY3","ZDB-GENE-011205-9");
        genPeptMap.put("Q9MIY4","ZDB-GENE-011205-16");
        genPeptMap.put("Q9MIY6","ZDB-GENE-011205-19");
        genPeptMap.put("Q9MIY9","ZDB-GENE-011205-8");
        genPeptMap.put("Q9MIZ0","ZDB-GENE-011205-7");

        String accession = lostUniProt.getAccession();
        String gene = lostUniProt.getDataZdbID();

        if (genBankMap.containsKey(accession) && genBankMap.get(accession).equals(gene)) {
            action.setTitle(LOST_UNIPROT_PREV_MATCH_BY_GB.getValue());
            action.setDetails("UniProt accession " + accession + " previously matched gene " + gene + " by GenBank.\n" + action.getDetails());
            action.setType(UniProtLoadAction.Type.INFO);
        }

        if (genPeptMap.containsKey(accession) && genPeptMap.get(accession).equals(gene)) {
            action.setTitle(LOST_UNIPROT_PREV_MATCH_BY_GP.getValue());
            action.setDetails("UniProt accession " + accession + " previously matched gene " + gene + " by GenPept.\n" + action.getDetails());
            action.setType(UniProtLoadAction.Type.INFO);
        }

    }

}
