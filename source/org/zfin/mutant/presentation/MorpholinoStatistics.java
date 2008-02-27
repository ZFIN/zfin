package org.zfin.mutant.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeService;
import org.zfin.publication.Publication;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is a statistics class about Morpholinos on the Anatomy Detail page.
 */
public class MorpholinoStatistics {

    private GenotypeExperiment genoExperiment;
    private AnatomyItem anatomyItem;
    private Set<Figure> figures;
    private Set<Publication> publications;

    public MorpholinoStatistics(GenotypeExperiment genoExperiment, AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
        this.genoExperiment = genoExperiment;
    }

    public GenotypeExperiment getGenoExperiment() {
        return genoExperiment;
    }

    public Set<Marker> getMorpholinoMarkers() {
        Set<ExperimentCondition> experimentConditions = genoExperiment.getExperiment().getExperimentConditions();
        Set<Marker> morpholinoGenes = new HashSet<Marker>();
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

    public Map<String, Set> getPhenotypeDescriptions() {
        return PhenotypeService.getPhenotypesGroupedByOntology(genoExperiment, anatomyItem);
    }

    public int getNumberOfFigures() {
        if (figures == null) {
            for (Phenotype phenotype : genoExperiment.getPhenotypes()) {
                if (StringUtils.equals(phenotype.getPatoEntityAzdbID(), anatomyItem.getZdbID()) ||
                        StringUtils.equals(phenotype.getPatoEntityBzdbID(), anatomyItem.getZdbID())) {
                    if (figures == null)
                        figures = new HashSet<Figure>();
                    figures.addAll(phenotype.getFigures());
                }
            }
        }
        if (figures == null)
            return 0;
        return figures.size();
    }

    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.iterator().next();
    }

    public int getNumberOfPublications() {
        if (publications == null) {
            for (Phenotype phenotype : genoExperiment.getPhenotypes()) {
                if (StringUtils.equals(phenotype.getPatoEntityAzdbID(), anatomyItem.getZdbID()) ||
                        StringUtils.equals(phenotype.getPatoEntityBzdbID(), anatomyItem.getZdbID())) {
                    if (publications == null)
                        publications = new HashSet<Publication>();
                    publications.add(phenotype.getPublication());
                }
            }
        }
        if (publications == null)
            return 0;
        return publications.size();
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
