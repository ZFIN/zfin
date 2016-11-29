package org.zfin.infrastructure.delete;

import org.apache.log4j.Logger;
import org.zfin.publication.Publication;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class DeletePublicationRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeletePublicationRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Publication publication = getPublicationRepository().getPublication(zdbID);
        if (publication == null)
            throw new NullPointerException("No publication found: " + zdbID);

        entity = publication;
        return validationReportList;
    }

    private Logger logger = Logger.getLogger(DeletePublicationRule.class);

    @Override
    public void prepareDelete() {
        entity = getPublicationRepository().getPublication(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
