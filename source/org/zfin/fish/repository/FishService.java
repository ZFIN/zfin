package org.zfin.fish.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.expression.Figure;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.PhenotypeSummaryCriteria;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Service class to provide methods for retrieving fish records from the data warehouse.
 */
public class FishService {

    private Fish fish;

    public FishService(Fish fish) {
        this.fish = fish;
    }

    public static FishSearchResult getFish(FishSearchCriteria criteria) {
        return RepositoryFactory.getFishRepository().getFish(criteria);
    }


    public static List<FigureSummaryDisplay> getPhenotypeSummary(String fishID, FishSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new FishSearchCriteria();
            criteria.setPhenotypeAnatomyCriteria(new SearchCriterion(SearchCriterionType.PHENOTYPE_ANATOMY_ID, true));
        }
        Set<ZfinFigureEntity> zfinFigureEntities = getFishRepository().getFiguresByFishAndTerms(fishID, criteria.getPhenotypeAnatomyCriteria().getValues());
        if (zfinFigureEntities == null)
            return null;

        List<FigureSummaryDisplay> figureSummaryDisplays = new ArrayList<FigureSummaryDisplay>(zfinFigureEntities.size());
        Set<Figure> figures = new HashSet<Figure>(zfinFigureEntities.size());
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
        List<GenotypeExperiment> genotypeExperiments = new ArrayList<GenotypeExperiment>(fish.getGenotypeExperimentIDs().size());
        for (String genoID : fish.getGenotypeExperimentIDs()) {
            genotypeExperiments.add(getMutantRepository().getGenotypeExperiment(genoID));
        }
        criteria.setGenotypeExperiments(genotypeExperiments);
        return criteria;
    }

    public static List<Genotype> getGenotypes(long fishID) {
        Fish fish = getFishRepository().getFish(fishID);
        List<GenotypeExperiment> genotypeExperiments = new ArrayList<GenotypeExperiment>(fish.getGenotypeExperimentIDs().size());
        List<Genotype> genotype = new ArrayList<Genotype>();
        for (String genoID : fish.getGenotypeExperimentIDs()) {
            genotype.add(getMutantRepository().getGenotypeExperiment(genoID).getGenotype());
        }
        return genotype;
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

        List<PhenotypeStatement> phenotypeStatements = new ArrayList<PhenotypeStatement>(phenotypeStatementList.size());
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

    public static Fish getFish(String fishID) {
        return getFishRepository().getFish(fishID);
    }

    public static FishSearchCriteria getFishSearchCriteria(FishSearchFormBean bean) {
        return new FishSearchCriteria(bean);
    }

    /**
     * fishIDs are composed of 0..* genox ids and zero or one geno ID
     * The returned fish contains the components.
     *
     * @param fishID fish id
     * @return genox ids and geno id
     */
    public static Fish getGenoGenoxByFishID(String fishID) {
        if (StringUtils.isEmpty(fishID))
            return null;
        Fish fish = new Fish();
        String[] ids = fishID.split(",");
        List<String> genoxIds = new ArrayList<String>(3);
        // compile the genotype experiment ids
        for (String id : ids) {
            ActiveData.Type type = ActiveData.validateID(id);
            if (type == ActiveData.Type.GENOX) {
                genoxIds.add(id);
            }
            if (type == ActiveData.Type.GENO) {
                ZfinEntity genotype = new ZfinEntity();
                genotype.setID(id);
                fish.setGenotype(genotype);
            }
        }
        if (CollectionUtils.isNotEmpty(genoxIds)) {
            fish.setGenotypeExperimentIDs(genoxIds);
            String genoxIdString = "";
            for (String id : genoxIds) {
                genoxIdString += id;
                genoxIdString += ",";
            }
            genoxIdString = genoxIdString.substring(0, genoxIdString.length() - 1);
            fish.setGenotypeExperimentIDsString(genoxIdString);
        }
        return fish;
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

    public static List<String> getGenoxIds(String fishID) {
       return getFish(fishID).getGenotypeExperimentIDs();
    }

    /**
     * Retrieve the longest genotype experiment group id for all fish
     * @return String
     */
    public static String getGenoxMaxLength() {
        return RepositoryFactory.getFishRepository().getGenoxMaxLength();
    }
}
