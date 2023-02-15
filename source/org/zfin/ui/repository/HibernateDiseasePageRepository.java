package org.zfin.ui.repository;

import org.apache.commons.collections4.MapUtils;
import org.hibernate.query.Query;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.OmimPhenotypeDisplay;
import org.zfin.repository.PaginationResultFactory;

import java.util.List;

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
    public PaginationResult<FishStatistics> getPhenotype(GenericTerm term, Pagination pagination, boolean includeChildren) {
        PaginationBean bean = PaginationBean.getPaginationBean(pagination);
        String hql;
        if (!includeChildren) {
            hql = "select fishStat from FishStatistics as fishStat join fishStat.affectedGenes as zfinGene where fishStat.term = :term ";
        } else {
            hql = "select fishStat from FishStatistics as fishStat, TransitiveClosure as clo join fishStat.affectedGenes as zfinGene " +
                "where clo.child = fishStat.term AND clo.root = :term ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (var entry : pagination.getFilterMap().entrySet()) {
                hql += " AND ";
                hql += "LOWER(" + entry.getKey() + ") like '%" + entry.getValue().toLowerCase() + "%' ";
            }
        }
        hql += "order by fishStat.geneSymbolSearch";
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
        hql += "order by upper(fishModelDisplay.fish.displayName) ";
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
        hql += " order by upper(chebiDisplay.fishModelDisplay.fish.displayName) ";
        Query<ChebiFishModelDisplay> query = HibernateUtil.currentSession().createQuery(hql, ChebiFishModelDisplay.class);
        query.setParameter("chebiTerm", term);
        List<ChebiFishModelDisplay> list = query.list();
/*
        list.forEach(fishModelDisplay -> {
            fishModelDisplay.getChebiTerms().forEach(chebi -> {
                FishModelChebiDisplay chebiDisplay = new FishModelChebiDisplay();
                chebiDisplay.setChebiTerm(chebi);
                chebiDisplay.setDisease(fishModelDisplay.getDisease());
                chebiDisplay.setFish(fishModelDisplay.getFish());
                chebiDisplay.setConditionSearch(fishModelDisplay.getConditionSearch());
                chebiDisplay.setEvidenceCode(fishModelDisplay.getEvidenceCode());
                returnList.add(chebiDisplay);
            });
        });
*/
        return list;
    }

}
