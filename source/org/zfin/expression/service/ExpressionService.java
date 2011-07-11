package org.zfin.expression.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.datatransfer.microarray.MicroarrayWebserviceJob;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.expression.repository.StageExpressionPresentation;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;

/**
 * Service Class that deals with Marker related logic.
 */
@Service
public class ExpressionService {

    public static final String MICROARRAY_PUB = "ZDB-PUB-071218-1";

    private static Logger logger = Logger.getLogger(ExpressionService.class);

    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
//    private static ReferenceDatabase geoDatabase ;
//    private static ReferenceDatabase zfEspressoDatabase ;

    public ExpressionService() {
//    static{
//        geoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GEO,
//                ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
//        ReferenceDatabase zfEspressoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.ZF_ESPRESSO,
//                ForeignDBDataType.DataType.OTHER, ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);
    }

    public Set<String> getThissePublicationZdbIDs() {
        Set<String> pubZdbIDS = new HashSet<String>(5);
        pubZdbIDS.add("ZDB-PUB-051025-1");
        pubZdbIDS.add("ZDB-PUB-040907-1");
        pubZdbIDS.add("ZDB-PUB-010810-1");
        return pubZdbIDS;
    }

    public boolean isThisseProbe(Clone clone) {
        // get all expression experiments
        Set<ExpressionExperiment> expressionExperiments = clone.getExpressionExperiments();
        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        Set<String> thissePublications = getThissePublicationZdbIDs();

        for (ExpressionExperiment expressionExperiment : expressionExperiments) {
            // is there a record attribution for this expression experiment
            if (infrastructureRepository.getRecordAttribution(expressionExperiment.getZdbID(), expressionExperiment.getPublication().getZdbID(),
                    RecordAttribution.SourceType.STANDARD) != null
                    &&
                    thissePublications.contains(expressionExperiment.getPublication().getZdbID())
                    ) {
                return true;
            }
        }
        return false;
    }

    public DirectlySubmittedExpression getDirectlySubmittedExpressionGene(Marker marker) {
        // this is almost always 1
        List<PublicationExpressionBean> pubList = expressionRepository.getDirectlySubmittedExpressionForGene(marker);

        // this is almost always 1, so not too expensive
        for (PublicationExpressionBean publicationExpressionBean : pubList) {
            publicationExpressionBean.setNumImages(expressionRepository.getImagesFromPubAndClone(publicationExpressionBean));
        }

        DirectlySubmittedExpression directlySubmittedExpression = new DirectlySubmittedExpression();
        directlySubmittedExpression.setMarkerExpressionInstances(pubList);

        return directlySubmittedExpression;
    }

    public DirectlySubmittedExpression getDirectlySubmittedExpressionClone(Clone clone) {
        // this is almost always 1
        List<PublicationExpressionBean> pubList = expressionRepository.getDirectlySubmittedExpressionForClone(clone);

        // this is almost always 1, so not too expensive
        for (PublicationExpressionBean publicationExpressionBean : pubList) {
            publicationExpressionBean.setNumImages(expressionRepository.getImagesFromPubAndClone(publicationExpressionBean));
        }

        DirectlySubmittedExpression directlySubmittedExpression = new DirectlySubmittedExpression();
        directlySubmittedExpression.setMarkerExpressionInstances(pubList);

        return directlySubmittedExpression;
    }

    public int updateGeoLinkForMarker(Marker marker) {
        Collection<String> accessions;
        boolean hasGeoLink, shouldHaveGeoLink;
        if (marker.getZdbID().startsWith("ZDB-GENE")) {
            hasGeoLink = infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers(marker.getZdbID()
                    , MicroarrayWebserviceJob.MICROARRAY_PUB);
            accessions = sequenceRepository.getDBLinkAccessionsForEncodedMarkers(marker, ForeignDBDataType.DataType.RNA);
            accessions.addAll(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
            shouldHaveGeoLink = NCBIEfetch.hasMicroarrayData(accessions, marker.getAbbreviation());
        } else {
            hasGeoLink = infrastructureRepository.hasStandardPublicationAttribution(marker.getZdbID()
                    , MicroarrayWebserviceJob.MICROARRAY_PUB);
            accessions = sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA);
            shouldHaveGeoLink = NCBIEfetch.hasMicroarrayData(accessions);
        }
        if (hasGeoLink != shouldHaveGeoLink) {
            if (hasGeoLink) {
                infrastructureRepository.removeRecordAttributionForData(marker.getZdbID(),
                        MicroarrayWebserviceJob.MICROARRAY_PUB
                );
                return -1;
            } else {
                PublicationAttribution publicationAttribution = infrastructureRepository.insertPublicAttribution(marker.getZdbID()
                        , MicroarrayWebserviceJob.MICROARRAY_PUB);
                if (publicationAttribution != null) {
                    logger.debug(publicationAttribution);
                    return 1;
                }
                else {
                   logger.error("failed to add publication attribution for data["+publicationAttribution.getDataZdbID()
                           +"] source["+publicationAttribution.getSourceZdbID()+"]");
                }
            }
        }
        return 0;
    }

    public String getGeoLinkForMarker(Marker marker) {
        Collection<String> accessions;

        if (marker.getZdbID().startsWith("ZDB-GENE")) {
            if (false == infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers(marker.getZdbID(), MicroarrayWebserviceJob.MICROARRAY_PUB
            )) {
                return null;
            }
            accessions = sequenceRepository.getDBLinkAccessionsForEncodedMarkers(marker, ForeignDBDataType.DataType.RNA);
            accessions.addAll(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
            return NCBIEfetch.getMicroarrayLink(accessions, marker.getAbbreviation());
        } else {
            if (false == infrastructureRepository.hasStandardPublicationAttribution(marker.getZdbID(), MicroarrayWebserviceJob.MICROARRAY_PUB
            )) {
                return null;
            }
            accessions = sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA);
            return NCBIEfetch.getMicroarrayLink(accessions);
        }
    }

    public MarkerExpression getExpressionForGene(Marker marker) {
        MarkerExpression markerExpression = new MarkerExpression();
        if (marker.getMarkerType().getType() != Marker.Type.GENE
                &&
                marker.getMarkerType().getType() != Marker.Type.GENEP
                ) {

            logger.error("should not be trying to get gene expression for marker: \n" + marker);
            return markerExpression;
        }

        markerExpression.setGeoLink(getGeoLinkForMarker(marker));

        // all expression
        MarkerExpressionInstance allMarkerExpressionInstance = new MarkerExpressionInstance();

        allMarkerExpressionInstance.setPublicationCount(
                expressionRepository.getExpressionPubCountForGene(marker));

        if (allMarkerExpressionInstance.getPublicationCount() == 1) {
            allMarkerExpressionInstance.setSinglePublication(expressionRepository.getExpressionSinglePub(marker));
        }
        allMarkerExpressionInstance.setFigureCount(
                expressionRepository.getExpressionFigureCountForGene(marker));
        markerExpression.setAllExpressionData(allMarkerExpressionInstance);

        if (allMarkerExpressionInstance.getFigureCount() == 1) {
            allMarkerExpressionInstance.setSingleFigure(expressionRepository.getExpressionSingleFigure(marker));
        }

        // directly submitted
        logger.info("setting directly submitted expression");
        markerExpression.setDirectlySubmittedExpression(getDirectlySubmittedExpressionGene(marker));
        logger.info("got directly submitted expression");


        // wildtype stages
        // todo: when we handle genes, we need to get this
        WildTypeExpression wildTypeExpression = new WildTypeExpression();
        List<ExpressionExperimentPresentation> expressionExperiments = expressionRepository.getWildTypeExpressionExperiments(marker.getZdbID());
        Collections.sort(expressionExperiments);
        wildTypeExpression.setExpressedStructures(expressionExperiments);

        StageExpressionPresentation expressionPresentation = expressionRepository.getStageExpressionForMarker(marker.getZdbID());
        wildTypeExpression.setExpressionPresentation(expressionPresentation);

        markerExpression.setWildTypeStageExpression(wildTypeExpression);


        return markerExpression;
    }

    public MarkerExpression getExpressionForEfg(Marker marker) {
        MarkerExpression markerExpression = new MarkerExpression();
        if (marker.getMarkerType().getType() != Marker.Type.EFG) {
            logger.error("should not be trying to get efg expression for marker: \n" + marker);
            return markerExpression;
        }

        // all expression
        MarkerExpressionInstance allMarkerExpressionInstance = new MarkerExpressionInstance();
        allMarkerExpressionInstance.setPublicationCount(
                expressionRepository.getExpressionPubCountForEfg(marker));

        if (allMarkerExpressionInstance.getPublicationCount() == 1) {
            allMarkerExpressionInstance.setSinglePublication(expressionRepository.getExpressionSinglePub(marker));
        }
        allMarkerExpressionInstance.setFigureCount(
                expressionRepository.getExpressionFigureCountForEfg(
                        marker));
        markerExpression.setAllExpressionData(allMarkerExpressionInstance);


        return markerExpression;
    }

    public MarkerExpression getExpressionForRnaClone(Clone clone) {
        MarkerExpression markerExpression = new MarkerExpression();
        if (clone.getMarkerType().getType() != Marker.Type.CDNA
                && clone.getMarkerType().getType() != Marker.Type.EST
                ) {
            logger.error("should not be trying to get rna clone expression for marker: \n" + clone);
            return markerExpression;
        }

        markerExpression.setGeoLink(getGeoLinkForMarker(clone));

        // all expression
        MarkerExpressionInstance allMarkerExpressionInstance = new MarkerExpressionInstance();
        allMarkerExpressionInstance.setPublicationCount(
                expressionRepository.getExpressionPubCountForClone(clone));

        if (allMarkerExpressionInstance.getPublicationCount() == 1) {
            allMarkerExpressionInstance.setSinglePublication(expressionRepository.getExpressionSinglePub(clone));
        }
        allMarkerExpressionInstance.setFigureCount(
                expressionRepository.getExpressionFigureCountForClone(clone));

        markerExpression.setAllExpressionData(allMarkerExpressionInstance);

        // directly submitted
        logger.info("setting directly submitted expression");
        markerExpression.setDirectlySubmittedExpression(getDirectlySubmittedExpressionClone(clone));
        logger.info("got directly submitted expression");


        return markerExpression;
    }

    public MicroarrayWebServiceBean processMicroarrayRecordAttributionsForType(Marker.Type markerType) {
        return processMicroarrayRecordAttributionsForType(markerType, -1);
    }

    public MicroarrayWebServiceBean processMicroarrayRecordAttributionsForType(Marker.Type markerType, int maxSize) {
        List<String> allMarkerZdbIds;
        List<String> existingMarkerPubs;
        Marker marker;
        allMarkerZdbIds = markerRepository.getMarkerZdbIdsForType(markerType); // 31K for GENE
        existingMarkerPubs = infrastructureRepository.getPublicationAttributionZdbIdsForType(MICROARRAY_PUB, markerType);

        if (maxSize > 0 && allMarkerZdbIds.size() > maxSize) {
            allMarkerZdbIds = allMarkerZdbIds.subList(0, maxSize);
        }

        if (maxSize > 0 && existingMarkerPubs.size() > maxSize) {
            existingMarkerPubs = existingMarkerPubs.subList(0, maxSize);
        }

        Set<String> addSet = new HashSet<String>();
        Set<String> removeSet = new HashSet<String>();

        for (String zdbID : allMarkerZdbIds) {
            marker = markerRepository.getMarkerByID(zdbID);
            if (getGeoLinkForMarker(marker) != null) {
                // if it is not there add
                if (false == existingMarkerPubs.contains(zdbID)) {
                    addSet.add(zdbID);
                }
                // else, in both places do nothing
            } else {
                // it was there, but now its not . . . remove!
                if (existingMarkerPubs.contains(zdbID)) {
                    removeSet.add(zdbID);
                }
            }
        }

        MicroarrayWebServiceBean microarrayWebServiceBean = new MicroarrayWebServiceBean();
        microarrayWebServiceBean.setAddZdbIds(addSet);
        microarrayWebServiceBean.setRemovedZdbIDs(removeSet);

        return microarrayWebServiceBean;
    }

    public void writeMicroarrayWebServiceBean(MicroarrayWebServiceBean microarrayWebServiceBean) {
        // TODO: do stuff with transactions
        for (String zdbID : microarrayWebServiceBean.getAddZdbIds()) {
            infrastructureRepository.insertPublicAttribution(zdbID, MICROARRAY_PUB);
        }
        for (String zdbID : microarrayWebServiceBean.getRemovedZdbIDs()) {
            infrastructureRepository.removeRecordAttributionForData(zdbID, MICROARRAY_PUB);
        }
    }


    /**
     * Return the set of obsoleted terms from an expression record.
     * Could be one or both terms.
     *
     * @param expressionResult expression
     * @return set of obsolseted terms
     */
    public Set<GenericTerm> getObsoleteTerm(ExpressionResult expressionResult) {
        Set<GenericTerm> obsoletedTerms = new HashSet<GenericTerm>(2);
        if (expressionResult.getEntity().getSuperterm().isObsolete()) {
            obsoletedTerms.add(expressionResult.getEntity().getSuperterm());
        } else if (expressionResult.getEntity().getSubterm() != null && expressionResult.getEntity().getSubterm().isObsolete()) {
            obsoletedTerms.add(expressionResult.getEntity().getSubterm());
        }
        return obsoletedTerms;
    }

    /**
     * Return the set of obsoleted terms from an expression record.
     * Could be one or both terms.
     *
     * @param expressionResult expression
     * @return set of obsolseted terms
     */
    public Set<GenericTerm> getSecondaryTerm(ExpressionResult expressionResult) {
        Set<GenericTerm> obsoletedTerms = new HashSet<GenericTerm>(2);
        if (expressionResult.getEntity().getSuperterm().isSecondary()) {
            obsoletedTerms.add(expressionResult.getEntity().getSuperterm());
        } else if (expressionResult.getEntity().getSubterm() != null && expressionResult.getEntity().getSubterm().isSecondary()) {
            obsoletedTerms.add(expressionResult.getEntity().getSubterm());
        }
        return obsoletedTerms;
    }
}
