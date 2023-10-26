package org.zfin.infrastructure.delete;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DeleteConstructRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteConstructRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Marker construct = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        if (construct == null)
            throw new NullPointerException("No Construct found: " + zdbID);

        checkForDataAliasRelationships(construct);
        checkForFeatureRelationships(construct);
        entity = construct;
        return validationReportList;
    }

    private void checkForDataAliasRelationships(Marker construct) {
        Set<MarkerAlias> aliases = construct.getAliases();
        if (aliases != null && aliases.size() > 0) {
            addToValidationReport("Construct has data alias history that will be lost", aliases);

            List<Publication> relevantPublications = aliases.stream().map(DataAlias::getSinglePublication).toList();
            if (relevantPublications.size() > 0) {
                addToValidationReport("See publications: ", relevantPublications);
            }
        }
    }

    private void checkForFeatureRelationships(Marker construct) {
        List<Feature> featureList = RepositoryFactory.getFeatureRepository().getFeaturesByConstruct(construct);
        featureList.sort(Comparator.comparing(Feature::getAbbreviationOrder));
        addToValidationReport("Construct has a relationship to the following list of features", featureList);
    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
