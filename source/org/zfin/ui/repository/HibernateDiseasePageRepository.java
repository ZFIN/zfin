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
import java.util.List;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.zfin.util.ZfinCollectionUtils.firstInEachGrouping;


@Log4j2
public class HibernateDiseasePageRepository implements DiseasePageRepository {

    @Override
    public PaginationResult<OmimPhenotypeDisplay> getGenesInvolved(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql;
        if (!includeChildren) {
            hql = "select omimPhenotype from OmimPhenotypeDisplay as omimPhenotype join omimPhenotype.zfinGene as zfinGene where omimPhenotype.disease = :disease ";
        } else {
            hql = "select omimPhenotype from OmimPhenotypeDisplay as omimPhenotype, TransitiveClosure as clo join omimPhenotype.zfinGene as zfinGene " +
                  "where clo.child = omimPhenotype.disease AND clo.root = :disease ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += "order by omimPhenotype.homoSapiensGene.symbol";
        Query<OmimPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, OmimPhenotypeDisplay.class);
        query.setParameter("disease", term);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<FishStatistics> getPhenotype(GenericTerm term, Pagination pagination, Boolean includeChildren, Boolean isIncludeNormalPhenotype) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
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
                select distinct fishStat from FishStatistics as fishStat, TransitiveClosure as clo
                left join fetch fishStat.fish
                left join fetch fishStat.term
                left join fetch fishStat.figure
                left join fetch fishStat.publication
                left join fetch fishStat.affectedGenes
                left join fetch fishStat.phenotypeStatements as phenoStats
                where clo.child = fishStat.term AND clo.root = :term
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
        query.setParameter("term", term);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<FishModelDisplay> getFishDiseaseModels(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql;
        if (!includeChildren) {
            hql = "select fishModelDisplay from FishModelDisplay as fishModelDisplay where fishModelDisplay.disease = :term ";
        } else {
            hql = "select fishModelDisplay from FishModelDisplay as fishModelDisplay, TransitiveClosure as clo " +
                  "where clo.child = fishModelDisplay.disease AND clo.root = :term ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += " order by fishModelDisplay.order, fishModelDisplay.fish.order, upper(fishModelDisplay.fish.displayName) ";
        Query<FishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, FishModelDisplay.class);
        query.setParameter("term", term);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public List<ChebiFishModelDisplay> getFishDiseaseChebiModels(GenericTerm term, boolean includeChildren) {
        String hql;
        if (!includeChildren) {
            hql = "select chebiDisplay from ChebiFishModelDisplay as chebiDisplay " +
                  "where chebiDisplay.chebi = :chebiTerm ";
        } else {
            hql = "select chebiDisplay from ChebiFishModelDisplay as chebiDisplay, TransitiveClosure as clo " +
                  "where  " +
                  "clo.child = chebiDisplay.chebi AND clo.root = :chebiTerm ";
        }
        hql += " order by chebiDisplay.fishModelDisplay.order, chebiDisplay.fishModelDisplay.fish.order, upper(chebiDisplay.fishModelDisplay.fish.displayName) ";
        Query<ChebiFishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, ChebiFishModelDisplay.class);
        query.setParameter("chebiTerm", term);
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
        String hql;
        if (!includeChildren) {
            hql = "select chebiPhenotype from ChebiPhenotypeDisplay as chebiPhenotype where chebiPhenotype.term = :term ";
        } else {
            hql = "select chebiPhenotype from ChebiPhenotypeDisplay as chebiPhenotype, TransitiveClosure as clo  " +
                  "where clo.child = chebiPhenotype.term AND clo.root = :term ";
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
        query.setParameter("term", term);
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
