package org.zfin.expression.presentation;

import org.apache.log4j.Logger;

public class MarkerExpression {

    private MarkerExpressionInstance allMarkerExpressionInstance;
    private DirectlySubmittedExpression directlySubmittedExpression;
//    private List<MarkerDBLink> microarrayLinks = new ArrayList<MarkerDBLink>();
    private WildTypeExpression wildTypeStageExpression;
    private String geoLink;
    private String geoGeneSymbol ;
    private boolean geoLinkSearching = true ;

    private Logger logger = Logger.getLogger(MarkerExpression.class) ;

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

    public DirectlySubmittedExpression getDirectlySubmittedExpression() {
        return directlySubmittedExpression;
    }

    public void setDirectlySubmittedExpression(DirectlySubmittedExpression directlySubmittedExpression) {
        this.directlySubmittedExpression = directlySubmittedExpression;
    }

    public MarkerExpressionInstance getAllExpressionData() {
        return allMarkerExpressionInstance;
    }

    public void setAllExpressionData(MarkerExpressionInstance allMarkerExpressionInstance) {
        this.allMarkerExpressionInstance = allMarkerExpressionInstance;
    }

//    public List<MarkerDBLink> getMicroarrayLinks() {
//        return microarrayLinks;
//    }
//
//    public void setMicroarrayLinks(List<MarkerDBLink> microarrayLinks) {
//        this.microarrayLinks = microarrayLinks;
//    }

    public WildTypeExpression getWildTypeStageExpression() {
        return wildTypeStageExpression;
    }

    public void setWildTypeStageExpression(WildTypeExpression wildTypeStageExpression) {
        this.wildTypeStageExpression = wildTypeStageExpression;
    }

    public String getGeoLink() {
        return geoLink;
    }

    public void setGeoLink(String geoLink) {
        this.geoLink = geoLink;
    }

    public String getGeoGeneSymbol() {
        return geoGeneSymbol;
    }

    public void setGeoGeneSymbol(String geoGeneSymbol) {
        this.geoGeneSymbol = geoGeneSymbol;
    }

    public boolean isGeoLinkSearching() {
        return geoLinkSearching;
    }

    public void setGeoLinkSearching(boolean geoLinkSearching) {
        this.geoLinkSearching = geoLinkSearching;
    }
}
