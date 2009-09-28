package org.zfin.expression;

import org.apache.log4j.Logger;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.expression.presentation.MarkerExpressionInstance;
import org.zfin.expression.presentation.DirectlySubmittedExpression;
import org.zfin.marker.*;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.orthology.Species;
import org.zfin.publication.Publication;
import org.zfin.framework.HibernateUtil;
import org.hibernate.Session;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/**
 * Sevice Class that deals with Marker related logic.
 */
public class ExpressionService {

    static Logger logger = Logger.getLogger(ExpressionService.class);


    public static Set<String> getThissePublicationZdbIDs(){
        Set<String> pubZdbIDS = new HashSet<String>() ;
        pubZdbIDS.add("ZDB-PUB-051025-1") ;
        pubZdbIDS.add("ZDB-PUB-040907-1") ;
        pubZdbIDS.add("ZDB-PUB-010810-1") ;
        return pubZdbIDS ;
    }

    public static boolean isThisseProbe(Clone clone ){
        // get all expression experiments
        Set<ExpressionExperiment> expressionExperiments = clone.getExpressionExperiments() ;
        InfrastructureRepository infrastructureRepository =  RepositoryFactory.getInfrastructureRepository() ;
        Set<String> thissePubblications = getThissePublicationZdbIDs() ;

        for(ExpressionExperiment expressionExperiment: expressionExperiments){
            // is there a record attribution for this expression experement
            if(infrastructureRepository.getRecordAttribution(expressionExperiment.getZdbID(),expressionExperiment.getPublication().getZdbID(),
                    RecordAttribution.SourceType.STANDARD)!=null
                    &&
                    thissePubblications.contains(expressionExperiment.getPublication().getZdbID())
                    ){
                return true;
            }
        }
        return false ;
    }

    public static DirectlySubmittedExpression getDirectlySubmittedExpressionSummaries(Marker marker){
        Session session = HibernateUtil.currentSession() ;
        ExpressionRepository expressionRepository = RepositoryFactory.getExpressionSummaryRepository() ;
        List pubList = RepositoryFactory.getExpressionSummaryRepository().getDirectlySubmittedExpressionSummaries(marker) ;
        List<MarkerExpressionInstance> markerExpressionInstances = new ArrayList<MarkerExpressionInstance>() ;

        for(int i = 0 ; i < pubList.size() ; i++){
            MarkerExpressionInstance markerExpressionInstance = new MarkerExpressionInstance() ;
            Object[] objectArray = (Object[]) pubList.get(i) ;
            markerExpressionInstance.setFigureCount(Integer.valueOf(objectArray[0].toString()));
            Publication publication = (Publication) session.get(Publication.class,objectArray[1].toString()) ;
            markerExpressionInstance.setSinglePublication(publication);
            Clone clone = (Clone) session.get(Marker.class,objectArray[2].toString()) ;
            markerExpressionInstance.setMarker(clone);
            markerExpressionInstance.setImageCount(expressionRepository.getImagesFromPubAndClone(publication,clone)) ;

            markerExpressionInstances.add(markerExpressionInstance);
        }

        DirectlySubmittedExpression directlySubmittedExpression = new DirectlySubmittedExpression();
        directlySubmittedExpression.setExpressionSummaryInstances(markerExpressionInstances);

        return directlySubmittedExpression;
    }

    public static MarkerExpression getExpressionForMarker(Marker marker){
        MarkerExpression markerExpression = new MarkerExpression() ;

        // all expression

        if(marker.getMarkerType().getType().equals(Marker.Type.GENE)){
            MarkerExpressionInstance allMarkerExpressionInstance = new MarkerExpressionInstance() ;
            allMarkerExpressionInstance.setPublicationCount(RepositoryFactory.getExpressionSummaryRepository().getExpressionPubCount(
                    marker)) ;
            allMarkerExpressionInstance.setFigureCount(RepositoryFactory.getExpressionSummaryRepository().getExpressionFigureCount(
                    marker)) ;
            markerExpression.setAllExpressionData(allMarkerExpressionInstance) ;
        }

        // directly submitted
        logger.info("setting directly subbmitted expression");
        markerExpression.setDirectlySubmittedExpression(getDirectlySubmittedExpressionSummaries(marker));
        logger.info("got directly subbmitted expression");


        // wildtype stages
        // todo: when we handle genes, we need to get this
        if(marker.getMarkerType().getType().equals(Marker.Type.GENE)){
            List<String> wildTypeExpressionData = new ArrayList<String>() ;
            markerExpression.setWildTypeStageExpression(wildTypeExpressionData);
            logger.fatal("have not imlemented ExpressionService wild type processing");
        }


        // microarray expression
        SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository() ;
        List<MarkerDBLink> microarrayLinks = new ArrayList<MarkerDBLink>() ;

        ReferenceDatabase geoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GEO,
                ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
        ReferenceDatabase zfEspressoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ZF_ESPRESSO,
                ForeignDBDataType.DataType.OTHER,ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
        
        microarrayLinks.addAll(sequenceRepository.getDBLinksForMarker(marker,geoDatabase)) ;
        microarrayLinks.addAll(sequenceRepository.getDBLinksForMarker(marker,zfEspressoDatabase)) ;

        markerExpression.setMicroarrayLinks(microarrayLinks);



        return markerExpression;
    }

}