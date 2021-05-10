package org.zfin.infrastructure.delete;

import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class DeleteEFGRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteEFGRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        Marker efg = mr.getMarkerByID(zdbID);
        entity = efg;

        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        Set<Marker> constructs = MarkerService.getRelatedMarker(efg, types);
        addToValidationReport(efg.getAbbreviation() + " has relationships to the following constructs:", constructs);

        List<Marker> antibodyList = new ArrayList<>(efg.getFirstMarkerRelationships().size());
        for (MarkerRelationship rel : efg.getFirstMarkerRelationships()) {
            if (rel.getType().equals(MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY))
                antibodyList.add(rel.getSecondMarker());
        }

        addToValidationReport(efg.getAbbreviation() + " has relationships to the following antibodies:", antibodyList);

        int expressionFigureCount = RepositoryFactory.getExpressionRepository().getExpressionFigureCountForEfg(efg);
        // Can't delete an EFG if it is used in expression data
        if (expressionFigureCount > 0) {
            int expressionPublicationCount = RepositoryFactory.getExpressionRepository().getExpressionPubCountForEfg(efg);
            addToValidationReport("Used in expression data: " + expressionFigureCount + " figure(s) of " + expressionPublicationCount + " publication(s) (see " + efg.getAbbreviation() + " page for details)");
        }

        List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(efg.getZdbID());
        Collections.sort(publications);
        addToValidationReport(efg.getAbbreviation() + " is referenced in the following publications:", publications);
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
