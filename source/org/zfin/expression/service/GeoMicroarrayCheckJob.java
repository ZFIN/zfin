package org.zfin.expression.service;

import org.springframework.stereotype.Service;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.marker.Marker;

/**
 * this method should be asyncronous and set the value on markerExpression
 */
@Service
public class GeoMicroarrayCheckJob implements Runnable {

    private MarkerExpression markerExpression;
    private Marker marker;

    // can be autowired once tests are in Spring again
//    @Autowired
    private ExpressionService expressionService;

    public GeoMicroarrayCheckJob(){ }

    @Override
    public void run() {
        markerExpression.setGeoLinkSearching(true);
        markerExpression.setGeoGeneSymbol(marker.getAbbreviation());
        markerExpression.setGeoLink(expressionService.getGeoLinkForMarkerIfExists(marker));
        markerExpression.setGeoLinkSearching(false);
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }
}
