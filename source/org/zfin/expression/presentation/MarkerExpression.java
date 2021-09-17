package org.zfin.expression.presentation;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.marker.presentation.LinkDisplay;

import java.util.List;

@Setter
@Getter
public class MarkerExpression {

    private MarkerExpressionInstance allMarkerExpressionInstance;
    private DirectlySubmittedExpression directlySubmittedExpression;
    private DirectlySubmittedExpression onlyThisse;
//    private List<MarkerDBLink> microarrayLinks = new ArrayList<MarkerDBLink>();
    private WildTypeExpression wildTypeStageExpression;
    private String geoLink;
    private LinkDisplay expressionAtlasLink;
    private List<String> ensdargGenes;
    private int inSituFigCount;
    private String geoGeneSymbol ;
    private boolean geoLinkSearching = true ;

    private Logger logger = LogManager.getLogger(MarkerExpression.class) ;

    public int getTotalCountForStuff() {
        return (directlySubmittedExpression == null ? 0 : directlySubmittedExpression.getMarkerExpressionInstances().size())
                + (allMarkerExpressionInstance == null ? 0 : allMarkerExpressionInstance.getFigureCount())
                + (wildTypeStageExpression == null ? 0 : wildTypeStageExpression.getExpressedStructures().size())
                + (geoLink == null ? 0 : 1)
                ;
    }

    public int getExpressionPubCount() {
        return (directlySubmittedExpression == null ? 0 : directlySubmittedExpression.getPublicationCount())
                + (allMarkerExpressionInstance == null ? 0 : allMarkerExpressionInstance.getPublicationCount())
                ;
    }

    public int getExpressionFigureCount() {
        return (directlySubmittedExpression == null ? 0 : directlySubmittedExpression.getFigureCount())
                + (allMarkerExpressionInstance == null ? 0 : allMarkerExpressionInstance.getPublicationCount())
                ;
    }

    public int getExpressionImageCount() {
        return (directlySubmittedExpression == null ? 0 : directlySubmittedExpression.getImageCount())
                + (allMarkerExpressionInstance == null ? 0 : allMarkerExpressionInstance.getPublicationCount())
                ;
    }

//    public List<MarkerDBLink> getMicroarrayLinks() {
//        return microarrayLinks;
//    }
//
//    public void setMicroarrayLinks(List<MarkerDBLink> microarrayLinks) {
//        this.microarrayLinks = microarrayLinks;
//    }

    public boolean isGeoLinkSearching() {
        return geoLinkSearching;
    }

    public void setGeoLinkSearching(boolean geoLinkSearching) {
        this.geoLinkSearching = geoLinkSearching;
    }
}
