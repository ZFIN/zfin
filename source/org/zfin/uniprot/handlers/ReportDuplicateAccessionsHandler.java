package org.zfin.uniprot.handlers;

import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportDuplicateAccessionsHandler implements UniProtLoadHandler {
    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        //group actions by accession
        Map<String, Set<UniProtLoadAction>> actionsByAccession = actions.stream()
                .collect(java.util.stream.Collectors.groupingBy(UniProtLoadAction::getAccession, java.util.stream.Collectors.toSet()));

        //filter out accessions with only one action
        actionsByAccession.entrySet().removeIf(entry -> entry.getValue().size() < 2);

        //report
        actionsByAccession.forEach((accession, actionsForAccession) -> {
            actions.add(
                    UniProtLoadAction.builder()
                            .type(UniProtLoadAction.Type.DUPES)
                            .subType(UniProtLoadAction.SubType.DUPLICATE_ACCESSIONS)
                            .accession(accession)
                            .details("Duplicate accessions: " + actionsForAccession
                                    .stream()
                                    .map(a -> a.getSubType().getValue())
                                    .collect(Collectors.joining(", ")))
                            .build()
            );
        });
    }
}
