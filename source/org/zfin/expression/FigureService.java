package org.zfin.expression;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.MarkerTypeGroup;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.mutant.Feature;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Phenotype;
import org.zfin.repository.RepositoryFactory;
import org.zfin.ontology.GoTerm;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Utility methods for the Figure class
 */
public class FigureService {
    static Logger LOG = Logger.getLogger(FigureService.class);

    /**
     * Get a sorted list of genes for which expression is shown in this figure
     * @param figure Figure
     * @return List of Markers
     */
    public static List<Marker> getExpressionGenes(Figure figure) {
        List<Marker> genes = new ArrayList<Marker>();
        for (ExpressionResult er : figure.getExpressionResults()) {
            ExpressionExperiment ee = er.getExpressionExperiment();
            Marker marker = ee.getMarker();

            if ((marker != null)
                    && ( marker.isInTypeGroup(Marker.TypeGroup.GENEDOM) )
                    && !genes.contains(marker) ) {
                genes.add(ee.getMarker());
            }
        }
        Collections.sort(genes);
        LOG.debug("found " + genes.size() + " genes for " + figure.getZdbID()); 
        return genes;
    }


}
