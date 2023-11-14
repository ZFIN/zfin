package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.zfin.sequence.InterProProtein;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.interpro.InterProProteinDTO;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * Creates actions for adding and deleting protein domain information
 * interpro_protein table for load/delete
 * domain.txt was the legacy load file
 * Uses InterProProteinDTO
 */
@Log4j2
public class InterproDomainHandler implements SecondaryLoadHandler {

    private final List<InterProProteinDTO> downloadedInterproDomainRecords;

    public InterproDomainHandler() {
        this.downloadedInterproDomainRecords = null;
    }

    public InterproDomainHandler(List<InterProProteinDTO> downloadedInterproDomainRecords) {
        this.downloadedInterproDomainRecords = downloadedInterproDomainRecords;
    }

    @Override
    public void createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        addAndRemoveFromInterproProteinTable(actions, context);

    }

    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN;
    }

    private void addAndRemoveFromInterproProteinTable(List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        //existing records
        List<InterProProteinDTO> existingProteinDomains = context.getExistingInterproDomainRecords();

        //new records from download file
        List<InterProProteinDTO> newProteinDomains = downloadedInterproDomainRecords;

        //create new result set that contains only records in the existing records and not in the new records
        List<InterProProteinDTO> toDeleteProteinDomains = ListUtils.subtract(existingProteinDomains, newProteinDomains);

        //create new result set that contains only records in the new records and not in the existing records
        List<InterProProteinDTO> toAddProteinDomains = ListUtils.subtract(newProteinDomains, existingProteinDomains);

        //create new actions for each of the above result sets
        for (InterProProteinDTO toDeleteProteinDomain : toDeleteProteinDomains) {
            actions.add(createDeleteAction(toDeleteProteinDomain));
        }
        for (InterProProteinDTO toAddProteinDomain : toAddProteinDomains) {
            actions.add(createAddAction(toAddProteinDomain));
        }
    }

    private SecondaryTermLoadAction createAddAction(InterProProteinDTO toAddProteinDomain) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.LOAD)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN)
                .relatedEntityID(toAddProteinDomain.accession())
                .relatedEntityFields(toAddProteinDomain.toMap())
                .handlerClass(this.getClass().getName())
                .build();
    }

    private SecondaryTermLoadAction createDeleteAction(InterProProteinDTO toDeleteProteinDomain) {
        return SecondaryTermLoadAction.builder()
                .type(SecondaryTermLoadAction.Type.DELETE)
                .subType(SecondaryTermLoadAction.SubType.PROTEIN_DOMAIN)
                .relatedEntityID(toDeleteProteinDomain.accession())
                .relatedEntityFields(toDeleteProteinDomain.toMap())
                .handlerClass(this.getClass().getName())
                .build();
    }


    @Override
    public void processActions(List<SecondaryTermLoadAction> actions) {
        for (SecondaryTermLoadAction action : actions) {
            InterProProteinDTO iprDTO = InterProProteinDTO.fromMap(action.getRelatedEntityFields());
            InterProProtein ipr = iprDTO.toInterProProtein();
            if (action.getType() == SecondaryTermLoadAction.Type.LOAD) {
                currentSession().save(ipr);
            } else if (action.getType() == SecondaryTermLoadAction.Type.DELETE) {
                currentSession().delete(ipr);
            }
        }
    }

}
