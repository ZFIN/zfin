package org.zfin.sequence.blast.results.view;

import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.HashMap;

/**
 * This class maps genes to their expression for caching.
 */
public class GeneDataMap extends HashMap<Marker,ExpressionMapBean>{

//    private static Map geneMappedExpression = new HashMap<Marker,ExpressionMapBean>() ;

    private static GeneDataMap instance = null ;

    private GeneDataMap(){}

    public static GeneDataMap getInstance(){
        if(instance==null){
            instance = new GeneDataMap() ;
        }
        return instance ;
    }

    public HitViewBean calculateExpressionForGene(Marker gene,HitViewBean hitViewBean){
        ExpressionMapBean expressionMapBean = get(gene) ;


        if(expressionMapBean==null){
            expressionMapBean = createExpressionViewBean(gene) ;
            put(gene,expressionMapBean) ;
        }

        hitViewBean.setHasExpression(expressionMapBean.isHasExpression());
        hitViewBean.setHasExpressionImages(expressionMapBean.isHasExpressionImages());
        hitViewBean.setHasGO(expressionMapBean.isHasGO());
        hitViewBean.setHasPhenotype(expressionMapBean.isHasPhenotype());
        hitViewBean.setHasPhenotypeImages(expressionMapBean.isHasPhenotypeImages());

        return hitViewBean ;
    }

    public ExpressionMapBean createExpressionViewBean(Marker gene){
        ExpressionMapBean expressionMapBean = new ExpressionMapBean() ;

        boolean hasExpressionImages ;
        boolean hasPhenotypeImages ;
        boolean hasGO ;
        boolean hasExpression ;
        boolean hasPhenotype ;

        hasExpressionImages= RepositoryFactory.getMarkerRepository().getGeneHasExpressionImages(gene) ;
        hasPhenotypeImages = RepositoryFactory.getMarkerRepository().getGeneHasPhenotypeImage(gene) ;
        hasGO= RepositoryFactory.getMarkerRepository().getGeneHasGOEvidence(gene) ;
        hasExpression = hasExpressionImages ;
        hasPhenotype = hasPhenotypeImages ;

        if(hasExpressionImages==false){
            hasExpression = RepositoryFactory.getMarkerRepository().getGeneHasExpression(gene) ;
        }

        if(hasPhenotypeImages==false){
            hasPhenotype = RepositoryFactory.getMarkerRepository().getGeneHasPhenotype(gene) ;
        }

        expressionMapBean.setHasExpression(hasExpression);
        expressionMapBean.setHasExpressionImages(hasExpressionImages); ;
        expressionMapBean.setHasGO(hasGO);
        expressionMapBean.setHasPhenotype(hasPhenotype);
        expressionMapBean.setHasPhenotypeImages(hasPhenotypeImages);
        return expressionMapBean ;
    }
}
