package org.zfin.infrastructure.delete;

import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeleteRegionRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteRegionRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Marker region = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        entity = region;

        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_REGION);
        Set<Marker> constructs = MarkerService.getRelatedMarker(region, types);
        addToValidationReport(region.getAbbreviation() + " has relationships to the following constructs:", constructs);

        List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(region.getZdbID());
        Collections.sort(publications);
        addToValidationReport(region.getAbbreviation() + " is referenced in the following publications:", publications);
        return validationReportList;

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
