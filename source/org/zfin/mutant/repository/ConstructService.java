package org.zfin.mutant.repository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.expression.*;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.Construct;
import org.zfin.mutant.presentation.ConstructSearchFormBean;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Service class to provide methods for retrieving fish records from the data warehouse.
 */
public class ConstructService {

    private Marker construct;
    private int numberOfPublications;

    public int getNumberOfPublications() {
        return numberOfPublications;
    }

    public void setNumberOfPublications(int numberOfPublications) {
        this.numberOfPublications = numberOfPublications;
    }

    public List<FigureSummaryDisplay> getFigureSummary() {
        return figureSummary;
    }

    public void setFigureSummary(List<FigureSummaryDisplay> figureSummary) {
        this.figureSummary = figureSummary;
    }


    private List<FigureSummaryDisplay> figureSummary;

    public ConstructService(Marker construct) {
        if (construct == null) {
            throw new RuntimeException("No construct object provided");
        }
        this.construct = construct;
    }

    public Marker getConstruct() {
        return construct;
    }

    public void setConstruct(Marker construct) {
        this.construct = construct;
    }

    public static ConstructSearchResult getConstruct(ConstructSearchCriteria criteria) {
        return RepositoryFactory.getConstructRepository().getConstructs(criteria);
    }


    public static Construct getConstruct(String constructID) {
        return getConstructRepository().getConstruct(constructID);
    }

    public static ConstructSearchCriteria getConstructSearchCriteria(ConstructSearchFormBean bean) {
        return new ConstructSearchCriteria(bean);
    }

    /**
     * fishIDs are composed of 0..* genox ids and zero or one geno ID
     * The returned fish contains the components.
     *
     * @param fishID fish id
     * @return genox ids and geno id
     */

    public static List<PhenotypeStatementWarehouse> getPhenotypeStatements(Figure figure, String fishID) {
        return RepositoryFactory.getPhenotypeRepository().getPhenotypeStatements(figure, fishID);
    }

    public static List<Fish> getFishByFigureConstruct(Figure figure, String constructID) {
        return getConstructRepository().getFishByFigureConstruct(figure, constructID);
    }


    /**
     * Retrieve the longest genotype experiment group id for all fish
     *
     * @return String
     */


    public void createFigureSummary(ConstructSearchCriteria criteria, String constructID) {
        Set<Publication> publications = new HashSet<>();

        List<FigureSummaryDisplay> summaryRows = getExpressionSummary(constructID, criteria);

        for (FigureSummaryDisplay summaryRow : summaryRows) {
            publications.add(summaryRow.getPublication());
        }

        setNumberOfPublications(publications.size());
        setFigureSummary(summaryRows);
    }

    public String getNumberOfFiguresDisplay() {
        int numberOfFigures = figureSummary.size();
        return numberOfFigures + " " + AnatomyLabel.figureChoice.format(numberOfFigures);
    }

    public String getNumberOfPublicationsDisplay() {
        return numberOfPublications + " " + AnatomyLabel.publicationChoice.format(numberOfPublications);
    }

    public static List<FigureSummaryDisplay> getExpressionSummary(String constructID, ConstructSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new ConstructSearchCriteria();
            criteria.setPhenotypeAnatomyCriteria(new SearchCriterion(SearchCriterionType.PHENOTYPE_ANATOMY_ID, true));
        }
        Map<String, FigureSummaryDisplay> map = new HashMap<>();

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getConstructRepository().getFiguresByConstructAndTerms(constructID, criteria.getPhenotypeAnatomyCriteria().getValues());
        List<Figure> figures = new ArrayList<>(zfinFigureEntities.size());
        for (ZfinFigureEntity figureEntity : zfinFigureEntities) {
            Figure figure = getPublicationRepository().getFigure(figureEntity.getID());
            figures.add(figure);
        }

        for (Figure figure : figures) {
            Publication pub = figure.getPublication();
            String key = pub.getZdbID() + figure.getZdbID();

            // if the key is not in the map, instantiate a display object and add it to the map
            // otherwise, get the display object from the map
            if (!map.containsKey(key)) {
                FigureSummaryDisplay figureData = new FigureSummaryDisplay();
                figureData.setPublication(pub);
                figureData.setFigure(figure);
                List<Fish> fishList = getFishByFigureConstruct(figure, constructID);
                figureData.setFishList(fishList);
                figureData.setExpressionStatementList(getFigureExpressionStatementList(figure, criteria, fishList));

                for (Image img : figure.getImages()) {
                    if (figureData.getThumbnail() == null) {
                        figureData.setThumbnail(img.getThumbnail());
                    }
                }

                map.put(key, figureData);
            }
        }

        List<FigureSummaryDisplay> summaryRows = new ArrayList<>();
        if (map.values().size() > 0) {
            summaryRows.addAll(map.values());
        }
        Collections.sort(summaryRows);
        return summaryRows;
    }

    public static List<ExpressionStatement> getFigureExpressionStatementList(Figure figure, ConstructSearchCriteria expressionCriteria, List<Fish> fishList) {
        //work with a clone of the original criteria, so that it doesn't get screwed up.
        ConstructSearchCriteria clone = expressionCriteria.clone();
        //set the figure we're limiting to
        clone.setFigure(figure);
        clone.setFishList(fishList);

        //unset the entities, since we want all terms that match all of the other criteria
        clone.setEntity(null);
        clone.setSingleTermEitherPosition(null);
        Set<ExpressionStatement> expressionResultSet = getExpressionStatements(clone);
        List<ExpressionStatement> expressionStatementList = new ArrayList<>();
        expressionStatementList.addAll(expressionResultSet);
        return expressionStatementList;

    }


    public static Set<ExpressionStatement> getExpressionStatements(ConstructSearchCriteria expressionCriteria) {
        Set<ExpressionResult> results = new HashSet<>();
        Set<ExpressionStatement> expressionStatements = new TreeSet<>();
        Session session = HibernateUtil.currentSession();

        //store strings for createAlias here, then only create what's necessary at the end.
        Map<String, String> aliasMap = new HashMap<>();

        Criteria criteria = session.createCriteria(ExpressionResult.class);
        Criteria figureCriteria = null;
        //duplicate createAlias statements are ok, so we'll put the necessary
        //aliases in for any set of restrictions.

        if (expressionCriteria.getFigure() != null) {
            //if there were hql, I would do member of..
            figureCriteria = criteria.createCriteria("figures", "figure");
            figureCriteria.add(Restrictions.eq("zdbID", expressionCriteria.getFigure().getZdbID()));

        }
        if (expressionCriteria.getGenos() != null) {
            aliasMap.put("xpatex.fishExperiment", "fishox");
            criteria.add(Restrictions.in("fishox.fish", expressionCriteria.getFishList()));

        }

        aliasMap.put("expressionExperiment", "xpatex");
        aliasMap.put("xpatex.fishExperiment", "fishox");
        criteria.add(Restrictions.eq("fishox.standardOrGenericControl", true));


        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            criteria.createAlias(entry.getKey(), entry.getValue());
        }

        //logger.debug("getting terms for an ExpressionSummaryCriteria object");
        results.addAll(criteria.list());
        for (ExpressionResult result : results) {
            ExpressionStatement statement = new ExpressionStatement();
            statement.setEntity(result.getEntity());
            statement.setExpressionFound(result.isExpressionFound());
            expressionStatements.add(statement);
        }

        return expressionStatements;
    }

    public static Experiment getExperiment(Figure figure, ConstructSearchCriteria expressionCriteria) {
        ConstructSearchCriteria clone = expressionCriteria.clone();

        //set the figure we're limiting to
        clone.setFigure(figure);

        Set<ExpressionResult> results = new HashSet<>();
        Experiment exp = new Experiment();
        Session session = HibernateUtil.currentSession();

        //store strings for createAlias here, then only create what's necessary at the end.
        Map<String, String> aliasMap = new HashMap<>();

        Criteria criteria = session.createCriteria(ExpressionResult.class);
        Criteria figureCriteria = null;
        //duplicate createAlias statements are ok, so we'll put the necessary
        //aliases in for any set of restrictions.

        if (expressionCriteria.getFigure() != null) {
            //if there were hql, I would do member of..
            if (figureCriteria == null) {
                figureCriteria = criteria.createCriteria("figures", "figure");
            }
            figureCriteria.add(Restrictions.eq("zdbID", expressionCriteria.getFigure().getZdbID()));

        }
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            criteria.createAlias(entry.getKey(), entry.getValue());
        }

        //logger.debug("getting terms for an ExpressionSummaryCriteria object");
        results.addAll(criteria.list());
        Experiment experiment = null;
        for (ExpressionResult result : results) {
            if (!result.getExpressionExperiment().getFishExperiment().getExperiment().isChemical()) {
                experiment = result.getExpressionExperiment().getFishExperiment().getExperiment();
            }
        }

        return experiment;


    }


    public static boolean hasImagesOnExpressionFigures(String constructID) {

        /*List<String> genoxIds = getGenoxIds(constructID);
        return getMutantRepository().hasImagesOnExpressionFigures("", genoxIds);*/
        return true;
    }

    public static List<String> getGenoxIds(String constructID) {
        return getConstruct(constructID).getGenotypeExperimentIDs();
    }


}
