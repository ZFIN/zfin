package org.zfin.fish.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
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
import org.zfin.fish.presentation.MartFish;
import org.zfin.fish.presentation.PhenotypeSummaryCriteria;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.Construct;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Service class to provide methods for retrieving fish records from the data warehouse.
 */
public class FishService {

    private static Logger logger = Logger.getLogger(FishService.class);

    private MartFish fish;

    public FishService(MartFish fish) {
        this.fish = fish;
    }

    public static FishSearchResult getFish(FishSearchCriteria criteria) {

        FishSearchResult results = new FishSearchResult();

        SolrQuery query = generateFishSearchSolrQuery(criteria);

        SolrServer solrServer = SolrService.getSolrServer("prototype");
        QueryResponse response = new QueryResponse();
        try {
            response = solrServer.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        List<FishResult> solrSearchResults = response.getBeans(FishResult.class);

        for (FishResult fishResult : solrSearchResults) {
            Fish fish = getFish(fishResult.getId());
            if (fish != null) {
                fishResult.setFish(fish);
                fishResult.setFeatureGenes(getFeatureGenes(fish));
                addFigures(fishResult, criteria);

            }


        }
        results.setResultsFound((int) response.getResults().getNumFound());
        results.setResults(solrSearchResults);

        return results;


    }

    private static SolrQuery generateFishSearchSolrQuery(FishSearchCriteria criteria) {
        SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();

        query.addFilterQuery("category:Fish");

        //the main query box, should probably be just matching against a subset of the record
        query.setQuery(criteria.getGeneOrFeatureNameCriteria().getValue());

        //results per page
        query.setRows(criteria.getRows());

        //page
        query.setStart(criteria.getStart());

        if (criteria.getExcludeSequenceTargetingReagentCriteria().isTrue()) {
            query.addFilterQuery("-" + FieldName.SEQUENCE_TARGETING_REAGENT.getName() + ":[* TO *]");
        }

        if (criteria.getRequireSequenceTargetingReagentCriteria().isTrue()) {
            query.addFilterQuery(FieldName.SEQUENCE_TARGETING_REAGENT.getName() + ":[* TO *]");
        }

        if (criteria.getExcludeTransgenicsCriteria().isTrue()) {
            query.addFilterQuery("-" + FieldName.CONSTRUCT.getName() + ":[* TO *]");
        }

        if (criteria.getRequireTransgenicsCriteria().isTrue()) {
            query.addFilterQuery(FieldName.CONSTRUCT.getName() + ":[* TO *]");
        }

        if (criteria.getMutationTypeCriteria().hasValues()) {
            //todo: implement me!
        }

        if (criteria.getPhenotypeAnatomyCriteria().hasValues()) {
            for (String term : criteria.getPhenotypeAnatomyCriteria().getNames()) {
                query.addFilterQuery(FieldName.AFFECTED_ANATOMY_TF.getName() + ":\"" + term + "\"");
            }
        }

        return query;
    }


    public static List<FeatureGene> getFeatureGenes(Fish fish) {
        List<FeatureGene> featureGenes = new ArrayList<>();
        List<Feature> features = new ArrayList<>();

        if (fish == null) { return null; }

        if (fish.getGenotype() != null) {
            for (GenotypeFeature genotypeFeature : fish.getGenotype().getGenotypeFeatures()) {
                features.add(genotypeFeature.getFeature());
            }
        }

        for (Feature feature : features) {
            for (Marker gene : feature.getAffectedGenes()) {
                if (feature.getConstructs() == null) {
                    FeatureGene featureGene = new FeatureGene();
                    featureGene.setFeature(feature);
                    featureGene.setGene(gene);
                    featureGenes.add(featureGene);
                } else {
                    for (FeatureMarkerRelationship fmr : feature.getConstructs()) {
                        FeatureGene featureGene = new FeatureGene();
                        featureGene.setFeature(feature);
                        featureGene.setGene(gene);
                        featureGene.setConstruct(fmr.getMarker());
                        featureGenes.add(featureGene);
                    }
                }

            }
        }

        for (Marker str : fish.getStrList()) {
            Set<MarkerRelationship> mrels = str.getFirstMarkerRelationships();
            for (MarkerRelationship mrel : mrels) {
                if (mrel.getMarkerRelationshipType().equals(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE)) {
                    FeatureGene featureGene = new FeatureGene();
                    featureGene.setSequenceTargetingReagent(str);
                    featureGene.setGene(mrel.getSecondMarker());
                    featureGenes.add(featureGene);
                }
            }
        }

        return featureGenes;
    }

    private static void addFigures(FishResult fishResult, FishSearchCriteria criteria) {
        if (criteria == null)
            addAllFigures(fishResult);
        else {
            List<String> values = criteria.getPhenotypeAnatomyCriteria().getValues();
            addFiguresByTermValues(fishResult, values);
        }
    }

    private static void addAllFigures(FishResult fishResult) {
        Set<ZfinFigureEntity> figures = RepositoryFactory.getFishRepository().getAllFigures(fishResult.getFish().getZdbID());
        setImageAttributeOnFish(fishResult, figures);
    }

    private static void addFiguresByTermValues(FishResult fishResult, List<String> values) {
        Set<ZfinFigureEntity> figures = RepositoryFactory.getFishRepository().getFiguresByFishAndTerms(fishResult.getFish().getZdbID(), values);
        setImageAttributeOnFish(fishResult, figures);
    }

    private static void setImageAttributeOnFish(FishResult fishResult, Set<ZfinFigureEntity> figures) {

        if (figures == null || figures.size() == 0)
            return;
        fishResult.setPhenotypeFigures(figures);
        for (ZfinFigureEntity figure : figures) {
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
        Set<ZfinFigureEntity> zfinFigureEntities = getFishRepository().getFiguresByFishAndTerms(fishID, criteria.getPhenotypeAnatomyCriteria().getValues());
        if (zfinFigureEntities == null)
            return null;

        List<FigureSummaryDisplay> figureSummaryDisplays = new ArrayList<>(zfinFigureEntities.size());
        Set<Figure> figures = new HashSet<>(zfinFigureEntities.size());
        for (ZfinFigureEntity figureEntity : zfinFigureEntities) {
            FigureSummaryDisplay figureSummaryDisplay = new FigureSummaryDisplay();
            Figure figure = getPublicationRepository().getFigure(figureEntity.getID());
            if (figures.add(figure)) {
                figureSummaryDisplay.setFigure(figure);
                figureSummaryDisplay.setPublication(figure.getPublication());
                List<PhenotypeStatement> phenotypeStatements = FishService.getPhenotypeStatements(figure, fishID);
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
        Fish fish = getFishRepository().getFish(fishID);
        PhenotypeSummaryCriteria criteria = new PhenotypeSummaryCriteria();
        criteria.setFish(fish);
        //todo: implement me!
//        criteria.setFishExperiments(fish.getFishExperiments());
        return criteria;
    }


    /**
     * Retrieve all distinct phenotype statements from a given phenotype statement list.
     *
     * @param phenotypeStatementList phenotypeStatementList
     * @return list of phenotype statements
     */
    public static List<PhenotypeStatement> getDistinctPhenotypeStatements(List<PhenotypeStatement> phenotypeStatementList) {
        if (phenotypeStatementList == null)
            return null;

        List<PhenotypeStatement> phenotypeStatements = new ArrayList<>(phenotypeStatementList.size());
        for (PhenotypeStatement phenotypeStatement : phenotypeStatementList) {
            if (isDistinctPhenotype(phenotypeStatement, phenotypeStatements))
                phenotypeStatements.add(phenotypeStatement);
        }
        Collections.sort(phenotypeStatements);
        return phenotypeStatements;
    }

    private static boolean isDistinctPhenotype(PhenotypeStatement phenotypeStatement, List<PhenotypeStatement> phenotypeStatements) {
        if (phenotypeStatements == null)
            return true;
        for (PhenotypeStatement phenoStatement : phenotypeStatements) {
            if (phenoStatement.equalsByName(phenotypeStatement))
                return false;
        }
        return true;
    }

    public static Fish getFish(String zdbID) {
        return getFishRepository().getFish(zdbID);
    }

    public static FishSearchCriteria getFishSearchCriteria(FishSearchFormBean bean) {
        return new FishSearchCriteria(bean);
    }

    public static String getGenotypeExperimentIDsString(String fishID) {
        if (fishID == null)
            return null;
        String[] ids = fishID.split(",");
        String returnId = null;
        for (String id : ids)
            if (ActiveData.isValidActiveData(id, ActiveData.Type.GENOX)) {
                if (returnId == null)
                    returnId = id + ",";
                else
                    returnId += id + ",";
            }
        if (returnId == null)
            return null;
        return returnId.substring(0, returnId.length() - 1);
    }

    public static String getGenotypeID(String fishID) {
        if (fishID == null)
            return null;
        String[] ids = fishID.split(",");
        String returnId = null;
        for (String id : ids)
            if (ActiveData.validateID(id).equals(ActiveData.Type.GENO)) {
                if (returnId == null)
                    returnId = id;
                else
                    throw new RuntimeException("Found more than one GENO id: " + fishID);

            }
        return returnId;
    }

    public static List<PhenotypeStatement> getPhenotypeStatements(Figure figure, String fishID) {
        return RepositoryFactory.getPhenotypeRepository().getPhenotypeStatements(figure, fishID);
    }

    public static List<String> getFishOxIds(String fishID) {
       // return getFish(fishID).getGenotypeExperimentIDs();
        Set<FishExperiment> fishOx = getMutantRepository().getFish(fishID).getFishExperiments();
        List<String>fishoxID=new ArrayList<>(fishOx.size());
        for (FishExperiment genoID : getMutantRepository().getFish(fishID).getFishExperiments()) {
            fishoxID.add(genoID.getZdbID());
        }
        return fishoxID;
    }

    /**
     * Retrieve the longest genotype experiment group id for all fish
     *
     * @return String
     */
    public static String getGenoxMaxLength() {
        return RepositoryFactory.getFishRepository().getGenoxMaxLength();
    }

    /**
     * Retrieve Summary of expression statements, figures and pub info
     *
     * @param fishID fish ID
     * @return list of expression statements records grouped by figure.
     */
    public static List<FigureExpressionSummary> getExpressionSummary(String fishID) {
        return getExpressionSummary(fishID, null);
    }

    /**
     * Retrieve Summary of expression statements, figures and pub info
     *
     * @param fishID fish ID
     * @param geneID gene ID
     * @return list of expression statements records grouped by figure.
     */
    public static List<FigureExpressionSummary> getExpressionSummary(String fishID, String geneID) {

        String genotypeID = getGenotypeID(fishID);
        //List<String> genoxIds = getGenoxIds(fishID);
        Fish fish=getMutantRepository().getFish(fishID);
        Set<FishExperiment> fishOx = fish.getFishExperiments();



        List<ExpressionResult> results = getMutantRepository().getExpressionSummary(fishOx, geneID);
        if (CollectionUtils.isEmpty(results))
            return null;

        return ExpressionService.createExpressionFigureSummaryFromExpressionResults(results);
    }

    /**
     * Check if a given fish has expression data with at least one figure that has an image.
     *
     * @param fishID fish ID
     * @return
     */
    public static boolean hasImagesOnExpressionFigures(String fishID) {
        String genotypeID = getGenotypeID(fishID);
        Set<FishExperiment> fishOx = getMutantRepository().getFish(fishID).getFishExperiments();


        return getMutantRepository().hasImagesOnExpressionFigures(genotypeID, fishOx);
    }


    public static Integer getCitationCount(Fish fish) {
        return getFishPublications(fish).size();
    }

    public static Integer getCitationCount(MartFish fish) {
        return RepositoryFactory.getMutantRepository().getGenoxAttributions(fish.getGenotypeExperimentIDs()).size();
    }

    public static Set<Publication> getFishPublications(Fish fish) {
        List<Publication> pubs = RepositoryFactory.getMutantRepository().getFishAttributionList(fish);
        Set<Publication> publicationSet = new HashSet<>(pubs.size());
        publicationSet.addAll(pubs);
        return publicationSet;
    }

    public static List<Marker> getAffectedGenes(Fish fish) {
        List<SequenceTargetingReagent> strList = fish.getStrList();
        if (CollectionUtils.isEmpty(strList))
            return null;
        List<Marker> geneList = new ArrayList<>(strList.size());
        Set<Marker> affectedMarkerOnGenotype = GenotypeService.getAffectedMarker(fish.getGenotype());
        if (affectedMarkerOnGenotype != null)
            geneList.addAll(affectedMarkerOnGenotype);
        for (SequenceTargetingReagent str : strList)
            geneList.addAll(str.getTargetGenes());
        return geneList;
    }

}
