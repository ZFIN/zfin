package org.zfin.ui.repository;

import org.apache.commons.collections4.MapUtils;
import org.hibernate.query.Query;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Clone;
import org.zfin.marker.MarkerType;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;

import jakarta.persistence.Tuple;
import java.util.*;
import java.util.stream.Collectors;

public class HibernatePublicationPageRepository implements PublicationPageRepository {


    @Override
    public PaginationResult<ExpressionTableRow> getPublicationExpression(Publication publication, Pagination pagination) {
        String hql;
        hql = """
            select tableRow from ExpressionTableRow as tableRow
            join fetch tableRow.publication
            join fetch tableRow.figure
            join fetch tableRow.start
            join fetch tableRow.end
            join fetch tableRow.assay
            left join fetch tableRow.subterm
            join fetch tableRow.superterm
            left join fetch tableRow.antibody
            join fetch tableRow.experiment
            """;

        String hqlWhereClause = "";
        if (publication != null) {
            hqlWhereClause += "where tableRow.publication = :pub ";
        }

        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            if (publication == null) {
                hqlWhereClause += "where ";
            }
            for (var entry : pagination.getFilterMap().entrySet()) {
                hqlWhereClause += " AND ";

                String key = entry.getKey();
                if (key.contains("OR")) {
                    // multiple OR clauses
                    String[] keys = key.split("OR");
                    hqlWhereClause += "(";
                    StringJoiner joiner = new StringJoiner(" OR ");
                    for (String singleKey : keys) {
                        joiner.add(" LOWER(" + singleKey + ") like '%" + entry.getValue().toLowerCase() + "%' ");
                    }
                    hqlWhereClause += joiner.toString();
                    hqlWhereClause += ")";
                } else {
                    // just a single clause
                    hqlWhereClause += " LOWER(" + key + ") like '%" + entry.getValue().toLowerCase() + "%' ";
                }
            }
        }
        hql += hqlWhereClause;
        String hqlOrderBy = "order by upper(tableRow.gene.abbreviation), tableRow.fish.displayName, tableRow.superterm.termName , tableRow.subterm.termName ";
        hql += hqlOrderBy;
        Query<ExpressionTableRow> query = HibernateUtil.currentSession().createQuery(hql, ExpressionTableRow.class);
        if (publication != null) {
            query.setParameter("pub", publication);
            query.setMaxResults(pagination.getLimit());
            query.setFirstResult(pagination.getStart());
        }
        PaginationResult<ExpressionTableRow> result = new PaginationResult<>();
        result.setPopulatedResults(query.getResultList());

        hql = """
            select count(tableRow) from ExpressionTableRow as tableRow
            """;
        hql += hqlWhereClause;
        Query<Long> queryCount = HibernateUtil.currentSession().createQuery(hql, Long.class);
        if (publication != null) {
            queryCount.setParameter("pub", publication);
        }
        result.setTotalCount((int) (long) queryCount.getSingleResult());
        return result;
    }

    @Override
    public PaginationResult<Clone> getProbes(Publication publication, Pagination pagination) {
        String hql = """
            select distinct exp.probe from ExpressionExperiment2 as exp
            """;
        if (publication != null) {
            hql += " where exp.publication = :publication ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                if (entry.getKey().endsWith("integer")) {
                    hql += entry.getKey().substring(0, entry.getKey().lastIndexOf(".")) + " =" + entry.getValue().toLowerCase();
                } else {
                    if (entry.getKey().equals("exp.probe.zdbID")) {
                        String[] vals = entry.getValue().split("||");
                        hql += "(";
                        for (int i = 0; i < vals.length - 1; i++) {
                            String val = vals[i];
                            if (i > 0) {
                                hql += " OR ";
                            }
                            val = "zdb-" + val.toLowerCase();
                            hql += "LOWER(" + entry.getKey() + ") like '%" + val + "%' ";
                        }
                        hql += ")";
                    } else {
                        hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
                    }
                }
            }
        }
        hql += " order by exp.probe.abbreviationOrder";
        Query<Clone> query = HibernateUtil.currentSession().createQuery(hql, Clone.class);
        if (publication != null) {
            query.setParameter("publication", publication);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(pagination, query.scroll());
    }

    @Override
    public List<String> getProbeTypes(Publication publication, Pagination pagination) {
        String hql = """
            select distinct exp.probe.markerType from ExpressionExperiment2 as exp
            where exp.publication = :publication
            """;
        Query<MarkerType> query = HibernateUtil.currentSession().createQuery(hql, MarkerType.class);
        query.setParameter("publication", publication);
        return query.list().stream().map(MarkerType::getName).sorted().toList();
    }

    private Map<Publication, List<ExpressionTableRow>> allPublicationExpressionMap = null;

    @Override
    public Map<Publication, List<ExpressionTableRow>> getAllPublicationExpression(Pagination pagination) {
        if (allPublicationExpressionMap != null) {
            return allPublicationExpressionMap;
        }
        PaginationResult<ExpressionTableRow> publicationExpression = getPublicationExpression(null, pagination);
        allPublicationExpressionMap = publicationExpression.getPopulatedResults().stream().collect(Collectors.groupingBy(ExpressionTableRow::getPublication));
        return allPublicationExpressionMap;
    }

    @Override
    public Map<Publication, List<Clone>> getAllProbes(Pagination pagination) {
        String hql = """
            select exp.probe, exp.publication from ExpressionExperiment2 as exp
            """;
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                if (entry.getKey().endsWith("integer")) {
                    hql += entry.getKey().substring(0, entry.getKey().lastIndexOf(".")) + " =" + entry.getValue().toLowerCase();
                } else {
                    if (entry.getKey().equals("exp.probe.zdbID")) {
                        String[] vals = entry.getValue().split("||");
                        hql += "(";
                        for (int i = 0; i < vals.length - 1; i++) {
                            String val = vals[i];
                            if (i > 0) {
                                hql += " OR ";
                            }
                            val = "zdb-" + val.toLowerCase();
                            hql += "LOWER(" + entry.getKey() + ") like '%" + val + "%' ";
                        }
                        hql += ")";
                    } else {
                        hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
                    }
                }
            }
        }
        hql += " order by exp.probe.abbreviationOrder";
        Query<Tuple> query = HibernateUtil.currentSession().createQuery(hql, Tuple.class);
        Map<Publication, List<Clone>> map = new HashMap<>();
        query.list().forEach(tuple -> {
            List<Clone> clones = map.computeIfAbsent((Publication) tuple.get(1), k -> new ArrayList<>());
            clones.add((Clone) tuple.get(0));
        });
        return map;
    }

}
