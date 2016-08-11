package org.zfin.fish.repository;

import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.service.ExpressionService;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.presentation.FishResult;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.PhenotypeSummaryCriteria;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.FishGenotypeExpressionStatistics;
import org.zfin.mutant.presentation.FishGenotypePhenotypeStatistics;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Service class to provide methods for retrieving fish records from the data warehouse.
 */
public class FishService {

    private static Logger logger = Logger.getLogger(FishService.class);

    public static FishSearchResult getFish(FishSearchCriteria criteria) {

        FishSearchResult results = new FishSearchResult();

        SolrQuery query = generateFishSearchSolrQuery(criteria);

        SolrClient solrClient = SolrService.getSolrClient("prototype");
        QueryResponse response;
        try {
            response = solrClient.query(query);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        List<FishResult> solrSearchResults = response.getBeans(FishResult.class);

        for (FishResult fishResult : solrSearchResults) {
            String category = fishResult.getCategory();
            if (StringUtils.equals(category, Category.FISH.getName()) || StringUtils.equals(category, Category.REPORTER_LINE.getName())) {
                Fish fish = RepositoryFactory.getMutantRepository().getFish(fishResult.getId());
                if (fish != null) {
                    fishResult.setFish(fish);
                    fishResult.setFeatureGenes(getFeatureGenes(fish));
                    addFigures(fishResult, criteria);
                }
            } else if (StringUtils.equals(category, Category.MUTANT.getName())) {
                Feature feature = RepositoryFactory.getFeatureRepository().getFeatureByID(fishResult.getId());
                if (feature != null) {
                    fishResult.setFeatureGenes(getFeatureGeneList(feature));
                }
            }
        }
        results.setResultsFound((int) response.getResults().getNumFound());
        results.setResults(solrSearchResults);

        return results;
    }

    private static SolrQuery generateFishSearchSolrQuery(FishSearchCriteria criteria) {

        SolrQuery query = new SolrQuery();

        query.setRequestHandler("/fish-search");

        //the main query box, should probably be just matching against a subset of the record
        query.setQuery(criteria.getGeneOrFeatureNameCriteria().getValue());

        //results per page
        query.setRows(criteria.getRows());

        //page
        query.setStart(criteria.getStart());

        if (criteria.getExcludeSequenceTargetingReagentCriteria() != null && criteria.getExcludeSequenceTargetingReagentCriteria().isTrue()) {
            query.addFilterQuery("-" + FieldName.SEQUENCE_TARGETING_REAGENT.getName() + ":[* TO *]");
        }

        if (criteria.getRequireSequenceTargetingReagentCriteria() != null && criteria.getRequireSequenceTargetingReagentCriteria().isTrue()) {
            query.addFilterQuery(FieldName.SEQUENCE_TARGETING_REAGENT.getName() + ":[* TO *]");
        }

        if (criteria.getExcludeTransgenicsCriteria() != null && criteria.getExcludeTransgenicsCriteria().isTrue()) {
            query.addFilterQuery("-" + FieldName.CONSTRUCT.getName() + ":[* TO *]");
        }

        if (criteria.getRequireTransgenicsCriteria() != null && criteria.getRequireTransgenicsCriteria().isTrue()) {
            query.addFilterQuery(FieldName.CONSTRUCT.getName() + ":[* TO *]");
        }

        if (criteria.getMutationTypeCriteria() != null && criteria.getMutationTypeCriteria().hasValues()) {
            query.addFilterQuery(FieldName.MUTATION_TYPE.getName() + ":\"" + criteria.getMutationTypeCriteria().getValue() + "\"");
        }

        if (criteria.getPhenotypeAnatomyCriteria() != null && criteria.getPhenotypeAnatomyCriteria().hasValues()) {
            for (String term : criteria.getPhenotypeAnatomyCriteria().getNames()) {
                query.addFilterQuery(FieldName.AFFECTED_ANATOMY.getName() + ":\"" + term + "\""
                                + " OR " + FieldName.AFFECTED_BIOLOGICAL_PROCESS.getName() + ":\"" + term + "\""
                                + " OR " + FieldName.AFFECTED_MOLECULAR_FUNCTION.getName() + ":\"" + term + "\""
                                + " OR " + FieldName.AFFECTED_CELLULAR_COMPONENT.getName() + ":\"" + term + "\""
                );
            }
        }

        return query;
    }

    public static List<FeatureGene> getFeatureGenes(Fish fish) {
        return getFeatureGenes(fish, true);
    }

    public static List<FeatureGene> getFeatureGenes(Fish fish, boolean includeStrs) {
        List<FeatureGene> featureGenes = new ArrayList<>();

        if (fish == null) {
            return null;
        }

        if (fish.getGenotype() != null) {
            Set<FeatureGene> featureGeneSet = new HashSet<>();
            for (GenotypeFeature genotypeFeature : fish.getGenotype().getGenotypeFeatures()) {
                Feature feature = genotypeFeature.getFeature();
                for (FeatureGene fg : getFeatureGeneList(feature)) {
                    fg.setParentalZygosityDisplay(genotypeFeature.getParentalZygosityDisplay());
                    featureGeneSet.add(fg);
                }
            }
            featureGenes.addAll(featureGeneSet);
        }
        if (includeStrs) {
            for (Marker str : fish.getStrList()) {
                Set<MarkerRelationship> mrels = str.getFirstMarkerRelationships();
                for (MarkerRelationship mrel : mrels) {
                    if (StringUtils.equals(mrel.getMarkerRelationshipType().getName(), MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE.toString())) {
                        FeatureGene featureGene = new FeatureGene();
                        featureGene.setSequenceTargetingReagent(str);
                        featureGene.setGene(mrel.getSecondMarker());
                        featureGenes.add(featureGene);
                    }
                }
            }
        }

        return featureGenes;
    }

    private static List<FeatureGene> getFeatureGeneList(Feature feature) {
        List<FeatureGene> featureGeneList = new ArrayList<>(feature.getFeatureMarkerRelations().size());
        for (FeatureMarkerRelationship rel : feature.getFeatureMarkerRelations()) {
            FeatureGene fg = new FeatureGene();
            fg.setFeature(feature);
            boolean hasMarkerOrConstruct = false;
            Marker marker = rel.getMarker();
            if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                fg.setGene(marker);
                hasMarkerOrConstruct = true;
            } else if (marker.isInTypeGroup(Marker.TypeGroup.CONSTRUCT)) {
                fg.setConstruct(marker);
                hasMarkerOrConstruct = true;
            }
            if (hasMarkerOrConstruct) {
                featureGeneList.add(fg);
            }
        }
        // at least have a feature in this list
        if (CollectionUtils.isEmpty(featureGeneList)) {
            FeatureGene featureGene = new FeatureGene();
            featureGene.setFeature(feature);
            featureGeneList.add(featureGene);
        }
        return featureGeneList;
    }

    private static void addFigures(FishResult fishResult, FishSearchCriteria criteria) {
        List<String> values = null;
        if (criteria.getPhenotypeAnatomyCriteria() != null) {
            values = criteria.getPhenotypeAnatomyCriteria().getValues();
        }
        addFiguresByTermValues(fishResult, values);
    }

    private static void addFiguresByTermValues(FishResult fishResult, List<String> values) {
        Set<ZfinFigureEntity> figures = FishService.getFiguresByFishAndTerms(fishResult.getFish().getZdbID(), values);
        Set<ZfinFigureEntity> expFigures = getAllExpressionFigureEntitiesForFish(fishResult.getFish());
        setImageAttributeOnFish(fishResult, figures, expFigures);
    }

    private static void setImageAttributeOnFish(FishResult fishResult, Set<ZfinFigureEntity> figures, Set<ZfinFigureEntity> expFigures) {

        if (figures == null || figures.size() == 0) {
            if (expFigures == null || expFigures.size() == 0) {
                return;
            }
        }
        fishResult.setPhenotypeFigures(figures);
        fishResult.setExpressionFigures(expFigures);
        for (ZfinFigureEntity figure : figures) {
            if (figure.isHasImage()) {
                fishResult.setImageAvailable(true);
            }
        }
        for (ZfinFigureEntity figure : expFigures) {
            if (figure.isHasImage()) {
                fishResult.setImageAvailable(true);
            }
        }
    }

    public static List<FigureSummaryDisplay> getPhenotypeSummary(String fishID, FishSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new FishSearchCriteria();
            criteria.setPhenotypeAnatomyCriteria(new SearchCriterion(SearchCriterionType.PHENOTYPE_ANATOMY_ID, true));
        }
        Set<ZfinFigureEntity> zfinFigureEntities = FishService.getFiguresByFishAndTerms(fishID, criteria.getPhenotypeAnatomyCriteria().getValues());
        if (zfinFigureEntities == null) {
            return null;
        }

        List<FigureSummaryDisplay> figureSummaryDisplays = new ArrayList<>(zfinFigureEntities.size());
        Set<Figure> figures = new HashSet<>(zfinFigureEntities.size());
        for (ZfinFigureEntity figureEntity : zfinFigureEntities) {
            FigureSummaryDisplay figureSummaryDisplay = new FigureSummaryDisplay();
            Figure figure = getPublicationRepository().getFigure(figureEntity.getID());
            if (figures.add(figure)) {
                figureSummaryDisplay.setFigure(figure);
                figureSummaryDisplay.setPublication(figure.getPublication());
                List<PhenotypeStatementWarehouse> phenotypeStatements = FishService.getPhenotypeStatements(figure, fishID);
                figureSummaryDisplay.setPhenotypeStatementList(FishService.getDistinctPhenotypeStatements(phenotypeStatements));
                figureSummaryDisplays.add(figureSummaryDisplay);
                if (!figure.isImgless()) {
                    figureSummaryDisplay.setImgCount(figure.getImages().size());
                    figureSummaryDisplay.setThumbnail(figure.getImages().iterator().next().getThumbnail());
                }
            }
        }
        return figureSummaryDisplays;
    }

    public static PhenotypeSummaryCriteria getPhenotypeSummaryCriteria(String fishID) {
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        PhenotypeSummaryCriteria criteria = new PhenotypeSummaryCriteria();
        criteria.setFish(fish);
        Set<FishExperiment> fishExperiments = getMutantRepository().getFish(fishID).getFishExperiments();
        List<FishExperiment> fishExperimentList = new ArrayList<>(fishExperiments.size());
        fishExperimentList.addAll(fishExperiments);
        criteria.setFishExperiments(fishExperimentList);
        return criteria;
    }


    /**
     * Retrieve all distinct phenotype statements from a given phenotype statement list.
     *
     * @param phenotypeStatementList phenotypeStatementList
     * @return list of phenotype statements
     */
    public static List<PhenotypeStatementWarehouse> getDistinctPhenotypeStatements(List<PhenotypeStatementWarehouse> phenotypeStatementList) {
        if (phenotypeStatementList == null) {
            return null;
        }

        List<PhenotypeStatementWarehouse> phenotypeStatements = new ArrayList<>(phenotypeStatementList.size());
        for (PhenotypeStatementWarehouse phenotypeStatement : phenotypeStatementList) {
            if (isDistinctPhenotype(phenotypeStatement, phenotypeStatements)) {
                phenotypeStatements.add(phenotypeStatement);
            }
        }
        Collections.sort(phenotypeStatements);
        return phenotypeStatements;
    }

    private static boolean isDistinctPhenotype(PhenotypeStatementWarehouse phenotypeStatement, List<PhenotypeStatementWarehouse> phenotypeStatements) {
        if (phenotypeStatements == null) {
            return true;
        }
        for (PhenotypeStatementWarehouse phenoStatement : phenotypeStatements) {
            if (phenoStatement.equalsByName(phenotypeStatement)) {
                return false;
            }
        }
        return true;
    }


    public static FishSearchCriteria getFishSearchCriteria(FishSearchFormBean bean) {
        return new FishSearchCriteria(bean);
    }

    public static String getGenotypeID(String fishID) {
        if (fishID == null) {
            return null;
        }
        String[] ids = fishID.split(",");
        String returnId = null;
        for (String id : ids) {
            if (ActiveData.validateID(id).equals(ActiveData.Type.GENO)) {
                if (returnId == null) {
                    returnId = id;
                } else {
                    throw new RuntimeException("Found more than one GENO id: " + fishID);
                }
            }
        }
        return returnId;
    }

    public static List<PhenotypeStatementWarehouse> getPhenotypeStatements(Figure figure, String fishID) {
        return RepositoryFactory.getPhenotypeRepository().getPhenotypeStatements(figure, fishID);
    }

    /**
     * Retrieve Summary of expression statements, figures and pub info
     *
     * @param fishID fish ID
     * @param geneID gene ID
     * @return list of expression statements records grouped by figure.
     */
    public static List<FigureExpressionSummary> getExpressionSummary(String fishID, String geneID) {

        Fish fish = getMutantRepository().getFish(fishID);
        Set<FishExperiment> fishOx = fish.getFishExperiments();

        List<ExpressionResult> results = getMutantRepository().getExpressionSummary(fishOx, geneID);
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }

        return ExpressionService.createExpressionFigureSummaryFromExpressionResults(results);
    }

    /**
     * Check if a given fish has expression data with at least one figure that has an image.
     *
     * @param fishID fish ID
     * @return boolean
     */
    public static boolean hasImagesOnExpressionFigures(String fishID) {
        Fish fish = getMutantRepository().getFish(fishID);
        String genotypeID = fish.getGenotype().getZdbID();
        Set<FishExperiment> fishOx = fish.getFishExperiments();

        return getMutantRepository().hasImagesOnExpressionFigures(genotypeID, fishOx);
    }


    public static Integer getCitationCount(Fish fish) {
        return getFishPublications(fish).size();
    }

    public static Set<Publication> getFishPublications(Fish fish) {
        List<Publication> pubs = RepositoryFactory.getMutantRepository().getFishAttributionList(fish);
        Set<Publication> publicationSet = new HashSet<>(pubs.size());
        publicationSet.addAll(pubs);
        return publicationSet;
    }

    public static List<Marker> getAffectedGenes(Fish fish) {
        List<SequenceTargetingReagent> strList = fish.getStrList();
        Set<Marker> geneSet = new TreeSet<>();
        Set<Marker> affectedMarkerOnGenotype = GenotypeService.getAffectedMarker(fish.getGenotype());
        if (affectedMarkerOnGenotype != null) {
            geneSet.addAll(affectedMarkerOnGenotype);
        }
        if (CollectionUtils.isNotEmpty(strList)) {
            for (SequenceTargetingReagent str : strList) {
                geneSet.addAll(str.getTargetGenes());
            }
        }
        List<Marker> geneList = new ArrayList<>(geneSet.size());
        geneList.addAll(geneSet);
        return geneList;
    }


    /**
     * Retrieve all figures for given fish id
     * that have phenotypes associated with the termID list
     * directly or indirectly through a substructure.
     *
     * @param fishID  fish ID
     * @param termIDs term ID list
     * @return set of figures
     */
    public static Set<ZfinFigureEntity> getFiguresByFishAndTerms(String fishID, List<String> termIDs) {
/*
        if (CollectionUtils.isEmpty(termIDs)) {
            Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
            if (fish == null)
                return null;
            return getCleanPhenotypeFigureEntitiesForFish(fish);
        }
*/

        SolrClient server = SolrService.getSolrClient("prototype");

        SolrQuery query = new SolrQuery();

        query.setFields(FieldName.ID.getName(), FieldName.FIGURE_ID.getName(), FieldName.THUMBNAIL.getName());
        query.addFilterQuery(FieldName.CATEGORY.getName() + ":\"" + Category.PHENOTYPE.getName() + "\"");
        query.addFilterQuery(FieldName.XREF.getName() + ":" + fishID);
        query.setRows(500);
        query.add("group", "true");
        query.add("group.field", "figure_id");
        query.add("group.ngroups", "true");


        if (CollectionUtils.isNotEmpty(termIDs)) {

            List<String> terms = new ArrayList<>();
            for (String termID : termIDs) {
                Term term = RepositoryFactory.getInfrastructureRepository().getTermByID(termID);
                terms.add("\"" + term.getTermName() + "\"");
            }

            String termQuery = Joiner.on(" OR ").join(terms);
            query.addFilterQuery(FieldName.ANATOMY.getName() + ":(" + termQuery + ")"
                            + " OR " + FieldName.BIOLOGICAL_PROCESS.getName() + ":(" + termQuery + ")"
                            + " OR " + FieldName.MOLECULAR_FUNCTION.getName() + ":(" + termQuery + ")"
                            + " OR " + FieldName.CELLULAR_COMPONENT.getName() + ":(" + termQuery + ")"
            );

        }

        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        Set<ZfinFigureEntity> figureEntitySet = new HashSet<>();

        for (GroupCommand groupCommand : response.getGroupResponse().getValues()) {

            if (CollectionUtils.isNotEmpty(groupCommand.getValues())) {
                for (Group group : groupCommand.getValues()) {
                    for (SolrDocument doc : group.getResult()) {
                        ZfinFigureEntity figure = new ZfinFigureEntity();

                        String figureZdbID = (String) doc.get(FieldName.FIGURE_ID.getName());
                        figure.setID(figureZdbID);
                        if (CollectionUtils.isNotEmpty((Collection) doc.get(FieldName.THUMBNAIL.getName()))) {
                            figure.setHasImage(true);
                        } else {
                            figure.setHasImage(false);
                        }
                        figureEntitySet.add(figure);
                    }
                }
            }
        }

        return figureEntitySet;

    }

    public static Set<ZfinFigureEntity> getAllExpressionFigureEntitiesForFish(Fish fish) {
        List<ExpressionResult> expressionResults = getExpressionRepository().getExpressionResultsByFish(fish);
        Set<ZfinFigureEntity> figureEntities = new HashSet<>();
        ZfinFigureEntity figureEntity;
        Set<Figure> expressionFigures = new HashSet<>();
        for (ExpressionResult expressionResult : expressionResults) {
            expressionFigures.addAll(expressionResult.getFigures());
        }

        for (Figure figure : expressionFigures) {
            figureEntity = new ZfinFigureEntity();
            figureEntity.setID(figure.getZdbID());
            if (figure.getImages() != null) {
                figureEntity.setHasImage(true);
            } else {
                figureEntity.setHasImage(false);
            }
            figureEntities.add(figureEntity);
        }

        return figureEntities;
    }

    public static List<GenotypeFishResult> getFishExperiementSummaryForGenotype(Genotype genotype) {
        MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
        List<FishExperiment> fishExperimentList = mutantRepository.getFishExperiment(genotype);
        List<GenotypeFishResult> fishGenotypePhenotypeStatisticsList = createResult(fishExperimentList);
        List<Fish> fishList = mutantRepository.getFishByGenotypeNoExperiment(genotype);
        addPureFish(fishGenotypePhenotypeStatisticsList, fishList);
        return fishGenotypePhenotypeStatisticsList;
    }

    private static List<GenotypeFishResult> createResult(List<FishExperiment> fishExperimentList) {
        Map<Fish, GenotypeFishResult> statisticsMap = new TreeMap<>();
        for (FishExperiment fishExperiment : fishExperimentList) {
            Fish fish = fishExperiment.getFish();
            GenotypeFishResult stat = statisticsMap.get(fish);
            if (stat == null) {
                stat = new GenotypeFishResult(fish);
                stat.setAffectedMarkers(getAffectedGenes(fish));
                FishGenotypePhenotypeStatistics pheno = stat.getFishGenotypePhenotypeStatistics();
                if (pheno == null) {
                    pheno = new FishGenotypePhenotypeStatistics(fish);
                    stat.setFishGenotypePhenotypeStatistics(pheno);
                }
                FishGenotypeExpressionStatistics expression = stat.getFishGenotypeExpressionStatistics();
                if (expression == null) {
                    expression = new FishGenotypeExpressionStatistics(fish);
                    stat.setFishGenotypeExpressionStatistics(expression);
                }
                statisticsMap.put(fish, stat);
            }
            stat.getFishGenotypePhenotypeStatistics().addFishExperiment(fishExperiment);
            stat.getFishGenotypeExpressionStatistics().addFishExperiment(fishExperiment);
        }

        return new ArrayList<>(statisticsMap.values());
    }

    private static void addPureFish(List<GenotypeFishResult> fishGenotypePhenotypeStatisticsList, List<Fish> fishList) {
        if (CollectionUtils.isEmpty(fishList)) {
            return;
        }
        if (fishGenotypePhenotypeStatisticsList == null) {
            fishGenotypePhenotypeStatisticsList = new ArrayList<>(fishList.size());
        }
        for (Fish fish : fishList) {
            GenotypeFishResult result = new GenotypeFishResult(fish);
            if (!fishGenotypePhenotypeStatisticsList.contains(result)) {
                fishGenotypePhenotypeStatisticsList.add(result);
            }
        }
    }

}
