package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.service.AntibodyWikiWebService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DeleteAntibodyRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteAntibodyRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        entity = marker;

        List<ExpressionExperiment2> expressionExperiment2s = RepositoryFactory.getExpressionRepository().getExperiment2sByAntibody((Antibody) marker);
        if (CollectionUtils.isNotEmpty(expressionExperiment2s)) {
            Set<ExpressionExperiment2> expressionExperiments = new HashSet<>();
            for (ExpressionExperiment2 expressionExperiment2 : expressionExperiment2s) {
                expressionExperiments.add(expressionExperiment2);
            }
            int numExpression = expressionExperiment2s.size();
            Set<Publication> pubs = new TreeSet<>();
            for (ExpressionExperiment2 expressionExperiment : expressionExperiment2s) {
                pubs.add(expressionExperiment.getPublication());
            }
            addToValidationReport(marker.getAbbreviation() + " is used in " + numExpression
                    + " expression records in the following " + pubs.size() + " publication(s)", pubs);

        }
        return validationReportList;
    }

    private Logger logger = Logger.getLogger(DeleteAntibodyRule.class);

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        try {
            if (ZfinProperties.isPushToWiki()) {
                AntibodyWikiWebService.getInstance().dropPageIndividually(entity.getAbbreviation());
            }
        } catch (Exception e) {
            logger.error("Failed to remove antibody: " + entity, e);
        }
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
