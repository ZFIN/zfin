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

    // "Including substructures" predicate: the row's own term is the queried term or any of its
    // descendants. all_term_contains (TransitiveClosure) includes the self row, so this covers the
    // direct matches too, and it binds exactly ONE parameter regardless of how many rows match.
    //
    // Do NOT go back to fetching the matching row ids in Java and binding them with `id in :ids`:
    // a wide term produces tens of thousands of ids and Postgres rejects the statement outright --
    // "PreparedStatement can have at most 65,535 parameters ... Given query has 86,604 parameters".
    // Eleven terms currently exceed that limit, which made these endpoints 500 in production.
    private static final String DESCENDANT_TERMS =
        " in (select closure.child.zdbID from TransitiveClosure as closure where closure.root.zdbID = :ancestorTermID) ";

    @Override
    public PaginationResult<OmimPhenotypeDisplay> getGenesInvolved(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql = "select omimPhenotype from OmimPhenotypeDisplay as omimPhenotype join omimPhenotype.zfinGene as zfinGene ";
        if (!includeChildren) {
            hql += "where omimPhenotype.disease = :disease ";
        } else {
            hql += "where omimPhenotype.disease.zdbID" + DESCENDANT_TERMS;
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
            query.setParameter("ancestorTermID", term.getZdbID());
        } else {
            query.setParameter("disease", term);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<FishStatistics> getPhenotype(GenericTerm term, Pagination pagination, Boolean includeChildren, Boolean isIncludeNormalPhenotype) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql = """
            select distinct fishStat from FishStatistics as fishStat
            left join fetch fishStat.fish
            left join fetch fishStat.term
            left join fetch fishStat.figure
            left join fetch fishStat.publication
            left join fetch fishStat.affectedGenes
            left join fetch fishStat.phenotypeStatements as phenoStats
            """;
        if (!includeChildren) {
            hql += "where fishStat.term = :term ";
        } else {
            hql += "where fishStat.term.zdbID" + DESCENDANT_TERMS;
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
        hql += "order by fishStat.fish.order, fishStat.fish.nameOrder, fishStat.geneSymbolSearch, " +
               "fishStat.fish.zdbID, fishStat.figure.zdbID, fishStat.publication.zdbID, fishStat.term.zdbID";
        Query<FishStatistics> query = HibernateUtil.currentSession().createQuery(hql, FishStatistics.class);
        if (includeChildren) {
            query.setParameter("ancestorTermID", term.getZdbID());
        } else {
            query.setParameter("term", term);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<FishModelDisplay> getFishDiseaseModels(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql = "select fishModelDisplay from FishModelDisplay as fishModelDisplay ";
        if (!includeChildren) {
            hql += "where fishModelDisplay.disease = :term ";
        } else {
            hql += "where fishModelDisplay.disease.zdbID" + DESCENDANT_TERMS;
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += " order by fishModelDisplay.order, fishModelDisplay.fish.order, upper(fishModelDisplay.fish.displayName), " +
               "fishModelDisplay.fish.zdbID, fishModelDisplay.experiment.zdbID, fishModelDisplay.singlePublication.zdbID, fishModelDisplay.disease.zdbID ";
        Query<FishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, FishModelDisplay.class);
        if (includeChildren) {
            query.setParameter("ancestorTermID", term.getZdbID());
        } else {
            query.setParameter("term", term);
        }
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public List<ChebiFishModelDisplay> getFishDiseaseChebiModels(GenericTerm term, boolean includeChildren) {
        String hql = "select chebiDisplay from ChebiFishModelDisplay as chebiDisplay ";
        if (!includeChildren) {
            hql += "where chebiDisplay.chebi = :chebiTerm ";
        } else {
            hql += "where chebiDisplay.chebi.zdbID" + DESCENDANT_TERMS;
        }
        hql += " order by chebiDisplay.fishModelDisplay.order, chebiDisplay.fishModelDisplay.fish.order, upper(chebiDisplay.fishModelDisplay.fish.displayName), " +
               "chebiDisplay.fishModelDisplay.fish.zdbID, chebiDisplay.fishModelDisplay.experiment.zdbID, chebiDisplay.fishModelDisplay.singlePublication.zdbID, chebiDisplay.chebi.zdbID ";
        Query<ChebiFishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, ChebiFishModelDisplay.class);
        if (includeChildren) {
            query.setParameter("ancestorTermID", term.getZdbID());
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
        String fetchJoins = " join fetch chebiPhenotype.fish" +
                " join fetch chebiPhenotype.term" +
                " left join fetch chebiPhenotype.figure" +
                " left join fetch chebiPhenotype.publication" +
                " left join fetch chebiPhenotype.experiment";
        String hql = "select chebiPhenotype from ChebiPhenotypeDisplay as chebiPhenotype" + fetchJoins;
        if (!includeChildren) {
            hql += " where chebiPhenotype.term = :term ";
        } else {
            hql += " where chebiPhenotype.term.zdbID" + DESCENDANT_TERMS;
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
        hql += "order by chebiPhenotype.fish.displayName, " +
               "chebiPhenotype.fish.zdbID, chebiPhenotype.figure.zdbID, chebiPhenotype.publication.zdbID, chebiPhenotype.experiment.zdbID, chebiPhenotype.term.zdbID";
        Query<ChebiPhenotypeDisplay> query = HibernateUtil.currentSession().createQuery(hql, ChebiPhenotypeDisplay.class);
        if (includeChildren) {
            query.setParameter("ancestorTermID", term.getZdbID());
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
