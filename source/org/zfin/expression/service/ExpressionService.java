package org.zfin.expression.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.datatransfer.microarray.MicroarrayWebserviceJob;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.expression.*;
import org.zfin.expression.presentation.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.RibbonType;
import org.zfin.gwt.root.dto.ExpressionPhenotypeExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionPhenotypeStatementDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.ExpressedGene;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.marker.presentation.MarkerReferenceBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.service.RibbonService;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.service.SolrService;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.util.ExpressionResultSplitStatement;
import org.zfin.util.TermFigureStageRange;
import org.zfin.util.TermStageSplitStatement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Service Class that deals with Marker related logic.
 */
@Service
public class ExpressionService {

    public static final String MICROARRAY_PUB = "ZDB-PUB-071218-1";

    private static Logger logger = LogManager.getLogger(ExpressionService.class);

    @Autowired
    private FigureRepository figureRepository;

    @Autowired
    private RibbonService ribbonService;

    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
//    private static ReferenceDatabase geoDatabase ;
//    private static ReferenceDatabase zfEspressoDatabase ;

    private Set<String> thissePubs;
    private static Logger LOG = LogManager.getLogger(ExpressionService.class);

    public ExpressionService() {
    }

    private Set<String> getThissePublicationZdbIDs() {
        if (thissePubs == null) {
            thissePubs = new HashSet<>();
            thissePubs.add("ZDB-PUB-051025-1");
            thissePubs.add("ZDB-PUB-040907-1");
            thissePubs.add("ZDB-PUB-010810-1");
            thissePubs.add("ZDB-PUB-080227-22");
            thissePubs.add("ZDB-PUB-080220-1");
        }
        return thissePubs;
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

    public DirectlySubmittedExpression getDirectlySubmittedExpressionEfg(Marker marker) {
        // this is almost always 1
        List<PublicationExpressionBean> pubList = expressionRepository.getDirectlySubmittedExpressionForEfg(marker);

        // this is almost always 1, so not too expensive
        for (PublicationExpressionBean publicationExpressionBean : pubList) {
            publicationExpressionBean.setNumImages(expressionRepository.getImagesForEfg(publicationExpressionBean));
        }

        DirectlySubmittedExpression directlySubmittedExpression = new DirectlySubmittedExpression();
        directlySubmittedExpression.setMarkerExpressionInstances(pubList);

        return directlySubmittedExpression;
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
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
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
                RecordAttribution publicationAttribution = infrastructureRepository.insertPublicAttribution(marker.getZdbID()
                        , MicroarrayWebserviceJob.MICROARRAY_PUB);
                if (publicationAttribution != null) {
                    logger.debug(publicationAttribution);
                    return 1;
                } else {
                    logger.error("failed to add publication attribution for data[" + publicationAttribution.getDataZdbID()
                            + "] source[" + publicationAttribution.getSourceZdbID() + "]");
                }
            }
        }
        return 0;
    }


    public String getGeoLinkForMarkerZdbId(String markerZdbID) {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);
        if (m == null) {
            return null;
        }
        return getGeoLinkForMarker(m);
    }

    public String getGeoLinkForMarker(Marker marker) {
        Collection<String> accessions;
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            accessions = sequenceRepository.getDBLinkAccessionsForEncodedMarkers(marker, ForeignDBDataType.DataType.RNA);
            accessions.addAll(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
            return NCBIEfetch.getMicroarrayLink(accessions, marker.getAbbreviation());
        } else {
            accessions = sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA);
            return NCBIEfetch.getMicroarrayLink(accessions);
        }
    }

    public LinkDisplay getExpressionAtlasForMarker(String mrkrZdbID, ForeignDB.AvailableName foreignDBName) {
        LinkDisplay gxaLinkDisplay = new LinkDisplay();
        List<DBLink> gxaLinks = sequenceRepository.getAtlasDBLink(mrkrZdbID, foreignDBName.toString());
        String accNumString = "";
        String linkPrefix = "[{";
        String linkSuffix = "]";

        int counter = 0;
        for (DBLink gxaDBLink : gxaLinks) {
            counter++;
            if (gxaLinks.size() > 0) {
                accNumString += "\"" + "value\":\"" + gxaDBLink.getAccessionNumber() + "\"}";
                if (counter < gxaLinks.size()) {
                    accNumString = accNumString + ",{";
                }
            }
        }

        try {
            String accessionNumber = URLEncoder.encode(linkPrefix + accNumString, StandardCharsets.UTF_8.toString());
            gxaLinkDisplay.setAccession(accessionNumber);
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
        gxaLinkDisplay.setMarkerZdbID(mrkrZdbID);
        gxaLinkDisplay.setReferenceDatabaseName("ExpressionAtlas");
        Set<MarkerReferenceBean> gxaReferences = new HashSet<>();
        MarkerReferenceBean gxaReference = new MarkerReferenceBean();
        gxaReference.setZdbID("ZDB-PUB-200103-6");
        gxaLinkDisplay.setUrlPrefix(sequenceRepository.getReferenceDatabaseByID("ZDB-FDBCONT-200123-1").getBaseURL());
        gxaLinkDisplay.setUrlSuffix(linkSuffix);
        Publication gxaPub = getPublicationRepository().getPublication("ZDB-PUB-200103-6");
        gxaReference.setTitle(gxaPub.getTitle());
        gxaReferences.add(gxaReference);
        gxaLinkDisplay.setReferences(gxaReferences);
        return gxaLinkDisplay;
    }

    public String getGeoLinkForMarkerIfExists(Marker marker) {
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            if (!infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers(marker.getZdbID(), MicroarrayWebserviceJob.MICROARRAY_PUB)) {
                return null;
            }
        } else {
            if (!infrastructureRepository.hasStandardPublicationAttribution(marker.getZdbID(), MicroarrayWebserviceJob.MICROARRAY_PUB)) {
                return null;
            }
        }
        return getGeoLinkForMarker(marker);
    }

    public MarkerExpression getExpressionForGene(Marker marker) {
        MarkerExpression markerExpression = new MarkerExpression();
        if (!marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {

            logger.error("should not be trying to get gene expression for marker: \n" + marker);
            return markerExpression;
        }

        // all expression
        MarkerExpressionInstance allMarkerExpressionInstance = new MarkerExpressionInstance();

        List<Publication> pubs = expressionRepository.getExpressionPub(marker);
        allMarkerExpressionInstance.setPublicationCount(pubs.size());

        if (allMarkerExpressionInstance.getPublicationCount() == 1) {
            allMarkerExpressionInstance.setSinglePublication(pubs.get(0));
        }
        allMarkerExpressionInstance.setFigureCount(
                expressionRepository.getExpressionFigureCountForGene(marker));

        markerExpression.setAllExpressionData(allMarkerExpressionInstance);

        // directly submitted
        logger.info("setting directly submitted expression");
        markerExpression.setDirectlySubmittedExpression(getDirectlySubmittedExpressionGene(marker));
        logger.info("got directly submitted expression");

        logger.info("setting only in situ expression for pub tracking");

        markerExpression.setInSituFigCount(expressionRepository.getExpressionFigureCountForGeneInSitu(marker));
        logger.info("got in situ expression for pub tracking");

        return markerExpression;
    }

    public MarkerExpression getExpressionForEfg(Marker marker) {
        MarkerExpression markerExpression = new MarkerExpression();
        if (marker.getMarkerType().getType() != Marker.Type.EFG) {
            logger.error("should not be trying to get efg expression for marker: \n" + marker);
            return markerExpression;
        }

        // all expressionList
        List<Publication> pubs = expressionRepository.getExpressionPub(marker);
        MarkerExpressionInstance allMarkerExpressionInstance = new MarkerExpressionInstance();
        allMarkerExpressionInstance.setPublicationCount(
                expressionRepository.getExpressionPubCountForEfg(marker));

        if (allMarkerExpressionInstance.getPublicationCount() == 1) {
            allMarkerExpressionInstance.setSinglePublication(pubs.get(0));
        }
        allMarkerExpressionInstance.setFigureCount(
                expressionRepository.getExpressionFigureCountForEfg(marker));
        markerExpression.setAllExpressionData(allMarkerExpressionInstance);
        if (allMarkerExpressionInstance.getFigureCount() == 1) {
            allMarkerExpressionInstance.setSingleFigure(expressionRepository.getExpressionSingleFigure(marker));
        }
        // directly submitted
        logger.info("setting directly submitted expression");
        markerExpression.setDirectlySubmittedExpression(getDirectlySubmittedExpressionEfg(marker));
        logger.info("got directly submitted expression");

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

        markerExpression.setGeoLink(getGeoLinkForMarkerIfExists(clone));

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

        Set<String> addSet = new HashSet<>();
        Set<String> removeSet = new HashSet<>();

        for (String zdbID : allMarkerZdbIds) {
            marker = markerRepository.getMarkerByID(zdbID);
            if (getGeoLinkForMarkerIfExists(marker) != null) {
                // if it is not there add
                if (!existingMarkerPubs.contains(zdbID)) {
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
     * @return set of obsoleted terms
     */
    public Set<GenericTerm> getObsoleteTerm(ExpressionResult2 expressionResult) {
        Set<GenericTerm> obsoletedTerms = new HashSet<>(2);
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
     * @return set of obsoleted terms
     */
    public Set<GenericTerm> getSecondaryTerm(ExpressionResult2 expressionResult) {
        Set<GenericTerm> obsoletedTerms = new HashSet<>(2);
        if (expressionResult.getSuperTerm().isSecondary()) {
            obsoletedTerms.add(expressionResult.getSuperTerm());
        } else if (expressionResult.getSubTerm() != null && expressionResult.getSubTerm().isSecondary()) {
            obsoletedTerms.add(expressionResult.getSubTerm());
        }
        return obsoletedTerms;
    }

    /**
     * Return a distinct list of expression experiments from a given expression result collection
     *
     * @param results expression result objects
     * @return list of expression experiments
     */
    public static List<ExpressionExperiment> getDistinctExpressionExperiments(List<ExpressionResult> results) {
        if (results == null) {
            return null;
        }
        Set<ExpressionExperiment> expressionExperimentSet = new HashSet<>(results.size());
        for (ExpressionResult result : results) {
            expressionExperimentSet.add(result.getExpressionExperiment());
        }
        return new ArrayList<>(expressionExperimentSet);
    }

    public static String populateEntities(TermFigureStageRange stageRange) {
        try {
            GenericTerm term = getOntologyRepository().getTermByExample(stageRange.getSuperTerm());
            stageRange.setSuperTerm(term);
        } catch (Exception e) {
            String error = "Superterm not found by oboID: " + stageRange.getSuperTerm().getOboID();
            logger.error(error);
            return error;
        }
        try {
            DevelopmentStage start = getOntologyRepository().getStageByExample(stageRange.getStart());
            if (start == null) {
                throw new RuntimeException("");
            }
            stageRange.setStart(start);
        } catch (Exception e) {
            String error = "Stage not found by abbreviation: " + stageRange.getStart().getAbbreviation();
            logger.error(error);
            return error;
        }
        try {
            DevelopmentStage end = getOntologyRepository().getStageByExample(stageRange.getEnd());
            stageRange.setEnd(end);
        } catch (Exception e) {
            String error = "End Stage not found by abbreviation: " + stageRange.getEnd().getAbbreviation();
            logger.error(error);
            return error;
        }
        return null;
    }

    /**
     * Split an existing expression_result record into multiple ones with a given term and start-end stages.
     *
     * @param statement TermStageSplitStatement
     */
    public static ExpressionResultSplitStatement splitExpressionAnnotations(TermStageSplitStatement statement) {
        // find expression Result records matching the original term-stage-range
        List<ExpressionResult> expressionResultList = getExpressionRepository().getExpressionResultsByTermAndStage(statement.getOriginalTermFigureStageRange());
        if (CollectionUtils.isEmpty(expressionResultList)) {
            return null;
        }
        logger.info("Found " + expressionResultList.size() + " expression_result records");
        ExpressionResultSplitStatement splitStatement = new ExpressionResultSplitStatement();
        for (ExpressionResult result : expressionResultList) {
            // create new records for the remaining split parts
            boolean firstElement = true;
            for (TermFigureStageRange stageRange : statement.getTermFigureStageRangeList()) {
                GenericTerm superTerm = stageRange.getSuperTerm();
                if (!validateStageRange(superTerm, stageRange.getStart(), stageRange.getEnd())) {
                    String message = "Term " + superTerm.getTermName() + " does not appear in the full stage range [";
                    message += stageRange.getStart().getAbbreviation() + ",";
                    message += stageRange.getEnd().getAbbreviation() + "]";
                    throw new RuntimeException(message);
                }
                if (firstElement) {
                    // update existing record
                    result.setSuperTerm(stageRange.getSuperTerm());
                    result.setStartStage(stageRange.getStart());
                    result.setEndStage(stageRange.getEnd());
                    ///result.setComment("Created by a split of " + result.getZdbID());
                    firstElement = false;
                    splitStatement.setOriginalExpressionResult(result);
                } else {
                    ExpressionResult splitResult = new ExpressionResult();
                    splitResult.setExpressionExperiment(result.getExpressionExperiment());
                    splitResult.setSuperTerm(stageRange.getSuperTerm());
                    splitResult.setStartStage(stageRange.getStart());
                    splitResult.setEndStage(stageRange.getEnd());
                    splitResult.setExpressionFound(result.isExpressionFound());
                    ///splitResult.setComment("Created by a split of " + result.getZdbID());
                    //splitResult.setFigures(result.getFigures());
                    for (Figure figure : result.getFigures()) {
                        getExpressionRepository().createExpressionResult(splitResult, figure);
                    }
                    for (Figure figure : result.getFigures()) {
                        Set<Figure> figures = splitResult.getFigures();
                        if (figures == null) {
                            figures = new HashSet<>(2);
                            splitResult.setFigures(figures);
                        }
                        figures.add(figure);
                    }
                    splitStatement.getExpressionResultList().add(splitResult);
                }
            }
        }
        return splitStatement;
    }

    /**
     * Check if a given term is defined (appears) in a given stage range.
     *
     * @param superTerm  term
     * @param startStage DevelopmentStage
     * @param endStage   DevelopmentStage
     */
    private static boolean validateStageRange(GenericTerm superTerm, DevelopmentStage startStage, DevelopmentStage endStage) {
        return DevelopmentStage.stageRangeOverlapsRange(superTerm.getStart(), superTerm.getEnd(), startStage, endStage);
    }

    public static List<FigureExpressionSummary> createExpressionFigureSummaryFromExpressionResults(List<ExpressionResult> results) {
        Map<Figure, Set<ExpressionResult>> figureListMap = new HashMap<>(results.size());
        for (ExpressionResult result : results) {
            for (Figure figure : result.getFigures()) {
                Set<ExpressionResult> expressionResults = figureListMap.get(figure);
                if (expressionResults == null) {
                    expressionResults = new HashSet<>();
                    figureListMap.put(figure, expressionResults);
                }
                expressionResults.add(result);
            }
        }

        List<FigureExpressionSummary> figureExpressionSummaries = new ArrayList<>(figureListMap.keySet().size());
        List<ExpressionExperiment> expressionExperiments = ExpressionService.getDistinctExpressionExperiments(results);
        for (Figure figure : figureListMap.keySet()) {
            FigureExpressionSummary figureExpressionSummary = new FigureExpressionSummary(figure);
            List<ExpressedGene> expressedGenes = new ArrayList<>(figureListMap.get(figure).size());
            for (ExpressionExperiment expressionExperiment : expressionExperiments) {
                List<ExpressionStatement> expressionStatements = new ArrayList<>(figureListMap.get(figure).size());
                if (!expressionExperiment.getAllFigures().contains(figure)) {
                    continue;
                }
                for (ExpressionResult result : expressionExperiment.getExpressionResults()) {
                    ExpressionStatement statement = new ExpressionStatement();
                    if (!result.getFigures().contains(figure)) {
                        continue;
                    }
                    statement.setEntity(result.getEntity());
                    statement.setExpressionFound(result.isExpressionFound());
                    // ensure distinctness
                    if (!expressionStatements.contains(statement)) {
                        expressionStatements.add(statement);
                    }
                }
                Collections.sort(expressionStatements);
                ExpressedGene expressedGene = new ExpressedGene(expressionExperiment.getGene());
                expressedGene.setExpressionStatements(expressionStatements);
                expressedGenes.add(expressedGene);
            }
            figureExpressionSummary.setExpressedGenes(expressedGenes);
            figureExpressionSummaries.add(figureExpressionSummary);
        }
        return figureExpressionSummaries;
    }

    /**
     * Create a list of expressionDisplay objects organized by expressed gene.
     */
    public static List<ExpressionDisplay> createExpressionDisplays(String initialKey,
                                                                   List<ExpressionResult> expressionResults,
                                                                   List<String> expressionFigureIDs,
                                                                   List<String> expressionPublicationIDs,
                                                                   boolean showCondition) {
        if (CollectionUtils.isEmpty(expressionResults) ||
                CollectionUtils.isEmpty(expressionFigureIDs) ||
                CollectionUtils.isEmpty(expressionPublicationIDs)) {
            return null;
        }

        // a map of zdbIDs of expressed genes as keys and display objects as values
        Map<String, ExpressionDisplay> map = new HashMap<>();

        for (ExpressionResult xpResult : expressionResults) {
            Marker expressedGene = xpResult.getExpressionExperiment().getGene();
            if (expressedGene != null) {
                logger.info(expressedGene.getAbbreviation());
                FishExperiment fishox = xpResult.getExpressionExperiment().getFishExperiment();
                Experiment exp = fishox.getExperiment();

                String key = initialKey + expressedGene.getZdbID();

                if (showCondition && fishox.isStandardOrGenericControl()) {
                    key += "standard";
                }

                if (showCondition && exp.isChemical()) {
                    key += "chemical";
                }
                if (CollectionUtils.isEmpty(xpResult.getFigures())) {
                    return null;
                }

                Set<Figure> figs = xpResult.getFigures();
                Set<Figure> qualifiedFigures = new HashSet<>();

                for (Figure fig : figs) {
                    if (expressionFigureIDs.contains(fig.getZdbID())) {
                        qualifiedFigures.add(fig);
                    }
                }

                GenericTerm term = xpResult.getSuperTerm();
                Publication pub = xpResult.getExpressionExperiment().getPublication();

                ExpressionDisplay xpDisplay;
                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!map.containsKey(key)) {
                    xpDisplay = new ExpressionDisplay(expressedGene);
                    xpDisplay.setExpressionResults(new ArrayList<>());
                    xpDisplay.setExperiment(exp);
                    xpDisplay.setExpressionTerms(new HashSet<>());

                    xpDisplay.getExpressionResults().add(xpResult);
                    xpDisplay.getExpressionTerms().add(term);

                    xpDisplay.setExpressedGene(expressedGene);

                    xpDisplay.setFigures(new HashSet<>());
                    xpDisplay.getFigures().addAll(qualifiedFigures);

                    SortedMap<Publication, SortedSet<Figure>> figuresPerPub = new TreeMap<>();
                    for (Figure figure : qualifiedFigures) {
                        Publication publication = figure.getPublication();
                        if (!figuresPerPub.containsKey(publication)) {
                            SortedSet<Figure> sortedFigs = new TreeSet<>();
                            sortedFigs.add(figure);
                            figuresPerPub.put(publication, sortedFigs);
                        } else {
                            figuresPerPub.get(publication).add(figure);
                        }
                    }

                    xpDisplay.setFiguresPerPub(figuresPerPub);

                    xpDisplay.setPublications(new HashSet<>());
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                        xpDisplay.getPublications().add(pub);

                        if (!xpDisplay.noFigureOrFigureWithNoLabel()) {
                            map.put(key, xpDisplay);
                        }
                    }
                } else {
                    xpDisplay = map.get(key);

                    if (!xpDisplay.getExpressionTerms().contains(term)) {
                        xpDisplay.getExpressionResults().add(xpResult);
                        xpDisplay.getExpressionTerms().add(term);
                    }

                    (xpDisplay.getExpressionResults()).sort(new ExpressionResultTermComparator());

                    xpDisplay.getFigures().addAll(qualifiedFigures);
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                        xpDisplay.getPublications().add(pub);
                    }

                    SortedMap<Publication, SortedSet<Figure>> figuresPerPub = xpDisplay.getFiguresPerPub();
                    for (Figure figure : qualifiedFigures) {
                        Publication publication = figure.getPublication();
                        if (!figuresPerPub.containsKey(publication)) {
                            SortedSet<Figure> sortedFigs = new TreeSet<>();
                            sortedFigs.add(figure);
                            xpDisplay.getFiguresPerPub().put(publication, sortedFigs);
                        } else {
                            xpDisplay.getFiguresPerPub().get(publication).add(figure);
                        }
                    }
                }

            }
        }

        List<ExpressionDisplay> expressionDisplays = new ArrayList<>(map.size());

        if (map.values().size() > 0) {
            expressionDisplays.addAll(map.values());
            Collections.sort(expressionDisplays);
            return expressionDisplays;
        } else {
            return null;
        }


    }

    public static List<ExpressionPhenotypeExperimentDTO> createPhenotypeFromExpressions(List<ExpressionResult2> expressionResultList) {
        List<ExpressionPhenotypeExperimentDTO> list = new ArrayList<>();
        for (ExpressionResult2 result : expressionResultList) {
            for (ExpressionPhenotypeTerm phenotypeTerm : result.getPhenotypeTermSet()) {
                ExpressionPhenotypeExperimentDTO dto = new ExpressionPhenotypeExperimentDTO();
                dto.setFish(DTOConversionService.convertToFishDtoFromFish(result.getExpressionFigureStage().getExpressionExperiment().getFishExperiment().getFish()));
                dto.setFigure(DTOConversionService.convertToFigureDTO(result.getExpressionFigureStage().getFigure()));
                dto.setStart(DTOConversionService.convertToStageDTO(result.getExpressionFigureStage().getStartStage()));
                dto.setEnd(DTOConversionService.convertToStageDTO(result.getExpressionFigureStage().getEndStage()));
                dto.setExperiment(DTOConversionService.convertToExperimentDTO(result.getExpressionFigureStage().getExpressionExperiment().getFishExperiment().getExperiment()));
                ExpressionPhenotypeStatementDTO statement = new ExpressionPhenotypeStatementDTO();
                statement.setQuality(DTOConversionService.convertToTermDTO(phenotypeTerm.getQualityTerm()));
                statement.setEntity(DTOConversionService.convertToEntityDTO(result.getEntity()));
                statement.setTag(phenotypeTerm.getTag());
                if (result.getExpressionFigureStage().getExpressionExperiment().getGene() != null) {
                    statement.setGeneName(result.getExpressionFigureStage().getExpressionExperiment().getGene().getAbbreviation());
                }
                if (result.getExpressionFigureStage().getExpressionExperiment().getAntibody() != null) {
                    statement.setAntibodyName(result.getExpressionFigureStage().getExpressionExperiment().getAntibody().getName());
                }
                dto.addExpressedTerm(statement);
                list.add(dto);
            }
        }
        return list;
    }

    /**
     * Create a list of expressionDisplay objects organized by expressed gene.
     */
    public static List<ProteinExpressionDisplay> createProteinExpressionDisplays(String initialKey, List<ExpressionResult> expressionResults, List<String> expressionFigureIDs, List<String> expressionPublicationIDs, boolean showCondition) {
        if (CollectionUtils.isEmpty(expressionResults) ||
                CollectionUtils.isEmpty(expressionFigureIDs) ||
                CollectionUtils.isEmpty(expressionPublicationIDs)) {
            return null;
        }

        // a map of zdbIDs of antibodies as keys and display objects as values
        Map<String, ProteinExpressionDisplay> map = new HashMap<>();

        for (ExpressionResult xpResult : expressionResults) {
            Marker antiGene = xpResult.getExpressionExperiment().getGene();
            Antibody antibody = xpResult.getExpressionExperiment().getAntibody();
            if (antibody != null) {
                FishExperiment fishox = xpResult.getExpressionExperiment().getFishExperiment();
                Experiment exp = fishox.getExperiment();

                String key = initialKey + antibody.getZdbID();

                if (showCondition && fishox.isStandardOrGenericControl()) {
                    key += "standard";
                }

                if (showCondition && exp.isChemical()) {
                    key += "chemical";
                }

                Set<Figure> figs = xpResult.getFigures();
                Set<Figure> qualifiedFigures = new HashSet<>();

                for (Figure fig : figs) {
                    if (expressionFigureIDs.contains(fig.getZdbID())) {
                        qualifiedFigures.add(fig);
                    }
                }

                GenericTerm term = xpResult.getSuperTerm();
                Publication pub = xpResult.getExpressionExperiment().getPublication();

                ProteinExpressionDisplay xpDisplay;
                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!map.containsKey(key)) {
                    xpDisplay = new ProteinExpressionDisplay(antibody);
                    xpDisplay.setAntiGene(antiGene);
                    xpDisplay.setExpressionResults(new ArrayList<>());
                    xpDisplay.setExperiment(exp);
                    xpDisplay.setExpressionTerms(new HashSet<>());

                    xpDisplay.getExpressionResults().add(xpResult);
                    xpDisplay.getExpressionTerms().add(term);

                    xpDisplay.setFigures(new HashSet<>());
                    xpDisplay.getFigures().addAll(qualifiedFigures);

                    SortedMap<Publication, SortedSet<Figure>> figuresPerPub = new TreeMap<>();
                    for (Figure figure : qualifiedFigures) {
                        Publication publication = figure.getPublication();
                        if (!figuresPerPub.containsKey(publication)) {
                            SortedSet<Figure> sortedFigs = new TreeSet<>();
                            sortedFigs.add(figure);
                            figuresPerPub.put(publication, sortedFigs);
                        } else {
                            figuresPerPub.get(publication).add(figure);
                        }
                    }

                    xpDisplay.setFiguresPerPub(figuresPerPub);

                    xpDisplay.setPublications(new HashSet<>());
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                        xpDisplay.getPublications().add(pub);

                        if (!xpDisplay.noFigureOrFigureWithNoLabel()) {
                            map.put(key, xpDisplay);
                        }
                    }
                } else {
                    xpDisplay = map.get(key);

                    if (!xpDisplay.getExpressionTerms().contains(term)) {
                        xpDisplay.getExpressionResults().add(xpResult);
                        xpDisplay.getExpressionTerms().add(term);
                    }

                    (xpDisplay.getExpressionResults()).sort(new ExpressionResultTermComparator());

                    xpDisplay.getFigures().addAll(qualifiedFigures);
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                        xpDisplay.getPublications().add(pub);
                    }

                    SortedMap<Publication, SortedSet<Figure>> figuresPerPub = xpDisplay.getFiguresPerPub();
                    for (Figure figure : qualifiedFigures) {
                        Publication publication = figure.getPublication();
                        if (!figuresPerPub.containsKey(publication)) {
                            SortedSet<Figure> sortedFigs = new TreeSet<>();
                            sortedFigs.add(figure);
                            xpDisplay.getFiguresPerPub().put(publication, sortedFigs);
                        } else {
                            xpDisplay.getFiguresPerPub().get(publication).add(figure);
                        }
                    }
                }

            }
        }

        List<ProteinExpressionDisplay> proteinExpressionDisplays = new ArrayList<>(map.size());

        if (map.values().size() > 0) {
            proteinExpressionDisplays.addAll(map.values());
            Collections.sort(proteinExpressionDisplays);
            return proteinExpressionDisplays;
        } else {
            return null;
        }


    }

    public JsonResultResponse<Image> getExpressionImages(String geneId, String termId, String supertermId, String subtermId, boolean includeReporter, boolean onlyInSitu, boolean isOther, Pagination pagination) throws IOException, SolrServerException {
        JsonResultResponse<Image> response = new JsonResultResponse<>();

        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/expression-annotation");
        query.addFilterQuery("xref:" + geneId);
        query.addFilterQuery("has_image:true");
        ribbonService.addRibbonTermQuery(query, RibbonType.EXPRESSION, termId, isOther);

        String postcomposedTermId = null;

        if (StringUtils.isNotEmpty(supertermId)) {
            postcomposedTermId = SolrService.luceneEscape(supertermId);
        }
        if (StringUtils.isNotEmpty(subtermId)) {
            postcomposedTermId = postcomposedTermId + "," + SolrService.luceneEscape(subtermId);
        }
        if (StringUtils.isNotEmpty(postcomposedTermId)) {
            query.addFilterQuery("postcomposed_term_id:" + postcomposedTermId);
        }

        addReporterFilter(query, includeReporter);
        addInSituFilter(query, onlyInSitu);

        String jsonFacet = "{" +
            "  images: {" +
            "    terms: {" +
            "      field: img_zdb_id," +
            "      limit: " + pagination.getLimit() + "," +
            "      offset: " + pagination.getStart() + "," +
            "      numBuckets: true," +
            "      sort: \"img_order desc\"," +
            "      facet : {" +
            "        img_order: \"max(expression_image_sort)\"" +
            "      }" +
            "    }" +
            "  }" +
            "}";
        query.set("json.facet", jsonFacet);

        QueryResponse queryResponse = SolrService.getSolrClient().query(query);

        NamedList<Object> facets = (NamedList<Object>) queryResponse.getResponse().get("facets");
        NamedList<Object> images = (NamedList<Object>) facets.get("images");
        if (images == null) {
            response.setTotal(0);
            response.setResults(Collections.emptyList());
        } else {
            Integer total = (Integer) Optional.of(images.get("numBuckets")).orElse(0);
            List<String> imageIds = ((List<NamedList<Object>>) images.get("buckets")).stream()
                    .map(bucket -> bucket.get("val").toString())
                    .collect(Collectors.toList());
            response.setTotal(total);
            response.setResults(figureRepository.getImages(imageIds));
        }

        return response;
    }

    public void addReporterFilter(SolrQuery query, boolean includeReporter) {
        if (includeReporter) {
            query.addFilterQuery("is_wildtype:true OR is_reporter:true");
        } else {
            query.addFilterQuery("is_wildtype:true");
        }
    }

    public void addInSituFilter(SolrQuery query, boolean onlyInSitu) {
        if (onlyInSitu) {
            query.addFilterQuery("assay:\"mRNA in situ hybridization\"");
        }
    }
}
