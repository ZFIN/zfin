package org.zfin.antibody.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.Species;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.presentation.HighQualityProbeAOStatistics;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.isNotEmpty;


/**
 * Hibernate implementation of the Antibody Repository.
 */
@Repository
public class HibernateAntibodyRepository implements AntibodyRepository {

    private final AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();

    // These attributes are cashed for performance reasons
    // They are static, i.e. they do not change all that often.
    // To update the list you need to restart Tomcat or we can have an
    // update at runtime.
    private List<Species> immunogenSpeciesList;
    private List<Species> hostSpeciesList;


    public Antibody getAntibodyByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Antibody.class, zdbID);
    }


    public PaginationResult<Antibody> getAntibodies(AntibodySearchCriteria searchCriteria) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("select distinct antibody ");
        hql.append(getAntibodiesByNameAndLabelingQueryBlock(searchCriteria));
        hql.append("order by antibody.abbreviationOrder");
        Query<Antibody> query = session.createQuery(hql.toString(), Antibody.class);
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.CONTAINS) {
                query.setParameter("name", "%" + searchCriteria.getName().toLowerCase() + "%");
            }
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.BEGINS) {
                query.setParameter("name", searchCriteria.getName().toLowerCase() + "%");
            }
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (searchCriteria.getAntigenNameFilterType() == FilterType.CONTAINS) {
                query.setParameter("markerName", "%" + searchCriteria.getAntigenGeneName().toLowerCase().trim() + "%");
            }
            if (searchCriteria.getAntigenNameFilterType() == FilterType.BEGINS) {
                query.setParameter("markerName", searchCriteria.getAntigenGeneName().toLowerCase().trim() + "%");
            }
        }
        if (searchCriteria.isHostSpeciesDefined()) {
            query.setParameter("hostSpecies", searchCriteria.getHostSpecies());
        }
        if (searchCriteria.isAssaySearch()) {
            query.setParameter("assay", searchCriteria.getAssay());
        }
        if (searchCriteria.isZircOnly()) {
            query.setParameter("zircLabID", "ZDB-LAB-991005-53");
        }
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType())) {
            query.setParameter("antibodyType", searchCriteria.getClonalType());
        }
        if (searchCriteria.isStageDefined()) {
            bindStageFilterValues(searchCriteria, query);
        }
        if (searchCriteria.isAnatomyDefined()) {
            applyAnatomyTermsFilter(searchCriteria, query);
        }
        int start = 0;
        if (searchCriteria.getPaginationBean() != null) {
            start = searchCriteria.getPaginationBean().getFirstRecord() - 1;
        }
        PaginationResult<Antibody> antibodyObjects = PaginationResultFactory.createResultFromScrollableResultAndClose(start, start + searchCriteria.getPaginationBean().getMaxDisplayRecordsInteger(), query.scroll());

        return antibodyObjects;
    }

    public int getNumberOfAntibodies(AntibodySearchCriteria searchCriteria) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("select count(distinct antibody)");
        hql.append(getAntibodiesByNameAndLabelingQueryBlock(searchCriteria));
        Query<Number> query = session.createQuery(hql.toString(), Number.class);
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            // antibody name and abbreviation is the same
            if (searchCriteria.getAntibodyNameFilterType() == FilterType.CONTAINS) {
                query.setParameter("name", "%" + searchCriteria.getName().toLowerCase() + "%");
            } else if (searchCriteria.getAntibodyNameFilterType() == FilterType.BEGINS) {
                query.setParameter("name", searchCriteria.getName().toLowerCase() + "%");
            }
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (searchCriteria.getAntigenNameFilterType() == FilterType.CONTAINS) {
                query.setParameter("markerName", "%" + searchCriteria.getAntigenGeneName().toLowerCase() + "%");
            } else if (searchCriteria.getAntigenNameFilterType() == FilterType.BEGINS) {
                query.setParameter("markerName", searchCriteria.getAntigenGeneName().toLowerCase() + "%");
            }
        }
        if (searchCriteria.isHostSpeciesDefined()) {
            query.setParameter("hostSpecies", searchCriteria.getHostSpecies());
        }
        if (searchCriteria.isAssaySearch()) {
            query.setParameter("assay", searchCriteria.getAssay());
        }
        if (searchCriteria.isZircOnly()) {
            query.setParameter("zircLabID", "ZDB-LAB-991005-53");
        }
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType())) {
            query.setParameter("antibodyType", searchCriteria.getClonalType());
        }
        if (searchCriteria.isStageDefined()) {
            bindStageFilterValues(searchCriteria, query);
        }
        if (searchCriteria.isAnatomyDefined()) {
            applyAnatomyTermsFilter(searchCriteria, query);
        }
        return query.uniqueResult().intValue();
    }

    public List<Species> getHostSpeciesList() {
        if (hostSpeciesList != null) {
            return hostSpeciesList;
        }

        Session session = HibernateUtil.currentSession();
        Query<Species> speciesQuery = session.createQuery("from Species where antibodyHost = true order by displayOrder", Species.class);
        hostSpeciesList = speciesQuery.list();
        return hostSpeciesList;
    }

    public List<Species> getUsedHostSpeciesList() {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select distinct species from Species as species, Antibody as ab
            where species.antibodyHost = true
            and species.commonName = ab.hostSpecies
            order by species.displayOrder
            """;
        Query<Species> query = session.createQuery(hql, Species.class);
        return query.list();
    }

    public List<Species> getImmunogenSpeciesList() {
        if (immunogenSpeciesList != null) {
            return immunogenSpeciesList;
        }

        Session session = HibernateUtil.currentSession();
        Query<Species> speciesQuery = session.createQuery("from Species where antibodyImmunogen = true order by displayOrder", Species.class);
        immunogenSpeciesList = speciesQuery.list();
        return immunogenSpeciesList;
    }


    public int getAntibodiesByAOTermCount(GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.setProjection(Projections.countDistinct("zdbID"));
        Criteria labeling = criteria.createCriteria("antibodyLabelings");
        Criteria fishExperiment = labeling.createCriteria("fishExperiment");
        fishExperiment.add(Restrictions.eq("standardOrGenericControl", true));
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria results = labeling.createCriteria("expressionResults");
        // check AO1 and AO2
        results.add(Restrictions.or(
            Restrictions.eq("entity.superterm", aoTerm),
            Restrictions.eq("entity.subterm", aoTerm)));
        results.add(eq("expressionFound", true));

        return ((Long) results.list().get(0)).intValue();
    }

    public PaginationResult<Antibody> getAntibodiesByAOTerm(GenericTerm aoTerm, PaginationBean paginationBean, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select distinct antibody from Antibody antibody, ExpressionExperiment expExp,
                   ExpressionResult res, FishExperiment fishox,
                   Experiment exp
            where
                   expExp.antibody = antibody
                   and res.expressionExperiment = expExp
                   and fishox = expExp.fishExperiment
                   and fishox.fish.wildtype = :wildType
                """;
        if (includeSubstructures) {
            hql += """
                and ( res.entity.superterm = :aoTerm  OR res.entity.subterm = :aoTerm
                                                      OR exists ( select 1 from TransitiveClosure child
                      where res.entity.superterm = child.child AND child.root = :aoTerm )
                                                      OR exists ( select 1 from TransitiveClosure child
                      where res.entity.subterm = child.child AND child.root = :aoTerm )
                      )
                    """;
        } else {
            hql += "       and (res.entity.superterm = :aoTerm OR res.entity.subterm = :aoTerm) ";
        }
        hql += """
            and res.expressionFound = :expressionFound
            and fishox.standardOrGenericControl = :standardOrGeneric
            order by antibody.abbreviationOrder
                """;
        Query<Antibody> query = session.createQuery(hql, Antibody.class);
        query.setParameter("wildType", true);
        query.setParameter("aoTerm", aoTerm);
        query.setParameter("expressionFound", true);
        query.setParameter("standardOrGeneric", true);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean, query.scroll());
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
        fishExperiment.add(Restrictions.eq("standardOrGenericControl", true));
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<>((List<Publication>) pubs.list());
    }

    public PaginationResult<Publication> getPublicationsProbeWithFigures(Marker probe, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria pubs = session.createCriteria(Publication.class);
        Criteria labeling = pubs.createCriteria("expressionExperiments");
        labeling.add(eq("probe", probe));
        Criteria results = labeling.createCriteria("expressionResults");
        // check AO1 and AO2
        results.add(Restrictions.or(
            Restrictions.eq("entity.superterm", aoTerm),
            Restrictions.eq("entity.subterm", aoTerm)));
        results.add(isNotEmpty("figures"));
        results.add(eq("expressionFound", true));
        Criteria fishExperiment = labeling.createCriteria("fishExperiment");
        fishExperiment.add(Restrictions.eq("standardOrGenericControl", true));
        Criteria fish = fishExperiment.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<>((List<Publication>) pubs.list());
    }

    public List<Antibody> getAntibodiesByPublication(Publication publication) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct antibody from Antibody as antibody, PublicationAttribution pubAttribute
            where pubAttribute.publication = :pub AND
                  pubAttribute.dataZdbID = antibody.zdbID
            order by antibody.abbreviationOrder
            """;
        Query<Antibody> query = session.createQuery(hql, Antibody.class);
        query.setParameter("pub", publication);
        return query.list();

    }

    public Antibody getAntibodyByAbbrev(String antibodyAbbrev) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Antibody.class);
        criteria.add(Restrictions.eq("abbreviation", antibodyAbbrev));
        return (Antibody) criteria.uniqueResult();
    }

    public Antibody getAntibodyByName(String antibodyName) {
        Session session = HibernateUtil.currentSession();
        Query<Antibody> query = session.createQuery("from Antibody where lower(name) = :antibodyName", Antibody.class);
        query.setParameter("antibodyName", antibodyName.toLowerCase());
        return query.uniqueResult();
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
        query.setParameter("hoursEnd", endStage.getHoursStart());
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

        if (searchCriteria.isZircOnly()) {
            hql.append(", MarkerSupplier markerSupplier ");
        }
        if (searchCriteria.isAssaySearch() || searchCriteria.isAnatomyDefined()) {
            hql.append(", ExpressionExperiment experiment ");
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            hql.append(",  AbstractMarkerRelationshipInterface rel   ");
        }
        if (searchCriteria.isAnatomyDefined()) {
            hql.append(",  ExpressionTermFastSearch expressionTerm, ExpressionResult expressionResult ");
        }

        if (StringUtils.isNotEmpty(searchCriteria.getName())) {
            hql.append("left outer join antibody.aliases atbAliases ");
        }
        if (StringUtils.isNotEmpty(searchCriteria.getAntigenGeneName())) {
            hql.append("left outer join rel.firstMarker antigenGenes ");
            hql.append("left outer join antigenGenes.aliases geneAliases ");
        }

        if (searchCriteria.isAny()) {
            hql.append("where ");
        }
        if (!StringUtils.isEmpty(searchCriteria.getName())) {
            hql.append(" (lower(antibody.name) like :name OR ");
            hql.append("  lower(antibody.abbreviation) like :name OR ");
            hql.append("  lower(atbAliases.alias) like :name) ");
            hasOneWhereClause = true;
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            hql.append(" rel.secondMarker = antibody ");
            hql.append(" AND (lower(antigenGenes.name) like :markerName OR ");
            hql.append("      lower(antigenGenes.abbreviation) like :markerName OR ");
            hql.append("      lower(geneAliases.alias) like :markerName) ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isAssaySearch()) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            hql.append(" experiment.assay.name = :assay AND experiment.antibody = antibody ");
            hasOneWhereClause = true;
        }
        if (!AntibodyType.isTypeAny(searchCriteria.getClonalType())) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            hql.append(" antibody.clonalType = :antibodyType ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isHostSpeciesDefined()) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            hql.append(" antibody.hostSpecies = :hostSpecies ");
            hasOneWhereClause = true;
        }
        if (searchCriteria.isZircOnly()) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            hql.append(" markerSupplier.organization.zdbID = :zircLabID AND markerSupplier.marker = antibody ");
            hasOneWhereClause = true;
        }
        // only stage range defined
        if (searchCriteria.isStageDefined() && !searchCriteria.isAnatomyDefined()) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            hasOneWhereClause = true;
            hql.append("  exists ( select result from ExpressionResult result " +
                "                  where result.startStage.hoursStart >= :hoursStart " +
                "                    AND result.endStage.hoursStart <= :hoursEnd " +
                "                    AND result.expressionExperiment.antibody = antibody ) ");
        }
        if (searchCriteria.isAnatomyDefined()) {
            if (hasOneWhereClause) {
                hql.append(" AND ");
            }
            //hql.append(" experiment.antibody = antibody AND ( ");
            hql.append("  ( ");
            int numberOfTerms = searchCriteria.getTermIDs().length;
            // handle the first term
            hql.append("     expressionTerm.term.zdbID = :aoTermID_0 " +
                "                     AND experiment.antibody = antibody " +
                "                     AND expressionTerm.expressionResult = expressionResult" +
                "                     AND expressionResult.expressionExperiment = experiment ");
            if (searchCriteria.isStageDefined()) {
                hql.append("                  AND expressionResult.startStage.hoursStart >= :hoursStart " +
                    "                     AND expressionResult.endStage.hoursEnd <= :hoursEnd ");
            }
            if (!searchCriteria.isIncludeSubstructures()) {
                hql.append("    AND expressionTerm.originalAnnotation = 't' ");
            }

            if (numberOfTerms > 1) {
                for (int i = 1; i < numberOfTerms; i++) {
                    if (searchCriteria.isAnatomyEveryTerm()) {
                        hql.append(" AND ");
                    } else {
                        hql.append(" OR ");
                    }
                    hql.append(" exists (select expressionTerm from ExpressionTermFastSearch expressionTerm, " +
                        "ExpressionResult expressionResult2, ExpressionExperiment experiment2 " +
                        "   where " +
                        "       expressionTerm.term.zdbID = :aoTermID_" + i +
                        "                     AND expressionResult2.expressionExperiment = experiment2 " +
                        "                     AND experiment2.antibody = antibody " +
                        "                     AND expressionTerm.expressionResult = expressionResult2 ");
                    if (searchCriteria.isStageDefined()) {
                        hql.append("                  AND expressionResult2.startStage.hoursStart >= :hoursStart " +
                            "                     AND expressionResult2.endStage.hoursEnd <= :hoursEnd ");
                    }
                    if (!searchCriteria.isIncludeSubstructures()) {
                        hql.append("    AND expressionTerm.originalAnnotation = 't' ");
                    }
                    hql.append(" ) ");
                }
            }
            hql.append(") ");
        }

        return hql.toString();
    }

    public List<AntibodyStatistics> getAntibodyStatistics(GenericTerm aoTerm, PaginationBean pagination, boolean includeSubstructures) {

        String hql;
        // loop over all antibodyAOStatistic records until the given number of distinct antibodies from the pagination
        // bean is reached.
        if (includeSubstructures) {
            hql = "  from AntibodyAOStatistics stat " +
                " LEFT JOIN FETCH stat.figure " +
                " LEFT JOIN FETCH stat.gene " +
                " LEFT JOIN FETCH stat.publication " +
                " LEFT JOIN FETCH stat.antibody " +
                "     where stat.superterm = :aoterm";
        } else {
            hql = " select distinct stat, lower(stat.antibody.name) from AntibodyAOStatistics stat " +
                " LEFT JOIN FETCH stat.figure " +
                " LEFT JOIN FETCH stat.gene " +
                " LEFT JOIN FETCH stat.publication " +
                " LEFT JOIN FETCH stat.antibody " +
                "     where stat.superterm = :aoterm and " +
                "           stat.subterm = :aoterm " +
                "           order by lower(stat.antibody.name) ";
        }

        ScrollableResults scrollableResults = HibernateUtil.currentSession().createQuery(hql)
            .setParameter("aoterm", aoTerm).scroll();
        List<AntibodyStatistics> list = new ArrayList<>();

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

    public List<AntibodyStatistics> getAntibodyStatisticsPaginated(GenericTerm aoTerm, PaginationBean pagination, List<String> termIDs, boolean includeSubstructures) {

        String hql;
        // loop over all antibodyAOStatistic records until the given number of distinct antibodies from the pagination
        // bean is reached.
        if (includeSubstructures) {
            hql = " select distinct stat, lower(stat.antibody.name) from AntibodyAOStatistics stat " +
                " LEFT JOIN FETCH stat.figure " +
                " LEFT JOIN FETCH stat.gene " +
                " LEFT JOIN FETCH stat.publication " +
                " LEFT JOIN FETCH stat.antibody " +
                "     where stat.superterm = :term" +
                "           AND stat.antibody.zdbID in (:abIds) " +
                "           order by lower(stat.antibody.name) ";
        } else {
            hql = " select distinct stat, lower(stat.antibody.name) from AntibodyAOStatistics stat " +
                " LEFT JOIN FETCH stat.figure " +
                " LEFT JOIN FETCH stat.gene " +
                " LEFT JOIN FETCH stat.publication " +
                " LEFT JOIN FETCH stat.antibody " +
                "     where (stat.superterm = :term AND " +
                "           stat.subterm = :term)  " +
                "           AND stat.antibody.zdbID in (:abIds) " +
                "           order by lower(stat.antibody.name) ";
        }

        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("term", aoTerm);
        query.setParameterList("abIds", termIDs);
        List<Object[]> resultList = query.list();

        List<AntibodyStatistics> list = new ArrayList<>();
        resultList.stream().map(objects -> (AntibodyAOStatistics) objects[0]).toList()
            .forEach(antibodyStat -> populateAntibodyStatisticsRecord(antibodyStat, list, aoTerm));
        return list;
    }

    public List<HighQualityProbe> getProbeStatisticsPaginated(GenericTerm aoTerm, PaginationBean pagination, List<String> termIDs, boolean includeSubstructures) {

        String hql;
        // loop over all highQualityProbeAOStatistic records until the given number of distinct genes from the pagination
        // bean is reached.
        if (includeSubstructures) {
            hql = " select distinct stat, lower(stat.gene.name) from HighQualityProbeAOStatistics stat " +
                " LEFT JOIN FETCH stat.figure " +
                " LEFT JOIN FETCH stat.gene " +
                " LEFT JOIN FETCH stat.publication " +
                " LEFT JOIN FETCH stat.probe " +
                "     where stat.superterm = :term" +
                "           AND stat.gene.zdbID in (:geneIds) " +
                "           order by lower(stat.gene.name) ";
        } else {
            hql = " select distinct stat, lower(stat.gene.name) from HighQualityProbeAOStatistics stat " +
                " LEFT JOIN FETCH stat.figure " +
                " LEFT JOIN FETCH stat.gene " +
                " LEFT JOIN FETCH stat.publication " +
                " LEFT JOIN FETCH stat.probe " +
                "     where (stat.superterm = :term AND " +
                "           stat.subterm = :term)  " +
                "           AND stat.gene.zdbID in (:geneIds) " +
                "           order by lower(stat.gene.name) ";
        }

        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("term", aoTerm);
        query.setParameterList("geneIds", termIDs);
        List<Object[]> resultList = query.list();

        List<HighQualityProbe> list = new ArrayList<>();
        resultList.stream().map(objects -> (HighQualityProbeAOStatistics) objects[0]).toList()
            .forEach(highQualityProbe -> populateHighQualityProbeStatisticsRecord(highQualityProbe, list, aoTerm));
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
                "     where (stat.superterm = :aoterm AND " +
                "           stat.subterm = :aoterm) ";
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("aoterm", aoTerm);
        return ((Number) query.uniqueResult()).intValue();
    }

    @Override
    public int getProbeCount(Term aoTerm, boolean includeSubstructures) {
        String hql;
        if (includeSubstructures) {
            hql = "select count(distinct stat.probe) " +
                "     from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm = :aoterm ";
        } else {
            hql = "select count(distinct stat.probe) " +
                "     from HighQualityProbeAOStatistics stat " +
                "     where (stat.superterm = :aoterm AND " +
                "           stat.subterm = :aoterm) ";
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("aoterm", aoTerm);
        return ((Number) query.uniqueResult()).intValue();
    }

    @Override
    public List<String> getPaginatedAntibodyIds(Term aoTerm, boolean includeSubstructures) {
        String hql;
        if (includeSubstructures) {
            hql = "select stat.antibody.zdbID, stat.antibody.abbreviationOrder " +
                "     from AntibodyAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           group by stat.antibody.zdbID, stat.antibody.abbreviationOrder " +
                "           order by 2 ";
        } else {
            hql = "select stat.antibody.zdbID, stat.antibody.abbreviationOrder " +
                "     from AntibodyAOStatistics stat " +
                "     where stat.superterm = :aoterm and " +
                "           stat.subterm = :aoterm  " +
                "           group by stat.antibody.zdbID, stat.antibody.abbreviationOrder " +
                "           order by 2 ";
        }
        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("aoterm", aoTerm);
        return query.list().stream().map(objects -> (String) objects[0]).collect(toList());
    }

    @Override
    public List<String> getPaginatedHighQualityProbeIds(Term aoTerm, boolean includeSubstructures) {
        String hql;
        if (includeSubstructures) {
            hql = "select stat.gene.zdbID, stat.gene.abbreviationOrder " +
                "     from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           group by stat.gene.zdbID, stat.gene.abbreviationOrder " +
                "           order by 2 ";
        } else {
            hql = "select stat.gene.zdbID, stat.gene.abbreviationOrder " +
                "     from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm = :aoterm and " +
                "           stat.subterm = :aoterm  " +
                "           group by stat.gene.zdbID, stat.gene.abbreviationOrder " +
                "           order by 2 ";
        }
        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("aoterm", aoTerm);
        return query.list().stream().map(objects -> (String) objects[0]).collect(toList());
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAllAntibodies() {
        Query<Antibody> query = HibernateUtil.currentSession().createQuery("from Antibody a order by a.name asc", Antibody.class);
        return query.list();
    }

    @Override
    public List<Antibody> getAntibodiesByName(String query) {
        List<Antibody> antibodies = new ArrayList<>();
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

        if (record == null || record.getAntibody() == null) {
            return;
        }

        AntibodyStatistics abStat;
        if (list.size() == 0) {
            abStat = new AntibodyStatistics(record.getAntibody(), aoTerm);
            list.add(abStat);
        } else {
            abStat = list.get(list.size() - 1);
        }

        // if antibody from records is the same as the one on the statistics object
        // add new info to that object.
        AntibodyStatistics newAntibodyStat;
        boolean isNew = false;
        if (record.getAntibody().equals(abStat.getAntibody())) {
            newAntibodyStat = abStat;
        } else {
            newAntibodyStat = new AntibodyStatistics(record.getAntibody(), record.getSubterm());
            isNew = true;
        }

        Marker gene = record.getGene();
        if (gene != null) {
            newAntibodyStat.addGene(gene);
        }
        Figure figure = record.getFigure();
        if (figure != null) {
            newAntibodyStat.addFigure(figure);
        }
        Publication publication = record.getPublication();
        if (publication != null) {
            newAntibodyStat.addPublication(publication);
        }

        if (isNew) {
            list.add(newAntibodyStat);
        }
    }

    private void populateHighQualityProbeStatisticsRecord(HighQualityProbeAOStatistics record, List<HighQualityProbe> list, GenericTerm aoTerm) {

        if (record == null || record.getGene() == null) {
            return;
        }

        HighQualityProbe probeStat;
        if (list.size() == 0) {
            probeStat = new HighQualityProbe(record.getProbe(), record.getGene(), aoTerm);
            list.add(probeStat);
        } else {
            probeStat = list.get(list.size() - 1);
        }

        // if antibody from records is the same as the one on the statistics object
        // add new info to that object.
        HighQualityProbe newAntibodyStat;
        boolean isNew = false;
        if (probeStat.getGenes().contains(record.getGene())) {
            newAntibodyStat = probeStat;
        } else {
            newAntibodyStat = new HighQualityProbe(record.getProbe(), record.getGene(), record.getSubterm());
            isNew = true;
        }

        Marker gene = record.getGene();
        if (gene != null) {
            newAntibodyStat.addGene(gene);
        }
        Figure figure = record.getFigure();
        if (figure != null) {
            newAntibodyStat.addFigure(figure);
        }
        Publication publication = record.getPublication();
        if (publication != null) {
            newAntibodyStat.addPublication(publication);
        }

        if (isNew) {
            list.add(newAntibodyStat);
        }
    }

    /**
     * Create a list of AntibodyStatistics objects from antibodyAOStatistics record.
     * This logic groups the objects accordingly.
     *
     * @param record AntibodyAOStatistics
     * @param aoTerm anatomy term
     * @param list   antibodyStatistics objects to be manipulated.
     */
    private void populateAntibodyStatisticsRecordPaginated(GenericTerm aoTerm, AntibodyAOStatistics record, List<AntibodyStatistics> list, List<String> termIDs) {

        if (record == null || record.getAntibody() == null) {
            return;
        }

        AntibodyStatistics abStat;
        if (list.size() == 0) {
            abStat = new AntibodyStatistics(record.getAntibody(), aoTerm);
            list.add(abStat);
        } else {
            abStat = list.get(list.size() - 1);
        }

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
        if (gene != null) {
            newAntibodyStat.addGene(gene);
        }
        Figure figure = record.getFigure();
        if (figure != null) {
            newAntibodyStat.addFigure(figure);
        }
        Publication publication = record.getPublication();
        if (publication != null) {
            newAntibodyStat.addPublication(publication);
        }

        if (isNew) {
            list.add(newAntibodyStat);
        }
    }

    public List<Figure> getFiguresForAntibodyWithTermsAtStage(Antibody antibody, GenericTerm superTerm, GenericTerm subTerm,
                                                              DevelopmentStage start, DevelopmentStage end, boolean withImgOnly) {

        List<Figure> figures = new ArrayList<>();
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(Figure.class);
        criteria.createAlias("expressionResults", "xpatres");
        criteria.createAlias("xpatres.expressionExperiment", "xpatex");
        criteria.createAlias("xpatex.fishExperiment", "fishox");
        criteria.createAlias("fishox.fish", "fish");
        criteria.createAlias("fish.genotype", "geno");
        criteria.createAlias("publication", "pub");


        criteria.add(Restrictions.eq("xpatex.antibody", antibody));
        criteria.add(Restrictions.eq("xpatres.expressionFound", true));
        criteria.add(Restrictions.eq("geno.wildtype", true));
        criteria.add(Restrictions.eq("fishox.standardOrGenericControl", true));
        criteria.add(Restrictions.eq("xpatres.entity.superterm", superTerm));

        if (subTerm != null) {
            criteria.add(Restrictions.eq("xpatres.entity.subterm", subTerm));
        } else {
            criteria.add(Restrictions.isNull("xpatres.entity.subterm"));
        }

        if (start != null) {
            criteria.add(Restrictions.eq("xpatres.startStage", start));
        }
        if (end != null) {
            criteria.add(Restrictions.eq("xpatres.endStage", end));
        }
        if (withImgOnly) {
            criteria.add(Restrictions.isNotEmpty("images"));
        }

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
        List<Figure> figures = new ArrayList<>();
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(Figure.class);
        criteria.createAlias("expressionResults", "xpatres");
        criteria.createAlias("xpatres.expressionExperiment", "xpatex");
        criteria.createAlias("xpatex.fishExperiment", "fishox");
        criteria.createAlias("fishox.fish", "fish");
        criteria.createAlias("fish.genotype", "geno");
        criteria.createAlias("publication", "pub");


        criteria.add(Restrictions.eq("xpatex.antibody", antibody));
        criteria.add(Restrictions.eq("xpatres.expressionFound", true));
        criteria.add(Restrictions.eq("geno.wildtype", true));
        criteria.add(Restrictions.eq("fishox.standardOrGenericControl", true));
        criteria.add(Restrictions.or(Restrictions.eq("xpatres.entity.superterm", term),
            Restrictions.eq("xpatres.entity.subterm", term)));

        if (withImgOnly) {
            criteria.add(Restrictions.isNotEmpty("images"));
        }

        figures.addAll(criteria.list());
        return figures;
    }

}
