package org.zfin.expression.presentation;

import java.util.List;

public class DirectlySubmittedExpression {

	private List<MarkerExpressionInstance> markerExpressionInstances;


	public int getFigureCount(){
        if(markerExpressionInstances ==null) return 0 ;
        int figureCount = 0 ;
        for(MarkerExpressionInstance markerExpressionInstance : markerExpressionInstances){
            figureCount += markerExpressionInstance.getFigureCount() ;
        }
        return figureCount ;
    }

	public int getPublicationCount(){
        return ( markerExpressionInstances ==null ? 0 : markerExpressionInstances.size()) ;
	}

    public int getImageCount() {
        if(markerExpressionInstances ==null) return 0 ;
        int imageCount = 0 ;
        for(MarkerExpressionInstance markerExpressionInstance : markerExpressionInstances){
            imageCount += markerExpressionInstance.getImageCount() ;
        }
        return imageCount ;
    }

    public int getTotalCount(){
        return getFigureCount() + getPublicationCount() + getImageCount() ;
    }

    public List<MarkerExpressionInstance> getExpressionSummaryInstances() {
        return markerExpressionInstances;
    }

    public void setExpressionSummaryInstances(List<MarkerExpressionInstance> markerExpressionInstances) {
        this.markerExpressionInstances = markerExpressionInstances;
    }
}
