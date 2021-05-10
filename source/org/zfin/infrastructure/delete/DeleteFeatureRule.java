package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.profile.FeatureSupplier;
import org.zfin.profile.Organization;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class DeleteFeatureRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteFeatureRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbID);
        addToValidationReport(feature.getAbbreviation() + " is used in the following list of genotypes: ", getMutantRepository().getGenotypesByFeature(feature));

        // Can't delete the feature if it has a source
        if (CollectionUtils.isNotEmpty(feature.getSuppliers())) {
            List<Organization> organizationList = new ArrayList<>(feature.getSuppliers().size());
            for (FeatureSupplier supplier : feature.getSuppliers()) {
                organizationList.add(supplier.getOrganization());
            }
            addToValidationReport(feature.getAbbreviation() + " is provided by the following suppliers: ", organizationList);
        }

        // Can't delete the feature if has accession #
        if (CollectionUtils.isNotEmpty(feature.getDbLinks())) {
            addToValidationReport(feature.getAbbreviation() + " has the following accession numbers associated: ", feature.getDbLinks());
        }

        // Can't delete the feature if it has more than 1 publications
        SortedSet<Publication> featurePublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForFeature(feature);
        if (CollectionUtils.isNotEmpty(featurePublications) && featurePublications.size() > 1) {
            addToValidationReport(feature.getAbbreviation() + " associated with more than one publication: ", featurePublications);
        }
        return validationReportList;

    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbID);
    }

    public void clearTrackingRecords(String zdbID, String deleteFeatureTracking) {
        if (deleteFeatureTracking != null && deleteFeatureTracking.equalsIgnoreCase("yes")) {
            int deletedTracking = RepositoryFactory.getFeatureRepository().deleteFeatureFromTracking(zdbID);
            logger.info("deleted record attrs: " + deletedTracking);
        }

    }

    @Override
    public Publication getPublication() {
        Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(zdbID);
        SortedSet<Publication> featurePublications = RepositoryFactory.getPublicationRepository().getAllPublicationsForFeature(feature);
        // FB case 11678, provide link back to pub.
        if (CollectionUtils.isNotEmpty(featurePublications) && featurePublications.size() == 1)
            return featurePublications.first();
        return null;
    }

    Logger logger = LogManager.getLogger(DeleteFeatureRule.class);
}
