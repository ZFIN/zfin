package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.feature.Feature;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class DeleteSTRRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteSTRRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        SequenceTargetingReagent sequenceTargetingReagent = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(zdbID);
        entity = sequenceTargetingReagent;

        Set<Feature> featuresCreatedBySTR = RepositoryFactory.getFeatureRepository().getFeaturesCreatedBySequenceTargetingReagent(sequenceTargetingReagent);
        // Can't delete if used as a mutagen for a feature
        if (CollectionUtils.isNotEmpty(featuresCreatedBySTR)) {
            addToValidationReport(entity.getAbbreviation() + " is used in the following features: ", featuresCreatedBySTR);
        }

        List<Fish> fishList = RepositoryFactory.getMutantRepository().getFishListBySequenceTargetingReagent(sequenceTargetingReagent);
        // Can't delete if used in an environment
        if (CollectionUtils.isNotEmpty(fishList)) {
            addToValidationReport(entity.getAbbreviation() + " is used in the following fish: ", fishList);
        }
        List<String> publicationListGO = RepositoryFactory.getPublicationRepository().getPublicationIDsForGOwithField(zdbID);
        SortedSet<Publication> sortedGOpubs = new TreeSet<>();
        for (String pubId : publicationListGO) {
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(pubId);
            sortedGOpubs.add(publication);
        }
        // Can't delete if used in a GO annotation "inferred from" field
        if (CollectionUtils.isNotEmpty(sortedGOpubs)) {
            addToValidationReport(entity.getAbbreviation() + " is used in  \"inferred from\" field of GO annotation in the following publications: ", sortedGOpubs);
        }
        /*
        List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(sequenceTargetingReagent.getZdbID());
        SortedSet<Publication> sortedPublications = new TreeSet<>();
        sortedPublications.addAll(publications);
        addToValidationReport(entity.getAbbreviation() + " is associated with the following publications: ", sortedPublications);   */
        return validationReportList;
    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
