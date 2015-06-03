package org.zfin.antibody.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.springframework.stereotype.Repository;
import org.zfin.Species;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.AllMarkerNamesFastSearch;
import org.zfin.infrastructure.AllNamesFastSearch;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.isNotEmpty;


/**
 * Hibernate implementation of the Antibody Repository.
 */
@Repository
public class HibernateAntibodyRepository implements AntibodyRepository {

    private AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();

    // These attributes are cashed for performance reasons
    // They are static, i.e. they do not change all that often.
    // To update the list you need to restart Tomcat or we can have an
    // update at runtime.
    private List<Species> immunogenSpeciesList;
    private List<Species> hostSpeciesList;


    public Antibody getAntibodyByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Antibody) session.get(Antibody.class, zdbID);
    }

    /**
     * Retrieve an antibody by ID for displaying on the detail page.
     * Pre-populate most of its attributes and collections for speed.
     *
     * @param zdbID primary key
     * @return antibody
     */
    public Antibody getAntibodyByZdbID(String zdbID) {
        Criteria query = HibernateUtil.currentSession().createCriteria(Antibody.class);
        query.add(Restrictions.eq("zdbID", zdbID));
        return (Antibody) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<Antibody> getAntibodies(AntibodySearchCriteria searchCriteria) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("select distinct antibody ");
        hql.append(getAntibodiesByNameAndLabelingQueryBlock(searchCriteria));
        hql.append("order by antibody.abbreviationOrder");
        Query query = session.createQuery(hql.toString());
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.CONTAINS)
                query.setParameter("name", "%" + searchCriteria.getName().toLowerCase() + "%");
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.BEGINS)
                query.setParameter("name", searchCriteria.getName().toLowerCase() + "%");
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (searchCriteria.getAntigenNameFilterType() == FilterType.CONTAINS)
                query.setParameter("markerName", "%" + searchCriteria.getAntigenGeneName().toLowerCase().trim() + "%");
            if (searchCriteria.getAntigenNameFilterType() == FilterType.BEGINS)
                query.setParameter("markerName", searchCriteria.getAntigenGeneName().toLowerCase().trim() + "%");
            query.setParameterList("genePrecedence", getGenePrecedenceInClause());
        }
        if (searchCriteria.isHostSpeciesDefined())
            query.setParameter("hostSpecies", searchCriteria.getHostSpecies());
        if (searchCriteria.isAssaySearch())
            query.setParameter("assay", searchCriteria.getAssay());
        if (searchCriteria.isZircOnly())
            query.setParameter("zircLabID", "ZDB-LAB-991005-53");
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType()))
            query.setParameter("antibodyType", searchCriteria.getClonalType());
        if (searchCriteria.isStageDefined())
            bindStageFilterValues(searchCriteria, query);
        if (searchCriteria.isAnatomyDefined()) {
            applyAnatomyTermsFilter(searchCriteria, query);
        }
        int start = 0;
        if (searchCriteria.getPaginationBean() != null)
            start = searchCriteria.getPaginationBean().getFirstRecord() - 1;
        PaginationResult<Antibody> antibodyObjects = PaginationResultFactory.createResultFromScrollableResultAndClose(start, start + searchCriteria.getPaginationBean().getMaxDisplayRecordsInteger(), query.scroll());

        return antibodyObjects;
    }

    private List<String> getGenePrecedenceInClause() {
        AllNamesFastSearch.Precedence[] genePrecedence = AllNamesFastSearch.getGenePrecedences();
        if (genePrecedence == null)
            return null;
        List<String> list = new ArrayList<>(genePrecedence.length);
        for (AllMarkerNamesFastSearch.Precedence precedence : genePrecedence) {
            list.add(precedence.toString());
        }
        return list;
    }

    public int getNumberOfAntibodies(AntibodySearchCriteria searchCriteria) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("select count(distinct antibody)");
        hql.append(getAntibodiesByNameAndLabelingQueryBlock(searchCriteria));
        Query query = session.createQuery(hql.toString());
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            // antibody name and abbreviation is the same
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.CONTAINS)
                query.setParameter("name", "%" + searchCriteria.getName().toLowerCase() + "%");
            else if (searchCriteria.getAntibodyNameFilterType() == FilterType.BEGINS)
                query.setParameter("name", searchCriteria.getName().toLowerCase() + "%");
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (searchCriteria.getAntigenNameFilterType() == FilterType.CONTAINS)
                query.setParameter("markerName", "%" + searchCriteria.getAntigenGeneName().toLowerCase() + "%");
            else if (searchCriteria.getAntigenNameFilterType() == FilterType.BEGINS)
                query.setParameter("markerName", searchCriteria.getAntigenGeneName().toLowerCase() + "%");
            query.setParameterList("genePrecedence", getGenePrecedenceInClause());
        }
        if (searchCriteria.isHostSpeciesDefined())
            query.setParameter("hostSpecies", searchCriteria.getHostSpecies());
        if (searchCriteria.isAssaySearch())
            query.setParameter("assay", searchCriteria.getAssay());
        if (searchCriteria.isZircOnly())
            query.setParameter("zircLabID", "ZDB-LAB-991005-53");
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType()))
            query.setParameter("antibodyType", searchCriteria.getClonalType());
        if (searchCriteria.isStageDefined())
            bindStageFilterValues(searchCriteria, query);
        if (searchCriteria.isAnatomyDefined()) {
            applyAnatomyTermsFilter(searchCriteria, query);
        }
        return ((Number) query.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Species> getHostSpeciesList() {
        if (hostSpeciesList != null)
            return hostSpeciesList;

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Species.class);
        criteria.add(eq("antibodyHost", true));
        criteria.addOrder(Order.asc("displayOrder"));
        hostSpeciesList = criteria.list();
        return hostSpeciesList;
    }

    @SuppressWarnings("unchecked")
    public List<Species> getUsedHostSpeciesList() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Species.class, "species");
        criteria.add(eq("antibodyHost", true));
        criteria.addOrder(Order.asc("displayOrder"));
        DetachedCriteria antibodies = DetachedCriteria.forClass(Antibody.class, "ab");
        antibodies.add(Restrictions.eqProperty("ab.hostSpecies", "species.commonName"));
        antibodies.setProjection(Property.forName("ab.zdbID").count());
        criteria.add(Subqueries.lt((long) 0, antibodies));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<Species> getImmunogenSpeciesList() {
        if (immunogenSpeciesList != null)
            return immunogenSpeciesList;

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Species.class);
        criteria.add(eq("antibodyImmunogen", true));
        criteria.addOrder(Order.asc("displayOrder"));
        immunogenSpeciesList = criteria.list();
        return immunogenSpeciesList;
    }


    public int getAntibodiesByAOTermCount(GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.setProjection(Projections.countDistinct("zdbID"));
        Criteria labeling = criteria.createCriteria("antibodyLabelings");
        Criteria fishExperiment = labeling.createCriteria("fishExperiment");
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria results = labeling.createCriteria("expressionResults");
        // check AO1 and AO2 
        results.add(Restrictions.or(
                Restrictions.eq("entity.superterm", aoTerm),
                Restrictions.eq("entity.subterm", aoTerm)));
        results.add(eq("expressionFound", true));
        Criteria experiment = fishExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));

        return ((Long) results.list().get(0)).intValue();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<Antibody> getAntibodiesByAOTerm(GenericTerm aoTerm, PaginationBean paginationBean, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();
        StringBuffer hql = new StringBuffer();
        hql.append("select distinct antibody from Antibody antibody, ExpressionExperiment expExp,  ");
        hql.append("                ExpressionResult res, FishExperiment fishox, Genotype geno, ");
        hql.append("                Experiment exp ");
        hql.append("where ");
        hql.append("       expExp.antibody = antibody ");
        hql.append("       and res.expressionExperiment = expExp ");
        hql.append("       and fishox = expExp.fishExperiment ");
        hql.append("       and geno = fishox.fish.genotype ");
        hql.append("       and geno.wildtype = :wildType ");
        if (includeSubstructures) {
            hql.append("   and ( res.entity.superterm = :aoTerm  OR res.entity.subterm = :aoTerm" +
                    "                                       OR exists ( select 1 from TransitiveClosure child " +
                    "                  where res.entity.superterm = child.child AND child.root = :aoTerm ) " +
                    "                                       OR exists ( select 1 from TransitiveClosure child " +
                    "                  where res.entity.subterm = child.child AND child.root = :aoTerm ) " +
                    ") ");
        } else
            hql.append("       and (res.entity.superterm = :aoTerm OR res.entity.subterm = :aoTerm) ");
        hql.append("       and res.expressionFound = :expressionFound ");
        hql.append("       and exp = fishox.experiment ");
        hql.append("       and exp.name in (:standard , :generic ) ");
        hql.append("order by antibody.abbreviationOrder");
        Query query = session.createQuery(hql.toString());
        query.setParameter("wildType", true);
        query.setParameter("aoTerm", aoTerm);
        query.setParameter("expressionFound", true);
        query.setParameter("standard", Experiment.STANDARD);
        query.setParameter("generic", Experiment.GENERIC_CONTROL);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean, query.scroll());
    }

    public int getNumberOfFiguresPerAoTerm(Antibody antibody, GenericTerm aoTerm, Figure.Type figureType) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria;
        if (figureType != null && figureType == Figure.Type.TOD)
            criteria = session.createCriteria(TextOnlyFigure.class);
        else
            criteria = session.createCriteria(FigureFigure.class);
        criteria.setProjection(Projections.countDistinct("zdbID"));
        Criteria results = criteria.createCriteria("expressionResults");
        // check AO1 and AO2
        results.add(Restrictions.or(
                Restrictions.eq("entity.superterm", aoTerm),
                Restrictions.eq("entity.subterm", aoTerm)));
        results.add(eq("expressionFound", true));
        Criteria labeling = results.createCriteria("expressionExperiment");
        labeling.add(eq("antibody", antibody));
        Criteria fishExperiment = labeling.createCriteria("fishExperiment");
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = fishExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD}));
        return ((Long) criteria.list().get(0)).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresPerAoTerm(Antibody antibody, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Figure.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        Criteria results = criteria.createCriteria("expressionResults");
        // check AO1 and AO2
        results.add(Restrictions.or(
                Restrictions.eq("entity.superterm", aoTerm),
                Restrictions.eq("entity.subterm", aoTerm)));
        results.add(eq("expressionFound", true));
        Criteria labeling = results.createCriteria("expressionExperiment");
        labeling.add(eq("antibody", antibody));
        Criteria fishExperiment = labeling.createCriteria("fishExperiment");
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = fishExperiment.createCriteria("experiment");
        experiment.add(Restrictions.eq("name", Experiment.STANDARD));

        return (List<Figure>) labeling.list();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<Publication> getPublicationsWithFigures(Antibody antibody, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria pubs = session.createCriteria(Publication.class);
        Criteria labeling = pubs.createCriteria("expressionExperiments");
        labeling.add(eq("antibody", antibody));
        Criteria results = labeling.createCriteria("expressionResults");
        // check AO1 and AO2
        results.add(Restrictions.or(
                Restrictions.eq("entity.superterm", aoTerm),
                Restrictions.eq("entity.subterm", aoTerm)));
        results.add(isNotEmpty("figures"));
        results.add(eq("expressionFound", true));
        Criteria fishExperiment = labeling.createCriteria("fishExperiment");
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = fishExperiment.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<>((List<Publication>) pubs.list());
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAntibodiesByPublication(Publication publication) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct antibody from Antibody as antibody, PublicationAttribution pubAttribute " +
                "            where pubAttribute.publication = :pub AND " +
                "            pubAttribute.dataZdbID = antibody.zdbID order by antibody.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setParameter("pub", publication);

        return (List<Antibody>) query.list();

    }

    public Antibody getAntibodyByAbbrev(String antibodyAbbrev) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.add(Restrictions.eq("abbreviation", antibodyAbbrev));
        return (Antibody) criteria.uniqueResult();
    }

    public Antibody getAntibodyByName(String antibodyName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.add(Restrictions.eq("name", antibodyName).ignoreCase());
        return (Antibody) criteria.uniqueResult();
    }

    private void applyAnatomyTermsFilter(AntibodySearchCriteria searchCriteria, Query query) {
        String[] termIDs = searchCriteria.getTermIDs();
        for (int i = 0; i < termIDs.length; i++) {
            query.setParameter("aoTermID_" + i, termIDs[i]);
        }
    }

    private void bindStageFilterValues(AntibodySearchCriteria searchCriteria, Query query) {
        DevelopmentStage startStageFilter = searchCriteria.getStartStage();
        DevelopmentStage ss = new DevelopmentStage();
        ss.setZdbID(startStageFilter.getZdbID());
        DevelopmentStage startStage = anatomyRepository.getStage(ss);
        query.setParameter("hoursStart", startStage.getHoursStart());
        DevelopmentStage endStageFilter = searchCriteria.getEndStage();
        DevelopmentStage end = new DevelopmentStage();
        end.setZdbID(endStageFilter.getZdbID());
        DevelopmentStage endStage = anatomyRepository.getStage(end);
        query.setParameter("hoursEnd", endStage.getHoursEnd());
    }

    /**
     * Uses the following parameters:
     * 1) abbrev
     * 2) host Species if not 'ANY" is set
     * 3) immunogen species if not 'ANY" is set
     *
     * @param searchCriteria antibody criteria
     * @return string
     */
    private String getAntibodiesByNameAndLabelingQueryBlock(AntibodySearchCriteria searchCriteria) {
        // Todo: need to do a better job!!!
        boolean hasOneWhereClause = false;
        StringBuilder hql = new StringBuilder(" from Antibody antibody ");

        if (searchCriteria.isZircOnly())
            hql.append(", MarkerSupplier markerSupplier ");
        if (searchCriteria.isAssaySearch() || searchCriteria.isAnatomyDefined())
            hql.append(", ExpressionExperiment experiment ");
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName()))
            hql.append(",  AbstractMarkerRelationshipInterface rel   ");
        if (!StringUtils.isEmpty(searchCriteria.getName()))
            hql.append(",  AllMarkerNamesFastSearch mapAntibody   ");
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName()))
            hql.append(",  AllMarkerNamesFastSearch mapGene   ");
        if (searchCriteria.isAnatomyDefined())
            hql.append(",  ExpressionTermFastSearch expressionTerm, ExpressionResult expressionResult ");

        if (searchCriteria.isAny())
            hql.append("where ");
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            hql.append(" mapAntibody.marker =  antibody AND mapAntibody.nameLowerCase like :name ");
            hasOneWhereClause = true;
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append("    rel.secondMarker = antibody ");
            hql.append("    AND mapGene.marker =  rel.firstMarker ");
            hql.append("    AND mapGene.nameLowerCase like :markerName ");
            hql.append("    AND mapGene.precedence in (:genePrecedence) ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isAssaySearch()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" experiment.assay.name = :assay AND experiment.antibody = antibody ");
            hasOneWhereClause = true;
        }
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType())) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" antibody.clonalType = :antibodyType ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isHostSpeciesDefined()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" antibody.hostSpecies = :hostSpecies ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isZircOnly()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hql.append(" markerSupplier.organization.zdbID = :zircLabID AND markerSupplier.marker = antibody ");
            hasOneWhereClause = true;
        }
        // only stage range defined
        if (searchCriteria.isStageDefined() && !searchCriteria.isAnatomyDefined()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            hasOneWhereClause = true;
            hql.append("  exists ( select result from ExpressionResult result " +
                    "                  where result.startStage.hoursStart >= :hoursStart " +
                    "                    AND result.endStage.hoursEnd <= :hoursEnd " +
                    "                    AND result.expressionExperiment.antibody = antibody ) ");
        }
        if (searchCriteria.isAnatomyDefined()) {
            if (hasOneWhereClause)
                hql.append(" AND ");
            //hql.append(" experiment.antibody = antibody AND ( ");
            hql.append("  ( ");
            int numberOfTerms = searchCriteria.getTermIDs().length;
            // handle the first term
            hql.append("     expressionTerm.term.zdbID = :aoTermID_0 " +
                    "                     AND experiment.antibody = antibody " +
                    "                     AND expressionTerm.expressionResult = expressionResult" +
                    "                     AND expressionResult.expressionExperiment = experiment ");
            if (searchCriteria.isStageDefined())
                hql.append("                  AND expressionResult.startStage.hoursStart >= :hoursStart " +
                        "                     AND expressionResult.endStage.hoursEnd <= :hoursEnd ");
            if (!searchCriteria.isIncludeSubstructures())
                hql.append("    AND expressionTerm.originalAnnotation = 't' ");

            if (numberOfTerms > 1) {
                for (int i = 1; i < numberOfTerms; i++) {
                    if (searchCriteria.isAnatomyEveryTerm())
                        hql.append(" AND ");
                    else
                        hql.append(" OR ");
                    hql.append(" exists (select expressionTerm from ExpressionTermFastSearch expressionTerm, " +
                            "ExpressionResult expressionResult2, ExpressionExperiment experiment2 " +
                            "   where " +
                            "       expressionTerm.term.zdbID = :aoTermID_" + i +
                            "                     AND expressionResult2.expressionExperiment = experiment2 " +
                            "                     AND experiment2.antibody = antibody " +
                            "                     AND expressionTerm.expressionResult = expressionResult2 ");
                    if (searchCriteria.isStageDefined())
                        hql.append("                  AND expressionResult2.startStage.hoursStart >= :hoursStart " +
                                "                     AND expressionResult2.endStage.hoursEnd <= :hoursEnd ");
                    if (!searchCriteria.isIncludeSubstructures())
                        hql.append("    AND expressionTerm.originalAnnotation = 't' ");
                    hql.append(" ) ");
                }
            }
            hql.append(") ");
        }

        return hql.toString();
    }

    private void setPaginationParameters(Query query, PaginationBean paginationBean) {
        if (paginationBean == null)
            return;
        query.setMaxResults(paginationBean.getMaxDisplayRecordsInteger());
        query.setFirstResult(paginationBean.getFirstRecord() - 1);
    }

    @SuppressWarnings("unchecked")
    public List<AllMarkerNamesFastSearch> getAllNameAntibodyMatches(String string) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AllMarkerNamesFastSearch.class);
        criteria.add(Restrictions.like("nameLowerCase", "%" + string + "%"));
        Criteria marker = criteria.createCriteria("marker");
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        MarkerType type = mr.getMarkerTypeByName(Marker.Type.ATB.toString());
        marker.add(Restrictions.eq("markerType", type));
        return (List<AllMarkerNamesFastSearch>) marker.list();

    }

    public List<AntibodyStatistics> getAntibodyStatistics(GenericTerm aoTerm, PaginationBean pagination, boolean includeSubstructures) {

        String hql;
        // loop over all antibodyAOStatistic records until the given number of distinct antibodies from the pagination
        // bean is reached.
        if (includeSubstructures)
            hql = "  from AntibodyAOStatistics stat fetch all properties" +
                    "     where stat.superterm = :aoterm";
        else
            hql = " select distinct stat, stat.antibody.abbreviation from AntibodyAOStatistics stat fetch all properties" +
                    "     where stat.superterm = :aoterm and " +
                    "           stat.subterm = :aoterm " +
                    "           order by stat.antibody.abbreviation";

        ScrollableResults scrollableResults = HibernateUtil.currentSession().createQuery(hql)
                .setParameter("aoterm", aoTerm).scroll();
        List<AntibodyStatistics> list = new ArrayList<AntibodyStatistics>();

        while (scrollableResults.next() && list.size() < pagination.getMaxDisplayRecordsInteger() + 1) {
            AntibodyAOStatistics antibodyStat = (AntibodyAOStatistics) scrollableResults.get(0);
            populateAntibodyStatisticsRecord(antibodyStat, list, aoTerm);
        }
        // remove the last entity as it is beyond the display limit.
        if (list.size() > pagination.getMaxDisplayRecordsInteger()) {
            list.remove(list.size() - 1);
        }
        scrollableResults.close();

        // Since the number of entities that manifest a single record are comprised of
        // multiple single records (differ by figures, genes, pubs) from the database we have to aggregate
        // them into single entities. Need to populate one more entity than requested to collect
        // all information pertaining to that record. Have to remove that last entity.

        return list;
    }

    @Override
    public int getAntibodyCount(Term aoTerm, boolean includeSubstructures) {
        String hql;
        if (includeSubstructures) {
            hql = "select count(distinct stat.antibody) " +
                    "     from AntibodyAOStatistics stat " +
                    "     where stat.superterm = :aoterm ";
        } else {
            hql = "select count(distinct stat.antibody) " +
                    "     from AntibodyAOStatistics stat " +
                    "     where stat.superterm = :aoterm and " +
                    "           stat.subterm = :aoterm";
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("aoterm", aoTerm);
        return ((Number) query.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAllAntibodies() {
        Query query = HibernateUtil.currentSession().createQuery("from Antibody a order by a.name asc");
        return query.list();
    }

    @Override
    public List<Antibody> getAntibodiesByName(String query) {
        List<Antibody> antibodies = new ArrayList<Antibody>();
        Session session = HibernateUtil.currentSession();

        Criteria criteria1 = session.createCriteria(Antibody.class);
        criteria1.add(Restrictions.ilike("abbreviation", query, MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviationOrder"));
        antibodies.addAll(criteria1.list());

        Criteria criteria2 = session.createCriteria(Antibody.class);
        criteria2.add(Restrictions.ilike("abbreviation", query, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.ilike("abbreviation", query, MatchMode.START)));
        criteria2.addOrder(Order.asc("abbreviationOrder"));
        antibodies.addAll(criteria2.list());
        return antibodies;
    }

    /**
     * Create a list of AntibodyStatistics objects from antibodyAOStatistics record.
     * This logic groups the objects accordingly.
     *
     * @param record AntibodyAOStatistics
     * @param aoTerm anatomy term
     * @param list   antibodyStatistics objects to be manipulated.
     */
    private void populateAntibodyStatisticsRecord(AntibodyAOStatistics record, List<AntibodyStatistics> list, GenericTerm aoTerm) {

        if (record == null || record.getAntibody() == null)
            return;

        AntibodyStatistics abStat;
        if (list.size() == 0) {
            abStat = new AntibodyStatistics(record.getAntibody(), aoTerm);
            list.add(abStat);
        } else
            abStat = list.get(list.size() - 1);

        // if antibody from records is the same as the one on the statistics object
        // add new info to that object.
        AntibodyStatistics newAntibodyStat;
        boolean isNew = false;
        if (record.getAntibody().equals(abStat.getAntibody())) {
            newAntibodyStat = abStat;
        } else {
            newAntibodyStat = new AntibodyStatistics(record.getAntibody(), abStat.getAnatomyItem());
            isNew = true;
        }

        Marker gene = record.getGene();
        if (gene != null)
            newAntibodyStat.addGene(gene);
        Figure figure = record.getFigure();
        if (figure != null)
            newAntibodyStat.addFigure(figure);
        Publication publication = record.getPublication();
        if (publication != null)
            newAntibodyStat.addPublication(publication);

        if (isNew)
            list.add(newAntibodyStat);
    }

    public List<Figure> getFiguresForAntibodyWithTermsAtStage(Antibody antibody, GenericTerm superTerm, GenericTerm subTerm,
                                                              DevelopmentStage start, DevelopmentStage end, boolean withImgOnly) {

        List<Figure> figures = new ArrayList<Figure>();
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(Figure.class);
        criteria.createAlias("expressionResults", "xpatres");
        criteria.createAlias("xpatres.expressionExperiment", "xpatex");
        criteria.createAlias("xpatex.fishExperiment", "fishox");
        criteria.createAlias("fishox.fish", "fish");
        criteria.createAlias("fish.genotype", "geno");
        criteria.createAlias("fishox.experiment", "exp");
        criteria.createAlias("publication", "pub");


        criteria.add(Restrictions.eq("xpatex.antibody", antibody));
        criteria.add(Restrictions.eq("xpatres.expressionFound", true));
        criteria.add(Restrictions.eq("geno.wildtype", true));
        criteria.add(Restrictions.or(Restrictions.eq("exp.name", Experiment.STANDARD),
                Restrictions.eq("exp.name", Experiment.GENERIC_CONTROL)));
        criteria.add(Restrictions.eq("xpatres.entity.superterm", superTerm));

        if (subTerm != null)
            criteria.add(Restrictions.eq("xpatres.entity.subterm", subTerm));
        else
            criteria.add(Restrictions.isNull("xpatres.entity.subterm"));

        if (start != null)
            criteria.add(Restrictions.eq("xpatres.startStage", start));
        if (end != null)
            criteria.add(Restrictions.eq("xpatres.endStage", end));
        if (withImgOnly)
            criteria.add(Restrictions.isNotEmpty("images"));

        figures.addAll(criteria.list());
        return figures;
    }

    /**
     * Retrieve a list of figures for a given antibody, super and sub term, stage range.
     * Note: If start and end stage is null and the subTerm as well we assume the
     * caller means: give me all figures with antibodies at any stage with the super term
     * either super term or sub term.
     *
     * @param antibody    antibody
     * @param term        term
     * @param withImgOnly only figures with images or not
     * @return list of figures
     */
    public List<Figure> getFiguresForAntibodyWithTerms(Antibody antibody, GenericTerm term, boolean withImgOnly) {
        List<Figure> figures = new ArrayList<Figure>();
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(Figure.class);
        criteria.createAlias("expressionResults", "xpatres");
        criteria.createAlias("xpatres.expressionExperiment", "xpatex");
        criteria.createAlias("xpatex.fishExperiment", "fishox");
        criteria.createAlias("fishox.fish", "fish");
        criteria.createAlias("fish.genotype", "geno");
        criteria.createAlias("fishox.experiment", "exp");
        criteria.createAlias("publication", "pub");


        criteria.add(Restrictions.eq("xpatex.antibody", antibody));
        criteria.add(Restrictions.eq("xpatres.expressionFound", true));
        criteria.add(Restrictions.eq("geno.wildtype", true));
        criteria.add(Restrictions.or(Restrictions.eq("exp.name", Experiment.STANDARD),
                Restrictions.eq("exp.name", Experiment.GENERIC_CONTROL)));
        criteria.add(Restrictions.or(Restrictions.eq("xpatres.entity.superterm", term),
                Restrictions.eq("xpatres.entity.subterm", term)));

        if (withImgOnly)
            criteria.add(Restrictions.isNotEmpty("images"));

        figures.addAll(criteria.list());
        return figures;
    }

    public Set<GenericTerm> getAntibodyFigureSummaryTerms(Figure figure, Antibody antibody,
                                                          DevelopmentStage start, DevelopmentStage end) {
        Set<GenericTerm> terms = new TreeSet<GenericTerm>();

        String hql = "select xpatres from ExpressionResult xpatres " +
                "   join xpatres.expressionExperiment " +
                "   join xpatres.figures " +
                "   join fetch xpatres.entity.superterm " +
                "   left outer join fetch xpatres.entity.subterm " +
                " where :figure member of xpatres.figures " +
                "   and xpatres.expressionFound = :expressionFound " +
                "   and xpatres.expressionExperiment.antibody = :antibody " +
                "   and xpatres.expressionExperiment.fishExperiment.fish.genotype.wildtype = :isWildtype " +
                "   and ( xpatres.expressionExperiment.fishExperiment.experiment.name = :standard  " +
                "         or xpatres.expressionExperiment.fishExperiment.experiment.name = :gc ) ";
        if (start != null)
            hql += "  and  xpatres.startStage = :startStage ";
        if (end != null)
            hql += "  and xpatres.endStage = :endStage ";


        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("figure", figure);
        if (start != null)
            query.setParameter("startStage", start);
        if (end != null)
            query.setParameter("endStage", end);
        query.setParameter("antibody", antibody);
        query.setParameter("isWildtype", true);
        query.setParameter("expressionFound", true);
        query.setParameter("standard", Experiment.STANDARD);
        query.setParameter("gc", Experiment.GENERIC_CONTROL);

        List<ExpressionResult> expressionResults = query.list();


        for (ExpressionResult expressionResult : expressionResults) {
            terms.add(expressionResult.getSuperTerm());
            if (expressionResult.getSubTerm() != null)
                terms.add(expressionResult.getSubTerm());
        }

        return terms;
    }




}
