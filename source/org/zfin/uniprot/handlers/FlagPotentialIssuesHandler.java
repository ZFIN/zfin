package org.zfin.uniprot.handlers;

import lombok.extern.log4j.Log4j2;
import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        log.warn("Found " + duplicates.size() + " duplicate accessions");
        duplicates.entrySet().forEach(entry -> {
                    log.warn("Duplicate accession: " + entry.getKey());
                    entry.getValue().forEach(action -> log.warn(action));
                });



        //look for duplicates: (accession + gene)
        duplicates = actions.stream().collect(Collectors.groupingBy(action -> action.getAccession() + "/" + action.getGeneZdbID()))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.warn("Found " + duplicates.size() + " duplicate accessions + gene");
        duplicates.entrySet().forEach(entry -> {
                    log.warn("Duplicate accession + gene: " + entry.getKey());
                    entry.getValue().forEach(action -> log.warn(action));
                });
    }

}
