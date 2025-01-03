package org.zfin.antibody.repository;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
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
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.presentation.HighQualityProbeAOStatistics;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.hibernate.criterion.CriteriaSpecification.DISTINCT_ROOT_ENTITY;


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


    public PaginationResult<Publication> getPublicationsWithFigures(Antibody antibody, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select publication from Publication as publication
            left outer join publication.expressionExperiments as expressionExperiment
            left outer join expressionExperiment.figureStageSet as figureStage
            left outer join figureStage.expressionResultSet as expressionResult
            inner join expressionExperiment.fishExperiment as fishExperiment
            """;

        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();

        hqlClauses.add("expressionExperiment.antibody = :antibody");
        parameterMap.put("antibody", antibody);
        hqlClauses.add("(expressionResult.superTerm = :term OR expressionResult.subTerm = :term )");
        parameterMap.put("term", aoTerm);
        hqlClauses.add("size(publication.figures) > 0");
        hqlClauses.add("expressionResult.expressionFound = true");
        hqlClauses.add("fishExperiment.standardOrGenericControl = true");
        hqlClauses.add("fishExperiment.fish.genotype.wildtype = true");
        hql += " where " + String.join(" and ", hqlClauses);
        Query<Publication> query = session.createQuery(hql, Publication.class);
        parameterMap.forEach(query::setParameter);
        return new PaginationResult<>(query.list());
    }

    public PaginationResult<Publication> getPublicationsProbeWithFigures(Marker probe, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct publication from Publication as publication
            join publication.expressionExperiments expExperiment
            join expExperiment.figureStageSet figureStage
            join figureStage.expressionResultSet expressionResult
            where expExperiment.publication = publication
            AND figureStage.expressionExperiment = expExperiment
            AND expExperiment.probe = :probe
            AND expressionResult.expressionFigureStage = figureStage
            AND (expressionResult.superTerm = :term OR expressionResult.subTerm = :term)
            AND expressionResult.expressionFound = true
            AND expExperiment.fishExperiment.standardOrGenericControl = true
            AND expExperiment.fishExperiment.fish.genotype.wildtype = true
            """;

        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("probe", probe);
        query.setParameter("term", aoTerm);

        query.setResultTransformer(DISTINCT_ROOT_ENTITY);
        return new PaginationResult<>(query.list());
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
        Query<Antibody> criteria = session.createQuery("from Antibody where abbreviation = :abbreviation", Antibody.class);
        criteria.setParameter("abbreviation", antibodyAbbrev);
        return criteria.uniqueResult();
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
            hql.append(", ExpressionExperiment2 experiment ");
        }
        if (!StringUtils.isEmpty(searchCriteria.getAntigenGeneName())) {
            hql.append(",  AbstractMarkerRelationshipInterface rel   ");
        }
        if (searchCriteria.isAnatomyDefined()) {
            hql.append(",  ExpressionTermFastSearch expressionTerm, ExpressionResult2 expressionResult ");
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
            hql.append("  exists ( select result from ExpressionResult2 result " +
                       "                  where result.expressionFigureStage.startStage.hoursStart >= :hoursStart " +
                       "                    AND result.expressionFigureStage.endStage.hoursStart <= :hoursEnd " +
                       "                    AND result.expressionFigureStage.expressionExperiment.antibody = antibody ) ");
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
                       "                     AND expressionResult.expressionFigureStage.expressionExperiment = experiment ");
            if (searchCriteria.isStageDefined()) {
                hql.append("                  AND expressionResult.expressionFigureStage.startStage.hoursStart >= :hoursStart " +
                           "                     AND expressionResult.expressionFigureStage.endStage.hoursEnd <= :hoursEnd ");
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
                               "ExpressionResult2 expressionResult2, ExpressionExperiment2 experiment2 " +
                               "   where " +
                               "       expressionTerm.term.zdbID = :aoTermID_" + i +
                               "                     AND expressionResult2.expressionFigureStage.expressionExperiment = experiment2 " +
                               "                     AND experiment2.antibody = antibody " +
                               "                     AND expressionTerm.expressionResult = expressionResult2 ");
                    if (searchCriteria.isStageDefined()) {
                        hql.append("                  AND expressionResult2.expressionFigureStage.startStage.hoursStart >= :hoursStart " +
                                   "                     AND expressionResult2.expressionFigureStage.endStage.hoursEnd <= :hoursEnd ");
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

    public List<AntibodyStatistics> getAntibodyStatisticsPaginated(GenericTerm aoTerm, PaginationBean pagination, List<String> termIDs, boolean includeSubstructures) {

        String hql;
        // loop over all antibodyAOStatistic records until the given number of distinct antibodies from the pagination
        // bean is reached.
        if (includeSubstructures) {
            hql = " select distinct stat, lower(stat.antibody.name) from AntibodyAOStatistics stat " +
                  " LEFT JOIN FETCH stat.figure " +
                  " LEFT JOIN FETCH stat.image " +
                  " LEFT JOIN FETCH stat.gene " +
                  " LEFT JOIN FETCH stat.publication " +
                  " LEFT JOIN FETCH stat.antibody " +
                  "     where stat.superterm = :term" +
                  "           AND stat.antibody.zdbID in (:abIds) ";
        } else {
            hql = " select distinct stat, lower(stat.antibody.name) from AntibodyAOStatistics stat " +
                  " LEFT JOIN FETCH stat.figure " +
                  " LEFT JOIN FETCH stat.image " +
                  " LEFT JOIN FETCH stat.gene " +
                  " LEFT JOIN FETCH stat.publication " +
                  " LEFT JOIN FETCH stat.antibody " +
                  "     where (stat.superterm = :term AND " +
                  "           stat.subterm = :term)  " +
                  "           AND stat.antibody.zdbID in (:abIds) ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                hql += " AND lower(stat." + entry.getKey() + ") like :" + entry.getKey().replace(".", "") + " ";
            }
        }
        hql += "           order by lower(stat.antibody.name) ";

        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("term", aoTerm);
        query.setParameterList("abIds", termIDs);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey().replace(".", ""), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        List<Object[]> resultList = query.list();

        List<AntibodyStatistics> list = new ArrayList<>();
        resultList.stream().map(objects -> (AntibodyAOStatistics) objects[0]).toList()
            .forEach(antibodyStat -> populateAntibodyStatisticsRecord(antibodyStat, list, antibodyStat.getSubterm()));
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
                  "           AND stat.gene.zdbID in (:geneIds) ";
        } else {
            hql = " select distinct stat, lower(stat.gene.name) from HighQualityProbeAOStatistics stat " +
                  " LEFT JOIN FETCH stat.figure " +
                  " LEFT JOIN FETCH stat.gene " +
                  " LEFT JOIN FETCH stat.publication " +
                  " LEFT JOIN FETCH stat.probe " +
                  "     where (stat.superterm = :term AND " +
                  "           stat.subterm = :term)  " +
                  "           AND stat.gene.zdbID in (:geneIds) ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                hql += " AND lower(stat." + entry.getKey() + ") like :" + entry.getKey().replace(".", "") + " ";
            }
        }
        hql += "           order by lower(stat.gene.name) ";

        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("term", aoTerm);
        query.setParameterList("geneIds", termIDs);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey().replace(".", ""), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        List<Object[]> resultList = query.list();

        List<HighQualityProbe> list = new ArrayList<>();
        resultList.stream().map(objects -> (HighQualityProbeAOStatistics) objects[0]).toList()
            .forEach(highQualityProbe -> populateHighQualityProbeStatisticsRecord(highQualityProbe, list, highQualityProbe.getSubterm()));
        return list;
    }

    @Override
    public int getAntibodyCount(Term aoTerm, boolean includeSubstructures, Pagination pagination) {
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
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                hql += " AND lower(stat." + entry.getKey() + ") like :" + entry.getKey().replace(".", "");
            }
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("aoterm", aoTerm);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey().replace(".", ""), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        return ((Number) query.uniqueResult()).intValue();
    }

    @Override
    public int getProbeCount(Term aoTerm, boolean includeSubstructures, Pagination pagination) {
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
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                hql += " AND lower(stat." + entry.getKey() + ") like :" + entry.getKey().replace(".", "");
            }
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("aoterm", aoTerm);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey().replace(".", ""), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        return ((Number) query.uniqueResult()).intValue();
    }

    @Override
    public List<String> getPaginatedAntibodyIds(Term aoTerm, boolean includeSubstructures, Pagination pagination) {
        String hql;
        if (includeSubstructures) {
            hql = "select stat.antibody.zdbID, stat.antibody.abbreviationOrder " +
                  "     from AntibodyAOStatistics stat " +
                  "     where stat.superterm = :aoterm ";
        } else {
            hql = "select stat.antibody.zdbID, stat.antibody.abbreviationOrder " +
                  "     from AntibodyAOStatistics stat " +
                  "     where stat.superterm = :aoterm and " +
                  "           stat.subterm = :aoterm  ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                hql += " AND lower(stat." + entry.getKey() + ") like :" + entry.getKey().replace(".", "") + " ";
            }
        }
        hql += """
            group by stat.antibody.zdbID, stat.antibody.abbreviationOrder
            order by 2
             """;
        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        query.setParameter("aoterm", aoTerm);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey().replace(".", ""), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        return query.list().stream().map(objects -> (String) objects[0]).collect(toList());
    }

    @Override
    public List<String> getPaginatedHighQualityProbeIds(Term aoTerm, boolean includeSubstructures, Pagination pagination) {
        String hql;
        if (includeSubstructures) {
            hql = """
                select stat.gene.zdbID, stat.gene.abbreviationOrder
                     from HighQualityProbeAOStatistics stat
                     where stat.superterm = :aoterm
                     """;
        } else {
            hql = """
                select stat.gene.zdbID, stat.gene.abbreviationOrder
                     from HighQualityProbeAOStatistics stat
                     where stat.superterm = :aoterm and
                           stat.subterm = :aoterm
                           """;
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                hql += " AND lower(stat." + entry.getKey() + ") like :" + entry.getKey().replace(".", "") + " ";
            }
        }
        hql += """
            group by stat.gene.zdbID, stat.gene.abbreviationOrder
            order by 2
             """;
        org.hibernate.query.Query<Object[]> query = HibernateUtil.currentSession().createQuery(hql, Object[].class);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey().replace(".", ""), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
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
        Session session = HibernateUtil.currentSession();

        String hql = """
            from Antibody
            where lower(abbreviation) like :abbreviation
            order by abbreviationOrder
            """;

        Query<Antibody> qQuery = session.createQuery(hql, Antibody.class);
        qQuery.setParameter("abbreviation", query.toLowerCase() + "%");
        List<Antibody> antibodies = new ArrayList<>(qQuery.list());

        String hql2 = """
            from Antibody
            where lower(abbreviation) like :abbreviation
            order by abbreviationOrder
            """;
        qQuery = session.createQuery(hql2, Antibody.class);
        qQuery.setParameter("abbreviation", "%" + query.toLowerCase() + "%");
        List<Antibody> list = qQuery.list();
        list.stream().filter(antibody -> !antibodies.contains(antibody)).forEach(antibodies::add);

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
            // do not unset images (false) once they are set to true
            if (CollectionUtils.isNotEmpty(figure.getImages()))
                newAntibodyStat.setHasImages(true);
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
            if (CollectionUtils.isNotEmpty(figure.getImages()))
                newAntibodyStat.setHasImages(true);
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

    @Override
    public Map<String, List<Marker>> getAntibodyAntigenGeneMap(List<String> antibodyIDs) {
        String hql = """
            from MarkerRelationship where secondMarker.zdbID in (:antibodyIdList)
            """;

        Query<MarkerRelationship> query = HibernateUtil.currentSession().createQuery(hql, MarkerRelationship.class);
        query.setParameterList("antibodyIdList", antibodyIDs);
        List<MarkerRelationship> markerRelationshipList = query.list();
        Map<String, List<Marker>> antibodyAntigenGeneList = markerRelationshipList.stream()
            .collect(groupingBy(MarkerRelationship::getSecondMarker))
            .entrySet().stream()
            .collect(toMap(entry -> entry.getKey().getZdbID(),
                e -> e.getValue().stream().map(MarkerRelationship::getFirstMarker).toList()));
        return antibodyAntigenGeneList;
    }

    private Map<Publication, List<Antibody>> pubAntibodyMapCached;

    @Override
    public Map<Publication, List<Antibody>> getAntibodiesFromAllPublications() {
        if (pubAntibodyMapCached != null)
            return pubAntibodyMapCached;
        Session session = HibernateUtil.currentSession();

        String hql = "select pubAttribute, antibody from Antibody as antibody, PublicationAttribution pubAttribute " +
                     "     where pubAttribute.dataZdbID = antibody.zdbID ";
        Query query = session.createQuery(hql);
        //query.setString("antibodytype", "ZDB-" + Marker.Type.ATB.name() + "%");
        List<Object[]> list = query.list();
        Map<Publication, List<Antibody>> antibodyMap = new HashMap<>();

        list.forEach(objects -> {
            Publication pub = ((PublicationAttribution) objects[0]).getPublication();
            Antibody antibody = (Antibody) objects[1];
            antibodyMap.computeIfAbsent(pub, k -> new ArrayList<>());
            antibodyMap.get(pub).add(antibody);
        });

        hql = " select rel from MarkerRelationship rel where " +
              "rel.markerRelationshipType.name = :type ";
        query = session.createQuery(hql);
        query.setParameter("type", MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY.toString());
        List<MarkerRelationship> rels = query.list();
        antibodyMap.values().stream().flatMap(Collection::stream).forEach(antibody -> {
            List<Marker> marker = rels.stream().filter(relationship -> relationship.getSecondMarker().getZdbID().equals(antibody.getZdbID()))
                .map(MarkerRelationship::getFirstMarker)
                .collect(Collectors.toList());
            antibody.setAntigenGenes(marker);
        });
        pubAntibodyMapCached = antibodyMap;
        return pubAntibodyMapCached;
    }

}
