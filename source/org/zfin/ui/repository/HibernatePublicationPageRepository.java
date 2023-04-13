package org.zfin.ui.repository;

import org.apache.commons.collections4.MapUtils;
import org.hibernate.query.Query;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Clone;
import org.zfin.marker.MarkerType;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;

import java.util.List;

public class HibernatePublicationPageRepository implements PublicationPageRepository {


    @Override
    public PaginationResult<ExpressionTableRow> getPublicationExpression(Publication publication, Pagination pagination) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql;
        hql = """
            select tableRow from ExpressionTableRow as tableRow
            join fetch tableRow.fish
            join fetch tableRow.publication
            join fetch tableRow.figure
            join fetch tableRow.start
            join fetch tableRow.end
            join fetch tableRow.assay
            left join fetch tableRow.subterm
            join fetch tableRow.superterm
            join fetch tableRow.gene
            left join fetch tableRow.antibody
            join fetch tableRow.experiment
            where tableRow.publication = :pub
            """;

        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += "order by upper(tableRow.gene.abbreviation), tableRow.fish.displayName ";
        Query<ExpressionTableRow> query = HibernateUtil.currentSession().createQuery(hql, ExpressionTableRow.class);
        query.setParameter("pub", publication);
        query.setMaxResults(pagination.getLimit());
        PaginationResult<ExpressionTableRow> result = new PaginationResult<>();
        result.setPopulatedResults(query.getResultList());

        hql = """
            select count(tableRow) from ExpressionTableRow as tableRow
            where tableRow.publication = :pub
            """;
        Query queryCount = HibernateUtil.currentSession().createQuery(hql);
        queryCount.setParameter("pub", publication);
        result.setTotalCount((int) (long) queryCount.getSingleResult());
        return result;
    }

    @Override
    public PaginationResult<Clone> getProbes(Publication publication, Pagination pagination) {
        String hql = """
            select distinct exp.probe from ExpressionExperiment as exp
            where exp.publication = :publication
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
        Query<Clone> query = HibernateUtil.currentSession().createQuery(hql, Clone.class);
        query.setParameter("publication", publication);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(pagination, query.scroll());
    }

    @Override
    public List<String> getProbeTypes(Publication publication, Pagination pagination) {
        String hql = """
            select distinct exp.probe.markerType from ExpressionExperiment as exp
            where exp.publication = :publication
            """;
        Query<MarkerType> query = HibernateUtil.currentSession().createQuery(hql, MarkerType.class);
        query.setParameter("publication", publication);
        return query.list().stream().map(MarkerType::getName).sorted().toList();
    }

}
