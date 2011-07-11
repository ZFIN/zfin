package org.zfin.mutant.presentation;

import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.*;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
public class MorpholinoStatistics extends EntityStatistics {

    private GenotypeExperiment genoExperiment;
    private GenericTerm anatomyItem;
    private Set<Figure> figures;
    private String targetGeneOrder;

    public MorpholinoStatistics(GenotypeExperiment genoExperiment, GenericTerm anatomyItem) {
        this.anatomyItem = anatomyItem;
        this.genoExperiment = genoExperiment;
        targetGeneOrder = "";
        Set<Marker> targetGenes = getMorpholinoMarkers();
        int numberOfTargetGenes = targetGenes.size();
        int index = 1;
        for (Marker marker : targetGenes) {
            targetGeneOrder += marker.getAbbreviation();
            if (index != numberOfTargetGenes)
                targetGeneOrder += ", ";
            index++;
        }
    }

    public GenotypeExperiment getGenoExperiment() {
        return genoExperiment;
    }

    public Set<Marker> getMorpholinoMarkers() {
        Set<ExperimentCondition> experimentConditions = genoExperiment.getExperiment().getExperimentConditions();
        Set<Marker> morpholinoGenes = new TreeSet<Marker>(new Comparator<Marker>() {
            public int compare(Marker one, Marker two) {
                return (one.getAbbreviation().compareTo(two.getAbbreviation()));
            }
        });
        for (ExperimentCondition condition : experimentConditions) {
            Marker morpholino = condition.getMorpholino();
            if (morpholino != null) {
                Set<MarkerRelationship> markerRelationshipSet = morpholino.getFirstMarkerRelationships();
                for (MarkerRelationship markerRelation : markerRelationshipSet) {
                    morpholinoGenes.add(markerRelation.getSecondMarker());
                }
            }
        }
        return morpholinoGenes;
    }

    public Set<PhenotypeStatement> getPhenotypeStatements() {
        return PhenotypeService.getPhenotypeStatements(genoExperiment, anatomyItem);
    }

    @Override
    public int getNumberOfFigures() {
        if (figures == null) {
            figures = new HashSet<Figure>(5);
            for (PhenotypeExperiment phenotype : genoExperiment.getPhenotypeExperiments()) {
                for (PhenotypeStatement phenoStatement : phenotype.getPhenotypeStatements()) {
                    if (phenoStatement.hasAffectedTerm(anatomyItem))
                        figures.add(phenotype.getFigure());
                }
            }
        }
        return figures.size();
    }

    public boolean isImgInFigure() {
        if (isNoFigureOrFigLabel())   {
            return false;
        }
        boolean thereIsImg = false;
        for (Figure fig : figures) {
            if (!fig.isImgless()) {
                thereIsImg = true;
                break;
            }
        }
        return thereIsImg;
    }

    public boolean isNoFigureOrFigLabel() {
        if (figures == null || figures.isEmpty())   {
            return true;
        }
        boolean thereIsFigLabel = false;
        for (Figure fig : figures) {
            if (fig.getLabel() == null) {
                thereIsFigLabel = true;
                break;
            }
        }
        return thereIsFigLabel;
    }

    @Override
    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.iterator().next();
    }

    @Override
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        Set<Publication>pubs = new HashSet<Publication>(5);
        for (PhenotypeExperiment phenotype : genoExperiment.getPhenotypeExperiments()) {
            for (PhenotypeStatement phenoStatement : phenotype.getPhenotypeStatements()) {
                if (phenoStatement.hasAffectedTerm(anatomyItem))
                    pubs.add(phenotype.getFigure().getPublication());
            }
        }
        List<Publication> pubList = new ArrayList<Publication>(pubs);
        return new PaginationResult<Publication>(pubList);
    }

    public String getTargetGeneOrder() {
        return targetGeneOrder;
    }

    private class PhenotypeComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            if (o1 == null)
                return -1;
            if (o2 == null)
                return +1;
            if (!o1.startsWith("[") && o2.startsWith("["))
                return -1;
            if (!o2.startsWith("[") && o1.startsWith("["))
                return +1;
            return o1.compareTo(o2);
        }
    }
}
