package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.uniprot.datfiles.UniprotReleaseRecords;
import org.zfin.uniprot.dto.InterProProteinDTO;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates actions for adding and deleting protein domain information
 * interpro_protein table for load/delete
 * domain.txt was the legacy load file
 * Uses InterProProteinDTO
 */
@Log4j2
public class InterproDomainActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN;
    }

    private final List<InterProProteinDTO> downloadedInterproDomainRecords;

    public InterproDomainActionCreator(List<InterProProteinDTO> downloadedInterproDomainRecords) {
        this.downloadedInterproDomainRecords = downloadedInterproDomainRecords;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(UniprotReleaseRecords uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        return addAndRemoveFromInterproProteinTable(context);
    }

    private List<SecondaryTermLoadAction> addAndRemoveFromInterproProteinTable(SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> newActions = new ArrayList<>();

        //existing records
        List<InterProProteinDTO> existingProteinDomains = (context.getExistingInterproDomainRecords() != null) ? context.getExistingInterproDomainRecords() : new ArrayList<>();

        //new records from download file
        List<InterProProteinDTO> newProteinDomains = (downloadedInterproDomainRecords != null) ? downloadedInterproDomainRecords : new ArrayList<>();

        //create new result set that contains only records in the existing records and not in the new records
        List<InterProProteinDTO> toDeleteProteinDomains = ListUtils.subtract(existingProteinDomains, newProteinDomains);

        //create new result set that contains only records in the new records and not in the existing records
        List<InterProProteinDTO> toAddProteinDomains = ListUtils.subtract(newProteinDomains, existingProteinDomains);

        //create new actions for each of the above result sets
        for (InterProProteinDTO toDeleteProteinDomain : toDeleteProteinDomains) {
            newActions.add(createDeleteAction(toDeleteProteinDomain));
        }
        for (InterProProteinDTO toAddProteinDomain : toAddProteinDomains) {
            newActions.add(createAddAction(toAddProteinDomain));
        }
        return newActions;
    }

    private SecondaryTermLoadAction createAddAction(InterProProteinDTO toAddProteinDomain) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN)
                .relatedEntityID(toAddProteinDomain.accession())
                .relatedEntityFields(toAddProteinDomain.toMap())
                .build();
    }

    private SecondaryTermLoadAction createDeleteAction(InterProProteinDTO toDeleteProteinDomain) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN)
                .relatedEntityID(toDeleteProteinDomain.accession())
                .relatedEntityFields(toDeleteProteinDomain.toMap())
                .build();
    }

}
