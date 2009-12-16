package org.zfin.expression.presentation;

import org.zfin.sequence.MarkerDBLink;

import java.util.ArrayList;
import java.util.List;

public class MarkerExpression {

    private MarkerExpressionInstance allMarkerExpressionInstance;
    private DirectlySubmittedExpression directlySubmittedExpression;
    private List<String> wildTypeStageExpression = new ArrayList<String>();
    private List<MarkerDBLink> microarrayLinks = new ArrayList<MarkerDBLink>();

//    private Logger logger = Logger.getLogger(MarkerExpression.class) ;

    public int getTotalCountForStuff() {
        return (directlySubmittedExpression == null ? 0 : directlySubmittedExpression.getExpressionSummaryInstances().size())
                + (allMarkerExpressionInstance == null ? 0 : allMarkerExpressionInstance.getPublicationCount())
                + (wildTypeStageExpression == null ? 0 : wildTypeStageExpression.size())
                + (wildTypeStageExpression == null ? 0 : microarrayLinks.size())
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

    public List<String> getWildTypeStageExpression() {
        return wildTypeStageExpression;
    }

    public void setWildTypeStageExpression(List<String> wildTypeStageExpression) {
        this.wildTypeStageExpression = wildTypeStageExpression;
    }

    public List<MarkerDBLink> getMicroarrayLinks() {
        return microarrayLinks;
    }

    public void setMicroarrayLinks(List<MarkerDBLink> microarrayLinks) {
        this.microarrayLinks = microarrayLinks;
    }
}
