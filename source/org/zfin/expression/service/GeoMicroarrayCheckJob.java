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

//    public static Set<String> convertDBLinks(Collection<DBLink> dbLinks) {
//        // since it is a gene, we have to construct this with the abbreviation AND the RNA dblinks
//        Set<String> accession = new HashSet<String>();
//        for (DBLink dbLink : dbLinks) {
//            if (dbLink.getReferenceDatabase().getForeignDBDataType().getDataType()
//                    == ForeignDBDataType.DataType.RNA) {
//                accession.add(dbLink.getAccessionNumber());
//            }
//        }
//        return accession;
//    }

    public GeoMicroarrayCheckJob(){ }

    @Override
    public void run() {
        markerExpression.setGeoLinkSearching(true);
        markerExpression.setGeoGeneSymbol(marker.getAbbreviation());
        markerExpression.setGeoLink(expressionService.getGeoLinkForMarker(marker));
        markerExpression.setGeoLinkSearching(false);
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }
}
