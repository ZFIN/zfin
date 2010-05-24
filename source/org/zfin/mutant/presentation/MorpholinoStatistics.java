package org.zfin.mutant.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeService;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.*;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
public class MorpholinoStatistics extends EntityStatistics {

    private GenotypeExperiment genoExperiment;
    private Term anatomyItem;
    private Set<Figure> figures;
    private String targetGeneOrder;

    public MorpholinoStatistics(GenotypeExperiment genoExperiment, Term anatomyItem) {
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

    public Map<String, Set<String>> getPhenotypeDescriptions() {
        return PhenotypeService.getPhenotypesGroupedByOntology(genoExperiment, anatomyItem);
    }

    @Override
    public int getNumberOfFigures() {
        if (figures == null) {
            for (Phenotype phenotype : genoExperiment.getPhenotypes()) {
                Term subTerm = phenotype.getSubterm();
                if ((subTerm != null && StringUtils.equals(subTerm.getID(), anatomyItem.getID())) ||
                        StringUtils.equals(phenotype.getSuperterm().getID(), anatomyItem.getID()) &&
                                !phenotype.getTag().equals(Phenotype.Tag.NORMAL.toString())) {
                    if (figures == null)
                        figures = new HashSet<Figure>(5);
                    figures.addAll(phenotype.getFigures());
                }
            }
        }
        if (figures == null)
            return 0;
        return figures.size();
    }

    @Override
    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.iterator().next();
    }

    @Override
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        Set<Publication> pubs = new HashSet<Publication>(5);
        for (Phenotype phenotype : genoExperiment.getPhenotypes()) {
            Term subterm = phenotype.getSubterm();
            if ( (subterm != null &&StringUtils.equals(subterm.getID(), anatomyItem.getID())) ||
                    StringUtils.equals(phenotype.getSuperterm().getID(), anatomyItem.getID()) &&
                            !phenotype.getTag().equals(Phenotype.Tag.NORMAL.toString())) {
                pubs.add(phenotype.getPublication());
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
