package org.zfin.expression.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.datatransfer.microarray.MicroarrayWebserviceJob;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.expression.*;
import org.zfin.expression.presentation.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.gwt.root.dto.ExpressionPhenotypeExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionPhenotypeStatementDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.ExpressedGene;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.util.ExpressionResultSplitStatement;
import org.zfin.util.TermFigureStageRange;
import org.zfin.util.TermStageSplitStatement;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getExpressionRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

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

    private Set<String> thissePubs;

    public ExpressionService() {
    }

    public Set<String> getThissePublicationZdbIDs() {
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
        if (m == null) return null;
        return getGeoLinkForMarker(m);
    }

    public String getGeoLinkForMarker(Marker marker) {
        Collection<String> accessions;
        if (marker.getZdbID().startsWith("ZDB-GENE")) {
            accessions = sequenceRepository.getDBLinkAccessionsForEncodedMarkers(marker, ForeignDBDataType.DataType.RNA);
            accessions.addAll(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
            return NCBIEfetch.getMicroarrayLink(accessions, marker.getAbbreviation());
        } else {
            accessions = sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA);
            return NCBIEfetch.getMicroarrayLink(accessions);
        }
    }

    public String getGeoLinkForMarkerIfExists(Marker marker) {
        if (marker.getZdbID().startsWith("ZDB-GENE")) {
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
        if (marker.getMarkerType().getType() != Marker.Type.GENE
                &&
                marker.getMarkerType().getType() != Marker.Type.GENEP
                ) {

            logger.error("should not be trying to get gene expression for marker: \n" + marker);
            return markerExpression;
        }

        markerExpression.setGeoLink(getGeoLinkForMarkerIfExists(marker));

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
        List<ExpressedStructurePresentation> expressedStructures = expressionRepository.getWildTypeExpressionExperiments(marker.getZdbID());
        Collections.sort(expressedStructures);
        wildTypeExpression.setExpressedStructures(expressedStructures);

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
                expressionRepository.getExpressionFigureCountForEfg(marker));
        markerExpression.setAllExpressionData(allMarkerExpressionInstance);

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
    public static List<ExpressionDisplay> createExpressionDisplays(String initialKey, List<ExpressionResult> expressionResults, List<String> expressionFigureIDs, List<String> expressionPublicationIDs, boolean showCondition) {
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
                FishExperiment fishox = xpResult.getExpressionExperiment().getFishExperiment();
                Experiment exp = fishox.getExperiment();

                String key = initialKey + expressedGene.getZdbID();

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

                ExpressionDisplay xpDisplay;
                // if the key not in the map, instantiate a display object and add it to the map
                // otherwise, get the display object from the map
                if (!map.containsKey(key)) {
                    xpDisplay = new ExpressionDisplay(expressedGene);
                    xpDisplay.setExpressionResults(new ArrayList<ExpressionResult>());
                    xpDisplay.setExperiment(exp);
                    xpDisplay.setExpressionTerms(new HashSet<GenericTerm>());

                    xpDisplay.getExpressionResults().add(xpResult);
                    xpDisplay.getExpressionTerms().add(term);

                    xpDisplay.setExpressedGene(expressedGene);

                    xpDisplay.setFigures(new HashSet<Figure>());
                    xpDisplay.getFigures().addAll(qualifiedFigures);

                    xpDisplay.setPublications(new HashSet<Publication>());
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

                    Collections.sort(xpDisplay.getExpressionResults(), new ExpressionResultTermComparator());

                    xpDisplay.getFigures().addAll(qualifiedFigures);
                    if (expressionPublicationIDs.contains(pub.getZdbID())) {
                        xpDisplay.getPublications().add(pub);
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
                dto.setEnvironment(DTOConversionService.convertToEnvironmentDTO(result.getExpressionFigureStage().getExpressionExperiment().getFishExperiment().getExperiment()));
                ExpressionPhenotypeStatementDTO statement = new ExpressionPhenotypeStatementDTO();
                statement.setQuality(DTOConversionService.convertToTermDTO(phenotypeTerm.getQualityTerm()));
                statement.setEntity(DTOConversionService.convertToEntityDTO(result.getEntity()));
                statement.setTag(phenotypeTerm.getTag());
                if (result.getExpressionFigureStage().getExpressionExperiment().getGene() != null)
                    statement.setGeneName(result.getExpressionFigureStage().getExpressionExperiment().getGene().getAbbreviation());
                if (result.getExpressionFigureStage().getExpressionExperiment().getAntibody() != null)
                    statement.setAntibodyName(result.getExpressionFigureStage().getExpressionExperiment().getAntibody().getName());
                dto.addExpressedTerm(statement);
                list.add(dto);
            }
        }
        return list;
    }
}
