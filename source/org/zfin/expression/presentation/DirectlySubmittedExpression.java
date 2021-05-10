package org.zfin.expression.presentation;

import java.util.List;

public class DirectlySubmittedExpression {

	private List<PublicationExpressionBean> markerExpressionInstances;


	public int getFigureCount(){
        if(markerExpressionInstances ==null) return 0 ;
        int figureCount = 0 ;
        for(PublicationExpressionBean markerExpressionInstance : markerExpressionInstances){
            figureCount += markerExpressionInstance.getNumFigures() ;
        }
        return figureCount ;
    }

	public int getPublicationCount(){
        return ( markerExpressionInstances ==null ? 0 : markerExpressionInstances.size()) ;
	}

    public int getImageCount() {
        if(markerExpressionInstances ==null) return 0 ;
        int imageCount = 0 ;
        for(PublicationExpressionBean markerExpressionInstance : markerExpressionInstances){
            imageCount += markerExpressionInstance.getNumImages() ;
        }
        return imageCount ;
    }

    public int getTotalCount(){
        return getFigureCount() + getPublicationCount() + getImageCount() ;
    }

    public List<PublicationExpressionBean> getMarkerExpressionInstances() {
        return markerExpressionInstances;
    }

    public void setMarkerExpressionInstances(List<PublicationExpressionBean> markerExpressionInstances) {
        this.markerExpressionInstances = markerExpressionInstances;
    }
}
