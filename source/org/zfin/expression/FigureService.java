package org.zfin.expression;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods for the Figure class
 */
public class FigureService {
    static Logger LOG = Logger.getLogger(FigureService.class);

    /**
     * Get a sorted list of genes for which expression is shown in this figure
     *
     * @param figure Figure
     * @return List of Markers
     */
    public static List<Marker> getExpressionGenes(Figure figure) {
        List<Marker> genes = new ArrayList<Marker>();
        for (ExpressionResult er : figure.getExpressionResults()) {
            ExpressionExperiment ee = er.getExpressionExperiment();
            Marker marker = ee.getMarker();

            if ((marker != null)
                    && (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM))
                    && !genes.contains(marker)) {
                genes.add(ee.getMarker());
            }
        }
        Collections.sort(genes);
        LOG.debug("found " + genes.size() + " genes for " + figure.getZdbID());
        return genes;
    }


}
