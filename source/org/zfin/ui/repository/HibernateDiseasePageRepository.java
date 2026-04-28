package org.zfin.ui.repository;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.ChebiPhenotypeDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.repository.PaginationResultFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.zfin.util.ZfinCollectionUtils.firstInEachGrouping;


@Log4j2
public class HibernateDiseasePageRepository implements DiseasePageRepository {

    @Override
    public PaginationResult<OmimPhenotypeDisplay> getGenesInvolved(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        List<Long> matchingIds = null;
        if (includeChildren) {
            matchingIds = findIdsByAncestor("ui.omim_phenotype_display", "opd_id", "opd_ancestor_term_ids", term.getZdbID());
            if (matchingIds.isEmpty()) {
                return new PaginationResult<>(0, Collections.emptyList());
            }
        }
        String hql;
        if (!includeChildren) {
            hql = "select omimPhenotype from OmimPhenotypeDisplay as omimPhenotype join omimPhenotype.zfinGene as zfinGene where omimPhenotype.disease = :disease ";
        } else {
            hql = "select omimPhenotype from OmimPhenotypeDisplay as omimPhenotype join omimPhenotype.zfinGene as zfinGene " +
                  "where omimPhenotype.id in :ids ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += "order by omimPhenotype.homoSapiensGene.symbol";
        Query<OmimPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, OmimPhenotypeDisplay.class);
        if (includeChildren) {
            query.setParameterList("ids", matchingIds);
        } else {
            query.setParameter("disease", term);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<FishStatistics> getPhenotype(GenericTerm term, Pagination pagination, Boolean includeChildren, Boolean isIncludeNormalPhenotype) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        List<Long> matchingIds = null;
        if (includeChildren) {
            matchingIds = findIdsByAncestor("ui.term_phenotype_display", "tpd_id", "tpd_ancestor_term_ids", term.getZdbID());
            if (matchingIds.isEmpty()) {
                return new PaginationResult<>(0, Collections.emptyList());
            }
        }
        String hql;
        if (!includeChildren) {
            hql = """
                select distinct fishStat from FishStatistics as fishStat
                left join fetch fishStat.fish
                left join fetch fishStat.term
                left join fetch fishStat.figure
                left join fetch fishStat.publication
                left join fetch fishStat.affectedGenes
                left join fetch fishStat.phenotypeStatements as phenoStats
                where fishStat.term = :term
                """;
        } else {
            hql = """
                select distinct fishStat from FishStatistics as fishStat
                left join fetch fishStat.fish
                left join fetch fishStat.term
                left join fetch fishStat.figure
                left join fetch fishStat.publication
                left join fetch fishStat.affectedGenes
                left join fetch fishStat.phenotypeStatements as phenoStats
                where fishStat.id in :ids
                   """;
        }
        if (!isIncludeNormalPhenotype) {
            hql += "AND phenoStats.tag = 'abnormal'";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += "order by fishStat.fish.order, fishStat.fish.nameOrder, fishStat.geneSymbolSearch";
        Query<FishStatistics> query = HibernateUtil.currentSession().createQuery(hql, FishStatistics.class);
        if (includeChildren) {
            query.setParameterList("ids", matchingIds);
        } else {
            query.setParameter("term", term);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<FishModelDisplay> getFishDiseaseModels(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        List<Long> matchingIds = null;
        if (includeChildren) {
            matchingIds = findIdsByAncestor("ui.zebrafish_models_display", "zmd_id", "zmd_ancestor_term_ids", term.getZdbID());
            if (matchingIds.isEmpty()) {
                return new PaginationResult<>(0, Collections.emptyList());
            }
        }
        String hql;
        if (!includeChildren) {
            hql = "select fishModelDisplay from FishModelDisplay as fishModelDisplay where fishModelDisplay.disease = :term ";
        } else {
            hql = "select fishModelDisplay from FishModelDisplay as fishModelDisplay " +
                  "where fishModelDisplay.id in :ids ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += " order by fishModelDisplay.order, fishModelDisplay.fish.order, upper(fishModelDisplay.fish.displayName) ";
        Query<FishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, FishModelDisplay.class);
        if (includeChildren) {
            query.setParameterList("ids", matchingIds);
        } else {
            query.setParameter("term", term);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public List<ChebiFishModelDisplay> getFishDiseaseChebiModels(GenericTerm term, boolean includeChildren) {
        List<Long> matchingIds = null;
        if (includeChildren) {
            matchingIds = findIdsByAncestor("ui.zebrafish_models_chebi_association", "omca_id", "omca_ancestor_term_ids", term.getZdbID());
            if (matchingIds.isEmpty()) {
                return Collections.emptyList();
            }
        }
        String hql;
        if (!includeChildren) {
            hql = "select chebiDisplay from ChebiFishModelDisplay as chebiDisplay " +
                  "where chebiDisplay.chebi = :chebiTerm ";
        } else {
            hql = "select chebiDisplay from ChebiFishModelDisplay as chebiDisplay " +
                  "where chebiDisplay.id in :ids ";
        }
        hql += " order by chebiDisplay.fishModelDisplay.order, chebiDisplay.fishModelDisplay.fish.order, upper(chebiDisplay.fishModelDisplay.fish.displayName) ";
        Query<ChebiFishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, ChebiFishModelDisplay.class);
        if (includeChildren) {
            query.setParameterList("ids", matchingIds);
        } else {
            query.setParameter("chebiTerm", term);
        }
        List<ChebiFishModelDisplay> list = query.list();
        return list;
    }

    public List<FishModelDisplay> getAllFishDiseaseModels() {
        String hql;
        hql = "select display from FishModelDisplay as display";
        Query<FishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, FishModelDisplay.class);
        List<FishModelDisplay> list = query.list();
        return list;
    }

    @Override
    public PaginationResult<ChebiPhenotypeDisplay> getPhenotypeChebi(GenericTerm term, Pagination pagination, String filterPhenotype, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        List<Long> matchingIds = null;
        if (includeChildren) {
            matchingIds = findIdsByAncestor("ui.chebi_phenotype_display", "cpd_id", "cpd_ancestor_term_ids", term.getZdbID());
            if (matchingIds.isEmpty()) {
                return new PaginationResult<>(0, Collections.emptyList());
            }
        }
        String hql;
        String fetchJoins = " join fetch chebiPhenotype.fish" +
                " join fetch chebiPhenotype.term" +
                " left join fetch chebiPhenotype.figure" +
                " left join fetch chebiPhenotype.publication" +
                " left join fetch chebiPhenotype.experiment";
        if (!includeChildren) {
            hql = "select chebiPhenotype from ChebiPhenotypeDisplay as chebiPhenotype" + fetchJoins + " where chebiPhenotype.term = :term ";
        } else {
            hql = "select chebiPhenotype from ChebiPhenotypeDisplay as chebiPhenotype" + fetchJoins +
                  " where chebiPhenotype.id in :ids ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        if (MapUtils.isNotEmpty(pagination.getBooleanFilterMap())) {
            for (var entry : pagination.getBooleanFilterMap().entrySet()) {
                hql += " AND ";
                hql += entry.getKey() + " = " + (entry.getValue() ? "true" : "false" ) + " ";
            }
        }
        if (CollectionUtils.isNotEmpty(pagination.getNotNullFilterMap())) {
            for (var entry : pagination.getNotNullFilterMap()) {
                hql += " AND ";
                hql += entry + " is not null ";
            }
        }
        hql += "order by chebiPhenotype.fish.displayName";
        Query<ChebiPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, ChebiPhenotypeDisplay.class);
        if (includeChildren) {
            query.setParameterList("ids", matchingIds);
        } else {
            query.setParameter("term", term);
        }
        PaginationResult<ChebiPhenotypeDisplay> result = PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
        // make phenotypeStatementWarehouse objects a unique list
        result.getPopulatedResults().forEach(chebiPhenotypeDisplay -> {
            var psws = chebiPhenotypeDisplay.getPhenotypeStatements();
            psws = firstInEachGrouping(psws, PhenotypeStatementWarehouse::getDisplayName);
            if (StringUtils.isNotEmpty(filterPhenotype)) {
                psws = psws.stream().filter(p -> containsIgnoreCase(p.getDisplayName(), filterPhenotype)).toList();
            }
            chebiPhenotypeDisplay.setPhenotypeStatements(psws);
        });
        return result;
    }

    // Returns PKs of rows whose ancestor-term-ids array contains termId. Native SQL with an
    // explicit text[] cast (CAST(...) form, NOT ::text[] — the latter confuses Hibernate's
    // named-parameter parser since `:` is the parameter prefix). Hibernate 6's HQL array_contains
    // binds the wrap as varchar[], which Postgres refuses to coerce to text[] for the @> operator.
    private List<Long> findIdsByAncestor(String tableName, String pkColumn, String ancestorColumn, String termId) {
        String sql = "SELECT " + pkColumn + " FROM " + tableName +
                     " WHERE " + ancestorColumn + " @> CAST(ARRAY[:termId] AS text[])";
        List<?> rows = HibernateUtil.currentSession()
            .createNativeQuery(sql)
            .setParameter("termId", termId)
            .getResultList();
        return rows.stream().map(r -> ((Number) r).longValue()).toList();
    }

    // empty out fast search tables (starting with ui.)
    @Override
    public int deleteUiTables(String... tableNames) {
        Arrays.stream(tableNames).filter(s -> s.toLowerCase().startsWith("ui.")).forEach(tableName -> {
            String hql = String.format("delete from %s", tableName);
            Query query = HibernateUtil.currentSession().createNativeQuery(hql);
            int number = query.executeUpdate();
            log.info("rm data [" + tableName + "] " + String.format("%,d", number));
        });
        return 0;
    }

}
