package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.SortedSet;

public class DeleteJournalRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteJournalRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Journal journal = RepositoryFactory.getPublicationRepository().getJournalByID(zdbID);
        entity = journal;
        SortedSet<Publication> publications = RepositoryFactory.getPublicationRepository().getPublicationForJournal(journal);
        // Can't delete the journal if there is any publication associated with it
        if (CollectionUtils.isNotEmpty(publications)) {
            addToValidationReport(journal.getAbbreviation() + " associated with the following publications: ", publications);
        }
        return validationReportList;

    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getPublicationRepository().getJournalByID(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
