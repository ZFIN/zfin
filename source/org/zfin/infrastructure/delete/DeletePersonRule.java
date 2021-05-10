package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class DeletePersonRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeletePersonRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Person person = RepositoryFactory.getProfileRepository().getPerson(zdbID);
        entity = person;

        // Can't delete the person if associated with a lab
        if (CollectionUtils.isNotEmpty(person.getLabs())) {
            addToValidationReport(person.getFullName() + " is associated to the following labs: ", person.getLabs());
        }
        if (CollectionUtils.isNotEmpty(person.getCompanies())) {
            addToValidationReport(person.getFullName() + " is associated to the following companies: ", person.getCompanies());
        }
        Set<Publication> publications = person.getPublications();
        // Can't delete the person if associated with a publication
        if (CollectionUtils.isNotEmpty(publications)) {
            SortedSet<Publication> sortedPubs = new TreeSet<>(publications);
            addToValidationReport(person.getFullName() + " is associated with the following publications: ", sortedPubs);
        }
        return validationReportList;

    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getProfileRepository().getPerson(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
