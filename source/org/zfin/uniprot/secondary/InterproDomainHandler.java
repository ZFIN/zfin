package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.EntryListItemDTO;

import java.util.*;

/**
 * Creates actions for adding and deleting protein domain information
 */
@Log4j2
public class InterproDomainHandler implements SecondaryLoadHandler {

    private final List<EntryListItemDTO> downloadedInterproDomainRecords;

    public InterproDomainHandler(List<EntryListItemDTO> downloadedInterproDomainRecords) {
        this.downloadedInterproDomainRecords = downloadedInterproDomainRecords;
    }

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        addAndRemoveFromInterproProteinTable(actions, context);

    }

    private void addAndRemoveFromInterproProteinTable(List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        //existing records
        List<EntryListItemDTO> existingProteinDomains = context.getExistingInterproDomainRecords();

        //new records from download file
        List<EntryListItemDTO> newProteinDomains = downloadedInterproDomainRecords;

        //create new result set that contains only records in the existing records and not in the new records
        List<EntryListItemDTO> toDeleteProteinDomains = ListUtils.subtract(existingProteinDomains, newProteinDomains);

        //create new result set that contains only records in the new records and not in the existing records
        List<EntryListItemDTO> toAddProteinDomains = ListUtils.subtract(newProteinDomains, existingProteinDomains);

        //create new actions for each of the above result sets
        for (EntryListItemDTO toDeleteProteinDomain : toDeleteProteinDomains) {
            actions.add(createDeleteAction(toDeleteProteinDomain));
        }
        for (EntryListItemDTO toAddProteinDomain : toAddProteinDomains) {
            actions.add(createAddAction(toAddProteinDomain));
        }
    }

    private SecondaryTermLoadAction createDeleteAction(EntryListItemDTO toDeleteProteinDomain) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN)
                .relatedEntityID(toDeleteProteinDomain.accession())
                .relatedEntityFields(toDeleteProteinDomain.toMap())
                .build();
    }

    private SecondaryTermLoadAction createAddAction(EntryListItemDTO toAddProteinDomain) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN)
                .relatedEntityID(toAddProteinDomain.accession())
                .relatedEntityFields(toAddProteinDomain.toMap())
                .build();
    }

}
