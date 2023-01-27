package org.zfin.mutant.repository;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.stereotype.Repository;
import org.zfin.expression.*;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.agr.*;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.presentation.TermHistogramBean;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.STRMarkerSequence;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;


/**
 *
 */
@Repository
public class HibernateMutantRepository implements MutantRepository {

    private Logger logger = LogManager.getLogger(HibernateMutantRepository.class);


    /**
     * This returns a list of fish that are annotated
     * to a given anatomy item. Does not include STRs
     *
     * @param item     Anatomy Item
     * @param wildtype return wildtype genotypes
     * @param bean     Pagination bean info
     * @return list of genotypes
     */
    public PaginationResult<Fish> getFishByAnatomyTerm(GenericTerm item, boolean wildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();

        String hql =
            "select distinct fishox.fish , fishox.fish.order, fishox.fish.nameOrder from FishExperiment fishox, " +
                "PhenotypeWarehouse phenoSource, PhenotypeStatementWarehouse phenoObserved " +
                "left outer join phenoObserved.e1a as e1a " +
                "left outer join phenoObserved.e1b as e1b " +
                "left outer join phenoObserved.e2a as e2a " +
                "left outer join phenoObserved.e2b as e2b " +
                "left outer join phenoObserved.quality as quality " +
                "WHERE phenoSource.fishExperiment = fishox " +
                "AND phenoObserved.phenotypeWarehouse = phenoSource " +
                "AND (e1a = :aoTerm " +
                "     or e1b = :aoTerm " +
                "     or e2a = :aoTerm " +
                "     or e2b = :aoTerm) " +
                "AND phenoObserved.tag != :tag " +
                "AND exists (select 'x' from GeneGenotypeExperiment where fishExperiment = fishox) ";
        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                if(entry.getKey().startsWith("fishox")) {
                    hql += " AND lower(fishox.fish.name) like :" + entry.getKey() + " ";
                }
                if(entry.getKey().startsWith("phenotype")) {
                    hql += " AND (";
                    hql += "  (lower(e1a.termName) like :" + entry.getKey() + " ";
                    hql += "  OR ";
                    hql += "  lower(e1b.termName) like :" + entry.getKey() + ") ";
                    hql += "  OR ";
                    hql += "  (lower(e2a.termName) like :" + entry.getKey() + " ";
                    hql += "  OR ";
                    hql += "  lower(e2b.termName) like :" + entry.getKey() + ") ";
                    hql += "  OR ";
                    hql += "  lower(quality.termName) like :" + entry.getKey();
                    hql += " )";
                }
            }
        }
        hql += "ORDER BY fishox.fish.order, fishox.fish.nameOrder ";

        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue().toLowerCase() + "%");
            }
        }

        // have to add extra select because of ordering, but we only want to return the first
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple[0];
            }
        });

        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<Fish> getDirtyFishByAnatomyTerm(GenericTerm item, boolean wildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();

        String hql =
            "select distinct fishox.fish, fishox.fish.order, fishox.fish.nameOrder from FishExperiment fishox, " +
                "PhenotypeExperiment phenox, PhenotypeStatement phenoeq " +
                "WHERE phenox.fishExperiment = fishox " +
                "AND phenoeq.phenotypeExperiment = phenox " +
                "AND (phenoeq.entity.superterm = :aoTerm " +
                "     or phenoeq.entity.subterm = :aoTerm " +
                "     or phenoeq.relatedEntity.superterm = :aoTerm " +
                "     or phenoeq.relatedEntity.subterm = :aoTerm) " +
                "AND phenoeq.tag != :tag " +
                "AND fishox.standardOrGenericControl = :standardOrGeneric " +
                "AND size(fishox.fish.strList) = 0  ";

        if (!wildtype) {
            hql += "AND fishox.fish.genotype.wildtype = 'f' ";
        }
        hql += "ORDER BY fishox.fish.order, fishox.fish.nameOrder ";

        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        query.setParameter("standardOrGeneric", false);
        // have to add extra select because of ordering, but we only want to return the first
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple[0];
            }
        });

        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    /**
     * This returns a list genotypes (mutants) that are annotated
     * to a given anatomy item or any substructure.
     *
     * @param item     Anatomy Item
     * @param wildtype return wildtype genotypes
     * @param bean     Pagination bean info
     * @return list of genotypes
     */
    @Override
    public PaginationResult<Fish> getFishByAnatomyTermIncludingSubstructures(GenericTerm item, boolean wildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();

        String hql =
            "select distinct fishox.fish , fishox.fish.order, fishox.fish.nameOrder from FishExperiment fishox, " +
                "PhenotypeWarehouse phenoSource, PhenotypeStatementWarehouse phenoObserved," +
                "TransitiveClosure transitiveClosure " +
                "left outer join phenoObserved.e1a as e1a " +
                "left outer join phenoObserved.e1b as e1b " +
                "left outer join phenoObserved.e2a as e2a " +
                "left outer join phenoObserved.e2b as e2b " +
                "left outer join phenoObserved.quality as quality " +
                "WHERE phenoSource.fishExperiment = fishox " +
                "AND phenoObserved.phenotypeWarehouse = phenoSource " +
                "AND transitiveClosure.root = :aoTerm and " +
                "(e1a = transitiveClosure.child OR e1b = transitiveClosure.child OR " +
                " e2a = transitiveClosure.child OR e2b = transitiveClosure.child OR quality = transitiveClosure.child ) " +
                "AND phenoObserved.tag != :tag " +
                "AND exists (select 'x' from GeneGenotypeExperiment where fishExperiment = fishox) ";
        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                if(entry.getKey().startsWith("fishox")) {
                    hql += " AND lower(fishox.fish.name) like :" + entry.getKey() + " ";
                }
                if(entry.getKey().startsWith("phenotype")) {
                    hql += " AND (";
                    hql += "  lower(e1a.termName) like :" + entry.getKey() + " ";
                    hql += "  OR ";
                    hql += "  lower(e1b.termName) like :" + entry.getKey() + " ";
                    hql += "  OR ";
                    hql += "  lower(e2a.termName) like :" + entry.getKey() + " ";
                    hql += "  OR ";
                    hql += "  lower(e2b.termName) like :" + entry.getKey() + " ";
                    hql += "  OR ";
                    hql += "  lower(quality.termName) like :" + entry.getKey() + " ";
                    hql += ") ";
                }
            }
        }
        hql += "ORDER BY fishox.fish.order, fishox.fish.nameOrder ";

        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        // have to add extra select because of ordering, but we only want to return the first
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple[0];
            }
        });
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @SuppressWarnings({"unchecked"})
    public List<Genotype> getGenotypesByFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select  distinct geno from Genotype geno, GenotypeFeature genofeat " +
            "WHERE  genofeat.feature = :feature " +
            "AND genofeat.genotype =geno " +
            "ORDER BY geno.nameOrder";

        Query query = session.createQuery(hql);
        query.setParameter("feature", feature);
        return (List<Genotype>) query.list();
    }


    public List<GenotypeFeature> getGenotypeFeaturesByGenotype(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql =
            "select genoFeat from GenotypeFeature genoFeat, Genotype geno, Feature feat " +
                "WHERE  geno.zdbID= :zdbID " +
                "AND genoFeat.genotype =geno " +
                "AND genoFeat.feature =feat " +
                "ORDER by feat.abbreviationOrder";


        Query query = session.createQuery(hql);
        query.setString("zdbID", genotype.getZdbID());
        List<GenotypeFeature> genotypeFeatures = query.list();
        return genotypeFeatures;
    }

    public List<GenotypeFeature> getGenotypeFeaturesByGenotype(String genotypeID) {
        Genotype genotype = new Genotype();
        genotype.setZdbID(genotypeID);
        return getGenotypeFeaturesByGenotype(genotype);
    }

    public GenotypeFeature getGenotypeFeature(String genoFeatId) {
        return (GenotypeFeature) HibernateUtil.currentSession().get(GenotypeFeature.class, genoFeatId);
    }

    public PaginationResult<FishGenotypeFeature> getFishByFeature(String featureId, boolean excludeFishWithSTR, Pagination pagination) {
        String excludeStrClause = "";
        if (excludeFishWithSTR) {
            excludeStrClause = "where str_count = 0 ";
        }

        Query query = HibernateUtil.currentSession().createSQLQuery(
            "select * " +
                "from ( " +
                "    select " +
                "      self.genofeat_zdb_id, " +
                "      fish_zdb_id, " +
                "      fish_name_order, " +
                "      zyg_name, " +
                "      string_agg(distinct affected_gene.mrkr_abbrev_order, ',' order by affected_gene.mrkr_abbrev_order) as affected_list, " +
                "      count(distinct affected_gene.mrkr_zdb_id) as affected_count, " +
                "      count(distinct fish_str.mrkr_zdb_id) as str_count, " +
                "      count(distinct other.genofeat_zdb_id) as feature_count " +
                "    from fish " +
                "      inner join genotype_feature self on self.genofeat_geno_zdb_id = fish_genotype_zdb_id " +
                "      inner join zygocity on genofeat_zygocity = zyg_zdb_id " +
                "      left outer join fish_components on fc_fish_zdb_id = fish_zdb_id " +
                "      left outer join marker affected_gene on fc_gene_zdb_id = affected_gene.mrkr_zdb_id " +
                "      left outer join marker fish_str on fc_affector_zdb_id = fish_str.mrkr_zdb_id " +
                "      left outer join genotype_feature other on other.genofeat_geno_zdb_id = fish_genotype_zdb_id " +
                "    where self.genofeat_feature_zdb_id = :feature_id " +
                "    group by self.genofeat_zdb_id, fish_zdb_id, zyg_name " +
                ") as sub " +
                excludeStrClause +
                "order by " +
                "  ( " +
                "    case " +
                "      when feature_count > 1 then 4 " +
                "      when str_count > 0 then 4 " +
                "      when zyg_name = 'homozygous' then 1 " +
                "      when zyg_name = 'heterozygous' then 2 " +
                "      when zyg_name = 'unknown' then 3 " +
                "      when zyg_name = 'complex' then 4 " +
                "      else 5 " +
                "    end " +
                "  ) asc, " +
                "  affected_count asc, " +
                "  affected_list asc, " +
                "  fish_name_order asc;"
        );
        query.setParameter("feature_id", featureId);
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public FishGenotypeFeature transformTuple(Object[] objects, String[] strings) {
                String genotypeFeatureId = (String) objects[0];
                String fishId = (String) objects[1];

                GenotypeFeature genotypeFeature = getGenotypeFeature(genotypeFeatureId);
                Fish fish = getFish(fishId);

                return new FishGenotypeFeature(fish, genotypeFeature);
            }
        });

        return PaginationResultFactory.createResultFromScrollableResultAndClose(pagination.getStart(), pagination.getEnd(), query.scroll());
    }

    public int getNumberOfImagesPerAnatomyAndMutant(GenericTerm term, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(distinct image) from Image image, Figure fig, ExpressionResult res, " +
            "                                  ExpressionExperiment exp " +
            "where " +
            "res member of exp.expressionResults AND " +
            "res.entity.superterm = :term AND " +
            "fig member of res.figures AND " +
            "image member of fig.images AND " +
            "res.expressionFound = :expressionFound AND " +
            "exp.fishExperiment.fish.genotype.zdbID = :genoZdbID ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setParameter("term", term);
        query.setString("genoZdbID", genotype.getZdbID());

        return ((Number) query.uniqueResult()).intValue();
    }

    public int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(GenericTerm item, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(distinct figure.publication) from Figure figure, ExpressionResult res, " +
            "                                               ExpressionExperiment exp " +
            "where " +
            "res member of exp.expressionResults AND " +
            "res.entity.superterm = :term AND " +
            "figure member of res.figures AND " +
            "res.expressionFound = :expressionFound AND " +
            "exp.fishExperiment.fish.genotype.zdbID = :genoZdbID ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setParameter("term", item);
        query.setString("genoZdbID", genotype.getZdbID());

        return ((Number) query.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    /**
     * @param name go term name
     * @return A unique Go Term.
     */
    public List<GenericTerm> getQualityTermsByName(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenericTerm.class);
        criteria.add(Restrictions.like("termName", "%" + name + "%"));
        criteria.add(Restrictions.eq("ontology", Ontology.QUALITY));
        return criteria.list();
    }

    /**
     * Retrieve a genotype object by PK.
     *
     * @param genotypeZbID pk
     * @return genotype
     */
    public Genotype getGenotypeByID(String genotypeZbID) {
        Session session = HibernateUtil.currentSession();
        return (Genotype) session.get(Genotype.class, genotypeZbID);
    }

    public Genotype getGenotypeByHandle(String handle) {
        Criteria criteria = currentSession().createCriteria(Genotype.class);
        criteria.add(Restrictions.eq("handle", handle));

        return (Genotype) criteria.uniqueResult();
    }

    public Genotype getGenotypeByName(String name) {
        Criteria criteria = currentSession().createCriteria(Genotype.class);
        criteria.add(Restrictions.eq("name", name));

        return (Genotype) criteria.uniqueResult();
    }


    public List<String> getDeletedMarkerLG(Feature feat) {
        Session session = HibernateUtil.currentSession();


        String hql = "select  distinct mapdel.lg from MappedDeletion mapdel, Marker m, Feature f" +
            " where f.name =mapdel.allele " +
            " AND mapdel.marker = m " +
            " AND f.name=:ftr " +
            " AND m.markerType.name =:type ";
        Query query = session.createQuery(hql);
        query.setString("ftr", feat.getName());
        query.setString("type", Marker.Type.GENE.toString());
        return (List<String>) query.list();
    }

    /**
     * Check if for a given figure annotation a pato record (Phenotype)
     *
     * @param efs expressionFigureStage entity
     * @return boolean
     */
    public boolean isPatoExists(ExpressionFigureStage efs) {

        Session session = HibernateUtil.currentSession();

        String hql = "select count(phenox) from PhenotypeExperiment phenox " +
            "     where phenox.fishExperiment = :fishExperiment" +
            "           and phenox.startStage = :start " +
            "           and phenox.endStage = :endStage " +
            "           and phenox.figure = :figure ";

        Query query = session.createQuery(hql);
        query.setParameter("fishExperiment", efs.getExpressionExperiment().getFishExperiment());
        query.setParameter("start", efs.getStartStage());
        query.setParameter("endStage", efs.getEndStage());
        query.setParameter("figure", efs.getFigure());

        long numberOfRecords = (Long) query.uniqueResult();
        return (numberOfRecords > 0);
    }

    /**
     * Lookup a term by name. Term must not be obsolete.
     *
     * @param termName term name
     * @return Term object
     */
    @SuppressWarnings("unchecked")
    public GenericTerm getQualityTermByName(String termName) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(GenericTerm.class);
        crit.add(Restrictions.eq("termName", termName));
        crit.add(Restrictions.eq("obsolete", false));
        return (GenericTerm) crit.uniqueResult();
    }

    /**
     * Retrieve a genotype experiment by PK.
     *
     * @param genotypeExperimentID pk
     * @return genotype experiment
     */
    public FishExperiment getGenotypeExperiment(String genotypeExperimentID) {
        Session session = HibernateUtil.currentSession();
        return (FishExperiment) session.get(FishExperiment.class, genotypeExperimentID);
    }

    public FishExperiment getGenotypeExperiment(String genotypeZdbID, String experimentZdbID) {
        Query query = HibernateUtil.currentSession().createQuery("" +
            "from FishExperiment fe " +
            "where fe.fish.genotype.zdbID = :genoID " +
            "and fe.experiment.zdbID = :expID");
        query.setParameter("genoID", genotypeZdbID);
        query.setParameter("expID", experimentZdbID);
        return (FishExperiment) query.uniqueResult();
    }

    /**
     * Remove a PhenotypeExperiment record:
     * 1) All matching phenotype statements
     * 2) The genotype experiment if left unused after removing phenotypes.
     *
     * @param phenoExperiment Mutants
     */
    public void deletePhenotypeExperiment(PhenotypeExperiment phenoExperiment) {
        if (phenoExperiment == null) {
            throw new NullPointerException("No PhenotypeExperiment provided");
        }
        FishExperiment fishExperiment = phenoExperiment.getFishExperiment();
        if (fishExperiment == null || fishExperiment.getZdbID() == null) {
            throw new NullPointerException("No genotype experiment provided");
        }

        Set<PhenotypeStatement> phenotypes = phenoExperiment.getPhenotypeStatements();

        // delete all phenotype statements records.
        Session session = HibernateUtil.currentSession();
        if (phenotypes != null) {
            for (PhenotypeStatement phenotype : phenotypes) {
                session.delete(phenotype);
            }
        }
        session.delete(phenoExperiment);

        // delete genotype experiment if it has no more phenotypes associated
        // and if it is not used in FX (expression_experiment)
        if (fishExperiment.getExpressionExperiments() == null) {
            session.delete(fishExperiment);
        }

       /* String hql = "delete GenotypeFigure where phenotypeExperiment = :phenoExp ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("phenoExp", phenoExperiment);
        query.executeUpdate();*/
    }


    @SuppressWarnings({"unchecked"})
    public List<Genotype> getGenotypesForStandardAttribution(Publication publication) {
        String hql = "select distinct g from PublicationAttribution pa , Genotype g " +
            " where pa.dataZdbID=g.zdbID and pa.publication.zdbID= :pubZdbID  " +
            " and pa.sourceType= :sourceType  " +
            " order by g.handle ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
    }

    /**
     * Go terms attributed as evidence to this marker by this pub.
     * todo: Don't include IEA, IC .
     *
     * @param marker      marker
     * @param publication publication
     * @return collection of terms
     */
    @SuppressWarnings({"unchecked"})
    public List<GenericTerm> getGoTermsByMarkerAndPublication(Marker marker, Publication publication) {
        String hql = "select distinct g from PublicationAttribution pa , GenericTerm g , MarkerGoTermEvidence ev " +
            " where pa.dataZdbID=ev.zdbID " +
            " and pa.publication.zdbID= :pubZdbID  " +
            " and ev.marker.zdbID = :markerZdbID " +
            " and ev.evidenceCode.code not in (:excludedEvidenceCodes) " +
            " and g.zdbID = ev.goTerm.zdbID " +
            " and pa.sourceType= :sourceType  " +
            " order by g.termName ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setParameterList("excludedEvidenceCodes", new String[]{GoEvidenceCodeEnum.IEA.name(), GoEvidenceCodeEnum.IC.name()});
        query.setString("markerZdbID", marker.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
    }

    /**
     * Go terms attributed as evidence to this marker by this pub.
     * todo: Don't include IEA, IC .
     *
     * @param publication
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public List<GenericTerm> getGoTermsByPhenotypeAndPublication(Publication publication) {
        String hql = "select distinct phenotype from PhenotypeStatement phenotype , GenericTerm g  " +
            " where phenotype.phenotypeExperiment.figure.publication.zdbID= :pubZdbID  " +
            " and ( phenotype.entity.subterm = g and g.oboID like :oboIDLike" +
            " or phenotype.entity.superterm = g or phenotype.relatedEntity.superterm = g OR phenotype.relatedEntity.subterm = g)";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setString("oboIDLike", "GO:%");
        return query.list();
    }

    public InferenceGroupMember addInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inferenceToAdd) {
        InferenceGroupMember inferenceGroupMember = new InferenceGroupMember();
        inferenceGroupMember.setMarkerGoTermEvidenceZdbID(markerGoTermEvidence.getZdbID());
        inferenceGroupMember.setInferredFrom(inferenceToAdd);
        if (markerGoTermEvidence.getInferredFrom() == null) {
            markerGoTermEvidence.setInferredFrom(new HashSet<InferenceGroupMember>());
        }
        markerGoTermEvidence.getInferredFrom().add(inferenceGroupMember);

        HibernateUtil.currentSession().save(inferenceGroupMember);
        return inferenceGroupMember;
    }

    public void removeInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inference) {
        for (Iterator<InferenceGroupMember> iterator = markerGoTermEvidence.getInferredFrom().iterator(); iterator.hasNext(); ) {
            if (iterator.next().getInferredFrom().equals(inference)) {
                iterator.remove();
                Criteria criteria2 = HibernateUtil.currentSession().createCriteria(InferenceGroupMember.class);
                criteria2.add(Restrictions.eq("inferredFrom", inference));
                criteria2.add(Restrictions.eq("markerGoTermEvidenceZdbID", markerGoTermEvidence.getZdbID()));
                InferenceGroupMember inferenceGroupMember = (InferenceGroupMember) criteria2.uniqueResult();
                HibernateUtil.currentSession().delete(inferenceGroupMember);
                return;
            }
        }

    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Genotype> getGenotypesForAttribution(String publicationZdbID) {
        String hql = "" +
            " select distinct g from Genotype g, RecordAttribution ra " +
            " where ra.dataZdbID=g.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
            " order by g.handle" +
            " ";

        return (List<Genotype>) HibernateUtil.currentSession().createQuery(hql)
            .setString("pubZdbID", publicationZdbID)
            .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
            .list();
    }

    public FeatureAlias getSpecificDataAlias(Feature feature, String alias) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(FeatureAlias.class);
        criteria.add(Restrictions.eq("feature", feature));
        criteria.add(Restrictions.eq("alias", alias));
        return (FeatureAlias) criteria.uniqueResult();
    }

    public FeatureDBLink getSpecificDBLink(Feature feature, String accessionNumber) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct ftrDbLink from FeatureDBLink ftrDbLink  where " +
            " ftrDbLink.feature = :feature ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("feature", feature);
        return (FeatureDBLink) query.uniqueResult();


    }


    public int getZFINInferences(String zdbID, String publicationZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery("" +
                " SELECT count(*) FROM marker_go_term_evidence  ev  " +
                " JOIN  inference_group_member inf ON ev.mrkrgoev_zdb_id=inf.infgrmem_mrkrgoev_zdb_id " +
                " WHERE " +
                " ev.mrkrgoev_source_zdb_id=:pubZdbID " +
                " AND " +
                " inf.infgrmem_inferred_from=:zdbID " +
                " ")
            .setString("zdbID", InferenceCategory.ZFIN_GENE.prefix() + zdbID)
            .setString("pubZdbID", publicationZdbID)
            .uniqueResult().toString()
        );
    }

    /**
     * Uses an alternate key that also includes the "inferrence" field, as well, which makes things kind
     * of tricky.
     *
     * @param markerGoTermEvidence Evidence to check against.
     * @return True if the exact version exists for this alternate key.
     */
    @SuppressWarnings("unchecked")
    @Override
    public int getNumberMarkerGoTermEvidences(MarkerGoTermEvidence markerGoTermEvidence) {
        String hql = " " +
            " select ev from MarkerGoTermEvidence ev " +
            " where ev.marker = :marker " +
            " and ev.goTerm = :goTerm " +
            " and ev.source = :publication " +
            " and ev.evidenceCode = :evidenceCode ";

        if (markerGoTermEvidence.getFlag() != null) {
            hql += " and ev.flag = :flag ";
        } else {
            hql += " and ev.flag is null ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql)
            .setParameter("marker", markerGoTermEvidence.getMarker())
            .setParameter("goTerm", markerGoTermEvidence.getGoTerm())
            .setParameter("publication", markerGoTermEvidence.getSource())
            .setString("evidenceCode", markerGoTermEvidence.getEvidenceCode().getCode());

        if (markerGoTermEvidence.getFlag() != null) {
            query.setParameter("flag", markerGoTermEvidence.getFlag());
        }
        List<MarkerGoTermEvidence> evidences = (List<MarkerGoTermEvidence>) query.list();

        if (evidences == null || evidences.size() == 0) {
            return 0;
        }

        int returnCount = 0;

        // compare the inferences
        // could use equals, but if an update has been flushed, the zdbIDs will be set, so can't use
        // that comparison anymore.
        for (MarkerGoTermEvidence evidence : evidences) {
            if (markerGoTermEvidence.sameInferences(evidence.getInferredFrom())) {
                ++returnCount;
            }
        }

        return returnCount;
    }


    @Override
    public List<BasicPhenotypeDTO> getBasicPhenotypeDTOObjects() {
        final String alleleQueryString = "select distinct fmrel1.fmrel_ftr_zdb_id, psg_short_name, zdb_id, accession_no as accession_no," +
            "                e1a.term_ont_id as psg_e1a_id, e1b.term_ont_id as psg_e1b_id, e2a.term_ont_id as psg_e2a_id, e2b.term_ont_id as psg_e2b_id, quality.term_ont_id as psg_quality_id, fish_zdb_id" +
            "                from feature_marker_relationship fmrel1" +
            "                join genotype_feature on fmrel_ftr_zdb_id = genofeat_feature_zdb_id" +
            "                join fish on genofeat_geno_zdb_id = fish_genotype_zdb_id" +
            "                join fish_experiment on fish_zdb_id = genox_fish_zdb_id" +
            "                join mutant_fast_search on mfs_genox_zdb_id = genox_zdb_id" +
            "                join phenotype_source_generated on pg_genox_zdb_id = genox_zdb_id" +
            "                join phenotype_observation_generated on psg_pg_id = pg_id" +
            "                join figure on fig_zdb_id = pg_fig_zdb_id" +
            "                join publication on fig_source_zdb_id = zdb_id" +
            "                join term as e1a on psg_e1a_zdb_id = e1a.term_zdb_id" +
            "                join term as quality on psg_quality_zdb_id = quality.term_zdb_id" +
            "                left outer join term as e1b on e1b.term_zdb_id = psg_e1b_zdb_id" +
            "                left outer join term as e2a on e2a.term_zdb_id = psg_e2a_zdb_id" +
            "                left outer join term as e2b on e2b.term_zdb_id = psg_e2b_zdb_id" +
            "                  where get_obj_type(fmrel1.fmrel_ftr_zdb_id) = 'ALT'" +
            "                  and fmrel1.fmrel_type = 'is allele of' and not exists" +
            "                               (select 'x' from feature_marker_relationship fmrel2" +
            "                                     where fmrel1.fmrel_zdb_id != fmrel2.fmrel_zdb_id and fmrel1.fmrel_ftr_zdb_id = fmrel2.fmrel_ftr_zdb_id" +
            "                                     and fmrel2.fmrel_type != 'created by')";

        final String geneQueryString = "select distinct mfs_data_zdb_id, psg_short_name, zdb_id, accession_no as accession_no," +
            "                e1a.term_ont_id as psg_e1a_id, e1b.term_ont_id as psg_e1b_id, e2a.term_ont_id as psg_e2a_id, " +
            "                e2b.term_ont_id as psg_e2b_id, quality.term_ont_id as psg_quality_id, genox_fish_zdb_id" +
            "                        from mutant_fast_search" +
            "                        join phenotype_source_generated on pg_genox_zdb_id = mfs_genox_zdb_id" +
            "                        join fish_experiment on mfs_genox_zdb_id = genox_zdb_id" +
            "                        join phenotype_observation_generated on psg_pg_id = pg_id" +
            "                        join figure on pg_fig_zdb_id = fig_zdb_id" +
            "                        join publication on fig_source_zdb_id = zdb_id " +
            "                        join term as e1a on e1a.term_zdb_id = psg_e1a_zdb_id" +
            "                        join term as quality on psg_quality_zdb_id = quality.term_zdb_id" +
            "                        left outer join term as e1b on e1b.term_zdb_id = psg_e1b_zdb_id" +
            "                        left outer join term as e2a on e2a.term_zdb_id = psg_e2a_zdb_id" +
            "                        left outer join term as e2b on e2b.term_zdb_id = psg_e2b_zdb_id" +
            "                        where get_obj_type(mfs_data_zdb_id) not in ('CRISPR','TALEN','MRPHLNO')";

        final String fishQueryString = "select distinct fish.fish_zdb_id, psg_short_name, zdb_id, accession_no as accession_no," +
            "                e1a.term_ont_id as psg_e1a_id, e1b.term_ont_id as psg_e1b_id, e2a.term_ont_id as psg_e2a_id, e2b.term_ont_id as psg_e2b_id, quality.term_ont_id as psg_quality_id, " +
            "                genox_zdb_id" +
            "                from fish fish " +
            "                join fish_experiment on fish_zdb_id = genox_fish_zdb_id" +
            "                join phenotype_source_generated on pg_genox_zdb_id = genox_zdb_id" +
            "                join phenotype_observation_generated on psg_pg_id = pg_id" +
            "                join figure on fig_zdb_id = pg_fig_zdb_id" +
            "                join publication on fig_source_zdb_id = zdb_id" +
            "                join term as e1a on psg_e1a_zdb_id = e1a.term_zdb_id" +
            "                join term as quality on psg_quality_zdb_id = quality.term_zdb_id" +
            "                left outer join genotype_feature on genofeat_geno_zdb_id = genox_fish_zdb_id" +
            "                left outer join feature_marker_relationship on fmrel_ftr_zdb_id = genofeat_feature_zdb_id" +
            "                left outer join term as e1b on e1b.term_zdb_id = psg_e1b_zdb_id" +
            "                left outer join term as e2a on e2a.term_zdb_id = psg_e2a_zdb_id" +
            "                left outer join term as e2b on e2b.term_zdb_id = psg_e2b_zdb_id";

        final Query alleleQuery = HibernateUtil.currentSession().createSQLQuery(alleleQueryString);
        final Query geneQuery = HibernateUtil.currentSession().createSQLQuery(geneQueryString);
        final Query fishQuery = HibernateUtil.currentSession().createSQLQuery(fishQueryString);

        List<Object[]> alleles = alleleQuery.list();
        List<Object[]> genes = geneQuery.list();
        List<Object[]> fishes = fishQuery.list();
        List<Object[]> phenos = genes;
        phenos.addAll(alleles);
        phenos.addAll(fishes);


        List<BasicPhenotypeDTO> basicPhenos = new ArrayList<>();
        for (Object[] basicPhenoObjects : phenos) {
            List<String> primaryGeneticEntityIDs = new ArrayList<>();
            BasicPhenotypeDTO basicPheno = new BasicPhenotypeDTO();

            if (basicPhenoObjects[0].toString().startsWith("ZDB-ALT") || basicPhenoObjects[0].toString().startsWith("ZDB-GENE")) {
                if (basicPhenoObjects[9] != null) {
                    primaryGeneticEntityIDs.add("ZFIN:" + basicPhenoObjects[9].toString());
                    basicPheno.setPrimaryGeneticEntityIDs(primaryGeneticEntityIDs);
                }
            } else {
                List<ConditionRelationDTO> conditionsN = new ArrayList<>();
                String fishox = basicPhenoObjects[9].toString();
                if (fishox != null && fishox.startsWith("ZDB-GENOX")) {
                    FishExperiment fishExperiment = getMutantRepository().getFishExperiment(fishox);
                    if (fishExperiment != null) {
                        List<ExperimentCondition> allConditions = getMutantRepository().getExperimentConditions(fishExperiment.getExperiment());
                        if (!allConditions.isEmpty()) {
                            ConditionRelationDTO relation = new ConditionRelationDTO();
                            relation.setConditionRelationType("has_condition");
                            List<ExperimentConditionDTO> expconds = new ArrayList<>();
                            for (ExperimentCondition condition : allConditions) {
                                String conditionStatement = condition.getZecoTerm().getTermName();
                                ExperimentConditionDTO expcond = new ExperimentConditionDTO();
                                if (condition.getAoTerm() != null) {
                                    conditionStatement = conditionStatement + " " + condition.getAoTerm().getTermName();
                                    expcond.setAnatomicalOntologyId(condition.getAoTerm().getOboID());
                                }
                                if (condition.getChebiTerm() != null) {
                                    expcond.setChemicalOntologyId(condition.getChebiTerm().getOboID());
                                    conditionStatement = conditionStatement + " " + condition.getChebiTerm().getTermName();
                                }
                                if (condition.getGoCCTerm() != null) {
                                    expcond.setGeneOntologyId(condition.getGoCCTerm().getOboID());
                                    conditionStatement = conditionStatement + " " + condition.getGoCCTerm().getTermName();
                                }
                                if (condition.getTaxaonymTerm() != null) {
                                    expcond.setNcbiTaxonId(condition.getTaxaonymTerm().getOboID());
                                    conditionStatement = conditionStatement + " " + condition.getTaxaonymTerm().getTermName();
                                }
                                expcond.setConditionClassId(condition.getZecoTerm().getOboID());
                                expcond.setConditionStatement(conditionStatement);
                                expconds.add(expcond);
                            }
                            relation.setConditions(expconds);
                            conditionsN.add(relation);
                        }
                    }
                }
                if (!conditionsN.isEmpty()) {
                    basicPheno.setConditionRelations(conditionsN);
                }
            }

            basicPheno.setObjectId("ZFIN:" + basicPhenoObjects[0].toString());
            basicPheno.setPhenotypeStatement(basicPhenoObjects[1].toString());
            PublicationAgrDTO pubDto = new PublicationAgrDTO();

            if (basicPhenoObjects[3] != null) {
                Integer pubMedId = (Integer) basicPhenoObjects[3];
                pubDto.setPublicationId("PMID:" + pubMedId);
                List<String> pubPages = new ArrayList<>();
                pubPages.add("reference");
                String pubZdbId = basicPhenoObjects[2].toString();
                pubDto.setCrossReference(new CrossReferenceDTO("ZFIN", pubZdbId, pubPages));
                basicPheno.setEvidence(pubDto);
            } else {
                String pubZdbId = basicPhenoObjects[2].toString();
                if (pubZdbId != null) {
                    pubDto.setPublicationId("ZFIN:" + pubZdbId);
                }
            }

            basicPheno.setEvidence(pubDto);

            PhenotypeTermIdentifierDTO termIdentifierE1aDTO = new PhenotypeTermIdentifierDTO();
            PhenotypeTermIdentifierDTO termIdentifierE1bDTO = new PhenotypeTermIdentifierDTO();
            PhenotypeTermIdentifierDTO termIdentifierE2aDTO = new PhenotypeTermIdentifierDTO();
            PhenotypeTermIdentifierDTO termIdentifierE2bDTO = new PhenotypeTermIdentifierDTO();
            PhenotypeTermIdentifierDTO termIdentifierQualityDTO = new PhenotypeTermIdentifierDTO();


            int termOrderCounter = 1;
            List<PhenotypeTermIdentifierDTO> termIdentifiers = new ArrayList<>();


            termIdentifierE1aDTO.setTermId(basicPhenoObjects[4].toString());
            termIdentifierE1aDTO.setTermOrder(termOrderCounter++);
            termIdentifiers.add(termIdentifierE1aDTO);

            if (basicPhenoObjects[5] != null && !basicPhenoObjects[5].toString().isEmpty()) {
                termIdentifierE1bDTO.setTermId(basicPhenoObjects[5].toString());
                termIdentifierE1bDTO.setTermOrder(termOrderCounter++);
                termIdentifiers.add(termIdentifierE1bDTO);
            }

            termIdentifierQualityDTO.setTermId(basicPhenoObjects[8].toString());
            termIdentifierQualityDTO.setTermOrder(termOrderCounter++);
            termIdentifiers.add(termIdentifierQualityDTO);

            if (basicPhenoObjects[6] != null && !basicPhenoObjects[6].toString().isEmpty()) {
                termIdentifierE2aDTO.setTermId(basicPhenoObjects[6].toString());
                termIdentifierE2aDTO.setTermOrder(termOrderCounter++);
                termIdentifiers.add(termIdentifierE2aDTO);
            }
            if (basicPhenoObjects[7] != null && !basicPhenoObjects[7].toString().isEmpty()) {
                termIdentifierE2bDTO.setTermId(basicPhenoObjects[7].toString());
                termIdentifierE2bDTO.setTermOrder(termOrderCounter++);
                termIdentifiers.add(termIdentifierE2bDTO);
            }


            basicPheno.setPhenotypeTermIdentifiers(termIdentifiers);

            basicPhenos.add(basicPheno);
        }

        return basicPhenos;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<STRMarkerSequence> getSequenceTargetingReagentsWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = "select m.zdbID ,m.abbreviation, s.sequence   from SequenceTargetingReagent m  " +
            "inner join m.sequence s " +
            "inner join m.firstMarkerRelationships  " +
            "where m.markerType in  (:moType, :crisprType, :talenType) ";

        final Query query = HibernateUtil.currentSession().createQuery(queryString);
        query.setString("moType", Marker.Type.MRPHLNO.toString());
        query.setString("crisprType", Marker.Type.CRISPR.toString());
        query.setString("talenType", Marker.Type.TALEN.toString());

        List<Object[]> sequences = query
            .list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Object[] seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects[0].toString());
            strSequence.setName(seqObjects[1].toString());
            strSequence.setSequence(seqObjects[2].toString());
            strSequences.add(strSequence);
        }
        return strSequences;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<STRMarkerSequence> getMorpholinosWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = "select m.zdbID ,m.abbreviation, s.sequence   from SequenceTargetingReagent m  " +
            "inner join m.sequence s " +
            "inner join m.firstMarkerRelationships  " +
            "where m.markerType in  (:moType) ";

        final Query query = HibernateUtil.currentSession().createQuery(queryString);
        query.setString("moType", Marker.Type.MRPHLNO.toString());

        List<Object[]> sequences = query
            .list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Object[] seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects[0].toString());
            strSequence.setName(seqObjects[1].toString());
            strSequence.setSequence(seqObjects[2].toString());
            strSequences.add(strSequence);
        }
        return strSequences;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<STRMarkerSequence> getCrisprsWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = "select m.zdbID ,m.abbreviation, s.sequence   from SequenceTargetingReagent m  " +
            "inner join m.sequence s " +
            "inner join m.firstMarkerRelationships  " +
            "where m.markerType in  (:crisprType) ";

        final Query query = HibernateUtil.currentSession().createQuery(queryString);
        query.setString("crisprType", Marker.Type.CRISPR.toString());

        List<Object[]> sequences = query
            .list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Object[] seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects[0].toString());
            strSequence.setName(seqObjects[1].toString());
            strSequence.setSequence(seqObjects[2].toString());
            strSequences.add(strSequence);
        }
        return strSequences;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<STRMarkerSequence> getTalensWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = "select m.zdbID ,m.abbreviation, s.sequence   from SequenceTargetingReagent m  " +
            "inner join m.sequence s " +
            "inner join m.firstMarkerRelationships  " +
            "where m.markerType in  (:talenType) ";

        final Query query = HibernateUtil.currentSession().createQuery(queryString);
        query.setString("talenType", Marker.Type.TALEN.toString());

        List<Object[]> sequences = query
            .list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Object[] seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects[0].toString());
            strSequence.setName(seqObjects[1].toString());
            strSequence.setSequence(seqObjects[2].toString());
            strSequences.add(strSequence);
        }
        return strSequences;
    }

    /**
     * Retrieve phenotypes that have an annotation to a given term
     * with tag=abnormal and the term either in super or sub position
     *
     * @param term Term
     * @return list of phenotypes
     */
    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeWithEntity(GenericTerm term) {
        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno where " +
            "(pheno.e1a = :term OR pheno.e1b = :term OR " +
            "pheno.e2a = :term OR pheno.e2b = :term ) " +
            "AND tag = :tag";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        query.setString("tag", PhenotypeStatement.Tag.ABNORMAL.toString());
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    public boolean hasPhenotype(GenericTerm term) {
        String hql = "select count(*) from PhenotypeStatementWarehouse where " +
            "(e1a = :term OR e1b = :term OR " +
            "e2a = :term OR e2b = :term ) " +
            "AND tag = :tag";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        query.setString("tag", PhenotypeStatement.Tag.ABNORMAL.toString());
        return ((long) query.uniqueResult()) > 0;
    }

    /**
     * Retrieve all distinct marker go evidence objects for a given term.
     *
     * @param term term
     * @return list of marker go
     */
    @Override
    public List<MarkerGoTermEvidence> getMarkerGoEvidence(GenericTerm term) {
        String hql = "select distinct evidence from MarkerGoTermEvidence evidence where " +
            " goTerm = :term ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        return (List<MarkerGoTermEvidence>) query.list();
    }

    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeWithEntity(List<GenericTerm> terms) {
        List<PhenotypeStatementWarehouse> allPhenotypes = new ArrayList<>(50);
        for (GenericTerm term : terms) {
            List<PhenotypeStatementWarehouse> phenotypes = getPhenotypeWithEntity(term);
            allPhenotypes.addAll(phenotypes);
        }
        List<PhenotypeStatementWarehouse> nonDuplicateExpressions = removeDuplicates(allPhenotypes);
        Collections.sort(nonDuplicateExpressions);
        return nonDuplicateExpressions;

    }

    @Override
    public List<MarkerGoTermEvidence> getMarkerGoEvidence(List<GenericTerm> terms) {
        List<MarkerGoTermEvidence> allMarkerGo = new ArrayList<MarkerGoTermEvidence>();
        for (GenericTerm term : terms) {
            List<MarkerGoTermEvidence> goTermEvidences = getMarkerGoEvidence(term);
            allMarkerGo.addAll(goTermEvidences);
        }
        List<MarkerGoTermEvidence> uniqueMarkerGo = removeDuplicateMarker(allMarkerGo);
        Collections.sort(uniqueMarkerGo);
        return uniqueMarkerGo;
    }

    private List<MarkerGoTermEvidence> removeDuplicateMarker(List<MarkerGoTermEvidence> goTermEvidences) {
        List<MarkerGoTermEvidence> phenotypeArrayList = new ArrayList<MarkerGoTermEvidence>(goTermEvidences.size());
        Set<String> uniqueID = new HashSet<String>();
        for (MarkerGoTermEvidence evidence : goTermEvidences) {
            String id = evidence.getMarker().getZdbID() + ":" + evidence.getGoTerm().getZdbID();
            if (!uniqueID.contains(id)) {
                phenotypeArrayList.add(evidence);
                uniqueID.add(id);
            }
        }
        return phenotypeArrayList;
    }


    private List<PhenotypeStatementWarehouse> removeDuplicates(List<PhenotypeStatementWarehouse> allPhenotypes) {
        Set<PhenotypeStatementWarehouse> phenos = new HashSet<>();
        for (PhenotypeStatementWarehouse pheno : allPhenotypes) {
            phenos.add(pheno);
        }
        ArrayList<PhenotypeStatementWarehouse> phenotypeArrayList = new ArrayList<>(phenos.size());
        phenotypeArrayList.addAll(phenos);
        return phenotypeArrayList;
    }

    public PhenotypeExperiment getPhenotypeExperiment(Long id) {
        return (PhenotypeExperiment) currentSession().get(PhenotypeExperiment.class, id);
    }

    /**
     * get a phenotype statement by id
     */
    public PhenotypeStatement getPhenotypeStatementById(Long id) {
        Session session = currentSession();
        return (PhenotypeStatement) session.get(PhenotypeStatement.class, id);
    }

    public PhenotypeStatementWarehouse getPhenotypeStatementWarehouseById(Long id) {
        return (PhenotypeStatementWarehouse) currentSession().get(PhenotypeStatementWarehouse.class, id);
    }


    public PhenotypeWarehouse getPhenotypeWarehouseById(Long id) {
        return (PhenotypeWarehouse) currentSession().get(PhenotypeWarehouse.class, id);
    }

    /**
     * Retrieve the phenotypes that are annotated with obsoleted terms.
     *
     * @return list of phenotypes
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesOnObsoletedTerms() {
        return getPhenotypesOnObsoletedTerms(null);
    }

    /**
     * Returns a list phenotype statements that are related to
     * a given genotype.
     *
     * @param genotype Genotype
     * @return list of phenotype statement objects
     */
    public List<PhenotypeStatementWarehouse> getPhenotypeStatementsByGenotype(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct phenoStatement from PhenotypeStatementWarehouse phenoStatement " +
            "WHERE phenoStatement.phenotypeWarehouse.fishExperiment.fish.genotype = :genotype";

        Query query = session.createQuery(hql);
        query.setParameter("genotype", genotype);

        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    public List<PhenotypeStatementWarehouse> getPhenotypeStatementsByFishExperiment(FishExperiment fishExperiment) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct phenoStatement from PhenotypeStatementWarehouse phenoStatement " +
            "WHERE phenoStatement.phenotypeWarehouse.fishExperiment = :fishExperiment";

        Query query = session.createQuery(hql);
        query.setParameter("fishExperiment", fishExperiment);

        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    /**
     * Returns list of phenotype statements that are annotated with a term marked secondary.
     *
     * @return list of phenotype statements.
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesOnSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        List<PhenotypeStatement> allPhenotypes = new ArrayList<PhenotypeStatement>();

        String hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.quality is not null AND phenotype.quality.secondary = :secondary";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);

        allPhenotypes.addAll((List<PhenotypeStatement>) query.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.entity.superterm is not null AND phenotype.entity.superterm.secondary = :secondary";
        Query queryEntitySuper = session.createQuery(hql);
        queryEntitySuper.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.entity.subterm is not null AND phenotype.entity.subterm.secondary = :secondary";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySub.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.relatedEntity.superterm is not null AND phenotype.relatedEntity.superterm.secondary = :secondary";
        Query queryRelatedEntitySuper = session.createQuery(hql);
        queryRelatedEntitySuper.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.relatedEntity.subterm is not null AND phenotype.relatedEntity.subterm.secondary = :secondary";
        Query queryRelatedEntitySub = session.createQuery(hql);
        queryRelatedEntitySub.setBoolean("secondary", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySub.list());

        return allPhenotypes;
    }

    /**
     * Returns list of MarkerGoTermEvidence statements that are annotated with a term marked obsolete.
     *
     * @return list of MarkerGoTermEvidence statements.
     */
    @Override
    public List<MarkerGoTermEvidence> getGoEvidenceOnObsoletedTerms() {
        Session session = HibernateUtil.currentSession();
        String hql = "select goEvidence from MarkerGoTermEvidence goEvidence " +
            "     where goEvidence.goTerm.obsolete = :obsolete";
        Query query = session.createQuery(hql);
        query.setBoolean("obsolete", true);

        return (List<MarkerGoTermEvidence>) query.list();
    }

    /**
     * Retrieve a histogram of phenotype terms usage.
     *
     * @return list of histograms
     */
    @Override
    public Map<TermHistogramBean, Long> getTermPhenotypeUsage() {
        Session session = HibernateUtil.currentSession();
        Map<TermHistogramBean, Long> histogram = new TreeMap<TermHistogramBean, Long>();

        // retrieve entity.superterm
        String hql = "select phenotype.entity.superterm.zdbID, phenotype.entity.superterm.termName," +
            "count(phenotype.entity.superterm) from PhenotypeStatement phenotype" +
            "     group by phenotype.entity.superterm.zdbID,  phenotype.entity.superterm.termName" +
            " order by count(phenotype.entity.superterm) desc";
        Query query = session.createQuery(hql);
        List<Object[]> result = query.list();
        if (result != null) {
            for (Object[] row : result) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row[0]);
                termUsageHistogram.setTermName((String) row[1]);
                addToHistogram(termUsageHistogram, (Long) row[2], histogram);
            }
        }
        // retrieve entity.subterm
        hql = "select phenotype.entity.subterm.zdbID, phenotype.entity.subterm.termName," +
            "count(phenotype.entity.superterm) from PhenotypeStatement phenotype" +
            "     group by phenotype.entity.subterm.zdbID,  phenotype.entity.subterm.termName" +
            " order by count(phenotype.entity.subterm) desc";
        Query queryEntitySubterm = session.createQuery(hql);
        List<Object[]> resultEntitySubterm = queryEntitySubterm.list();
        if (resultEntitySubterm != null) {
            for (Object[] row : resultEntitySubterm) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row[0]);
                termUsageHistogram.setTermName((String) row[1]);
                addToHistogram(termUsageHistogram, (Long) row[2], histogram);
            }
        }
        // retrieve related entity.superterm
        hql = "select phenotype.relatedEntity.superterm.zdbID, phenotype.relatedEntity.superterm.termName," +
            "count(phenotype.relatedEntity.superterm) from PhenotypeStatement phenotype" +
            "     group by phenotype.relatedEntity.superterm.zdbID,  phenotype.relatedEntity.superterm.termName" +
            " order by count(phenotype.relatedEntity.superterm) desc";
        Query queryRelatedEntitySuperbterm = session.createQuery(hql);
        List<Object[]> resultRelatedEntitySuperterm = queryRelatedEntitySuperbterm.list();
        if (resultRelatedEntitySuperterm != null) {
            for (Object[] row : resultRelatedEntitySuperterm) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row[0]);
                termUsageHistogram.setTermName((String) row[1]);
                addToHistogram(termUsageHistogram, (Long) row[2], histogram);
            }
        }
        // retrieve related entity.subterm
        hql = "select phenotype.relatedEntity.subterm.zdbID, phenotype.relatedEntity.subterm.termName," +
            "count(phenotype.relatedEntity.superterm) from PhenotypeStatement phenotype" +
            "     group by phenotype.relatedEntity.subterm.zdbID,  phenotype.relatedEntity.subterm.termName" +
            " order by count(phenotype.relatedEntity.subterm) desc";
        Query queryRelatedEntitySubterm = session.createQuery(hql);
        List<Object[]> resultRelatedEntitySubterm = queryRelatedEntitySubterm.list();
        if (resultRelatedEntitySubterm != null) {
            for (Object[] row : resultRelatedEntitySubterm) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row[0]);
                termUsageHistogram.setTermName((String) row[1]);
                addToHistogram(termUsageHistogram, (Long) row[2], histogram);
            }
        }
        return histogram;
    }

    private void addToHistogram(TermHistogramBean termUsageHistogram, Long number, Map<TermHistogramBean, Long> histogram) {
        Long termUsage = histogram.get(termUsageHistogram);
        if (termUsage != null) {
            histogram.put(termUsageHistogram, termUsage + number);
        } else {
            histogram.put(termUsageHistogram, number);
        }
    }

    @Override
    public List<Feature> getAllelesForMarker(String zdbID, String type) {
        String hql = "select distinct " +
            "feat from FeatureMarkerRelationship fmrel, Feature feat " +
            "where fmrel.marker.zdbID= :zdbID " +
            "and fmrel.type=:type " +
            "and fmrel.feature = feat " +
            "order by feat.abbreviationOrder";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("zdbID", zdbID);
        query.setParameter("type", type);
        return query.list();

    }

    @Override
    public List<Marker> getKnockdownReagents(Marker gene) {
        String hql = " select m from Marker m join m.firstMarkerRelationships mr " +
            " where mr.secondMarker.zdbID = :markerZdbId  " +
            " and mr.markerRelationshipType.name = :type " +
            " order by m.abbreviationOrder " +
            "";
        return HibernateUtil.currentSession().createQuery(hql)
            .setString("markerZdbId", gene.getZdbID())
            .setString("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE.toString())
            .list();
    }

    @Override
    public List<Genotype> getTransgenicLinesForConstruct(Marker construct) {

        Session session = HibernateUtil.currentSession();

        String hql = "select distinct genofeat.genotype from GenotypeFeature genofeat, FeatureMarkerRelationship featMarker " +
            "where featMarker.marker = :marker " +
            "and genofeat.feature = featMarker.feature ";

        Query query = session.createQuery(hql);
        query.setParameter("marker", construct);
        return (List<Genotype>) query.list();
    }

    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param fishExperimentIDs genox ids
     * @return list of phenotype statements
     */
    public List<PhenotypeStatement> getPhenotypeStatementsByGenotypeExperiments(List<String> fishExperimentIDs) {
        String hql = " from PhenotypeStatement where " +
            "phenotypeExperiment.fishExperiment.zdbID in (:fishoxIds)";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        return query.list();
    }

    public List<PhenotypeStatement> getPhenotypeStatementsByFish(Fish fish) {
        String hql = " from PhenotypeStatement where " +
            "phenotypeExperiment.fishExperiment.fish.zdbID = :fishZdbId";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fishZdbId", fish.getZdbID());
        return query.list();
    }

    public List<PhenotypeStatementWarehouse> getPhenotypeStatementWarehousesByFish(Fish fish) {
        String hql = " from PhenotypeStatementWarehouse where " +
            "phenotypeWarehouse.fishExperiment.fish.zdbID = :fishZdbId";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fishZdbId", fish.getZdbID());
        return query.list();
    }


    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param genotypeExperimentIDs genox ids
     * @return list of expression statements
     */
    @Override
    public List<ExpressionStatement> getExpressionStatementsByGenotypeExperiments(Set<FishExperiment> genotypeExperimentIDs) {
        String hql = " from ExpressionResult where " +
            "expressionExperiment.fishExperiment in (:genoxIds)";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("genoxIds", genotypeExperimentIDs);
        return query.list();
    }


    public Set<String> getGenoxAttributions(List<String> fishExperimentIDs) {
        String hql = "select distinct publication.zdbID from ExpressionExperiment where " +
            " fishExperiment.zdbID in (:fishoxIds)";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        List<String> pubIds = (List<String>) query.list();
        Set<String> distinctPubs = new HashSet<String>(pubIds.size());
        distinctPubs.addAll(pubIds);

        // phenotype experiments
        hql = "select distinct figure.publication.zdbID from PhenotypeExperiment where " +
            " fishExperiment.zdbID in (:fishoxIds)";
        query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        pubIds = (List<String>) query.list();
        distinctPubs.addAll(pubIds);

        // experiments
        hql = "select distinct experiment.publication.zdbID from FishExperiment where " +
            " zdbID in (:fishoxIds)";
        query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        pubIds = (List<String>) query.list();
        distinctPubs.addAll(pubIds);

        return distinctPubs;
    }

    /**
     * Retrieve citation list of pubs for fish annotations.
     *
     * @param fish
     * @return
     */
    public List<Publication> getFishAttributionList(Fish fish) {
        Session session = HibernateUtil.currentSession();

        // direct attribution
        String hql = "select p.publication " +
            " from PublicationAttribution p " +
            " where p.dataZdbID = :fishZdbID ";

        Query query = session.createQuery(hql);
        query.setString("fishZdbID", fish.getZdbID());
        return (List<Publication>) query.list();
    }

    /**
     * Retrieve sequence targeting reagent by its id
     *
     * @param sequenceTargetingReagentID equence targeting reagent by its id
     * @return SequenceTargetingReagent object
     */
    public SequenceTargetingReagent getSequenceTargetingReagentByID(String sequenceTargetingReagentID) {
        return (SequenceTargetingReagent) HibernateUtil.currentSession().get(SequenceTargetingReagent.class, sequenceTargetingReagentID);
    }

    /**
     * Retrieve all wildtype genotypes.
     *
     * @return
     */
    @Override
    public List<Genotype> getAllWildtypeGenotypes() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Genotype.class);
        criteria.add(Restrictions.eq("wildtype", true));
        criteria.addOrder(Order.asc("nameOrder"));
        return criteria.list();

    }

    /**
     * Retrieve a list of expression result records that show expression data for a given fish
     *
     * @return
     */
    @Override
    public List<ExpressionResult> getExpressionSummary(Set<FishExperiment> fishOx, String geneID) {
        if (CollectionUtils.isEmpty(fishOx)) {
            return null;
        }

        String hql = " select distinct expressionResult from ExpressionResult expressionResult where " +

            " expressionResult.expressionExperiment.fishExperiment in (:fishOx) AND ";
        if (geneID == null) {
            hql += " expressionResult.expressionExperiment.gene is not null";
        } else {
            hql += " expressionResult.expressionExperiment.gene = :geneID";
        }
        Query query = HibernateUtil.currentSession().createQuery(hql);

        query.setParameterList("fishOx", fishOx);
        if (geneID != null) {
            query.setString("geneID", geneID);
        }
        return query.list();
    }

    public List<ExperimentCondition> getExperimentConditions(Experiment experiment) {
        return HibernateUtil.currentSession()
            .createCriteria(ExperimentCondition.class)
            .add(Restrictions.eq("experiment", experiment))
            .list();
    }

    public List<ExperimentCondition> getExperimentConditionsByExp(Experiment experiment) {


        String hql = " select distinct expressionResult from ExpressionResult expressionResult where " +
            " expressionResult.expressionExperiment.fishExperiment.experiment= :exp AND " +
            " expressionResult.expressionExperiment.gene is not null";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("exp", experiment);

        return query.list();

    }

    ;


    public List<ExpressionResult> getConstructExpressionSummary(List<String> genoxIds) {
        if (CollectionUtils.isEmpty(genoxIds)) {
            return null;
        }

        String hql = " select distinct expressionResult from ExpressionResult expressionResult where " +
            " expressionResult.expressionExperiment.fishExperiment.zdbID in (:genoxIds) AND " +
            " expressionResult.expressionExperiment.gene is not null";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("genoxIds", genoxIds);

        return query.list();
    }


    /**
     * Check if a given fish has expression data with at least one figure that has an image.
     *
     * @return
     */
    //sierra do this one
    @Override
    public boolean hasImagesOnExpressionFigures(String genotypeID, Set<FishExperiment> fishOx) {
        if (CollectionUtils.isEmpty(fishOx) || StringUtils.isEmpty(genotypeID)) {
            return false;
        }
        List fishOxList = new ArrayList(fishOx);

        String sql = "SELECT count(*) FROM figure, expression_experiment, expression_result, expression_pattern_figure, fish_experiment,fish" +
            "  WHERE fig_zdb_id = xpatfig_fig_zdb_id" +
            " AND xpatex_zdb_id = xpatres_xpatex_zdb_id" +
            " AND xpatres_zdb_id = xpatfig_xpatres_zdb_id" +
            " AND genox_fish_zdb_id = fish_zdb_id" +
            " AND fish_genotype_zdb_id = :genotypeID" +
            " AND xpatex_genox_zdb_id = genox_zdb_id" +
            " AND genox_zdb_id IN (:fishOxList)" +
            " AND exists (SELECT 'x' FROM image WHERE img_fig_Zdb_id = fig_zdb_id)";

        Query query = currentSession().createSQLQuery(sql);
        query.setString("genotypeID", genotypeID);
        query.setParameterList("fishOxList", fishOxList);

        return (((Number) query.uniqueResult()).longValue() > 0);
    }


    public List<PhenotypeStatementWarehouse> getPhenotypeStatementForMutantSummary(GenericTerm term, Genotype genotype, boolean includeSubstructures) {
        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeTermFastSearch fastSearch " +
            "where fastSearch.phenotypeStatement = pheno and " +
            "pheno.phenotypeWarehouse.fishExperiment.fish.genotype = :genotype and " +
            "fastSearch.directAnnotation = :directAnnotation " +
            "AND not exists (select 1 from ExperimentCondition cond where" +
            " cond.experiment = pheno.phenotypeWarehouse.fishExperiment.experiment " +
            " AND cond.sequenceTargetingReagent is not null ) ";

        if (term != null) {
            hql += " and fastSearch.term = :term ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (term != null) {
            query.setParameter("term", term);
        }
        query.setParameter("genotype", genotype);
        query.setBoolean("directAnnotation", !includeSubstructures);
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeStatementObservedForMutantSummary(GenericTerm term, Fish fish, boolean includeSubstructures) {
        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeTermFastSearch fastSearch " +
            "where fastSearch.phenotypeObserved = pheno and " +
            "pheno.phenotypeWarehouse.fishExperiment.fish = :fish and " +
            "fastSearch.directAnnotation = :directAnnotation ";

        if (term != null) {
            hql += " and fastSearch.term = :term ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (term != null) {
            query.setParameter("term", term);
        }
        query.setParameter("fish", fish);
        query.setBoolean("directAnnotation", !includeSubstructures);
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    @Override
    public List<DiseaseAnnotationModel> getDiseaseAnnotationModels(int numfOfRecords) {
        String hql = " from DiseaseAnnotationModel model " +
            "join fetch model.fishExperiment " +
            "join fetch model.fishExperiment.fish " +
            "join fetch model.fishExperiment.experiment " +
            "join fetch model.diseaseAnnotation " +
            "join fetch model.diseaseAnnotation.disease " +
            "join fetch model.diseaseAnnotation.publication ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        return (List<DiseaseAnnotationModel>) query.list();

    }

    @Override
    public List<DiseaseAnnotationModel> getDiseaseAnnotationModelsNoStd(int numfOfRecords) {
        String hql = " from DiseaseAnnotationModel model " +
            "where model.fishExperiment.standardOrGenericControl=false";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        return (List<DiseaseAnnotationModel>) query.list();

    }

    @Override
    public List<GeneGenotypeExperiment> getGeneDiseaseAnnotationModels(int numfOfRecords) {
        String hql = "select distinct geneGenotype from GeneGenotypeExperiment geneGenotype, DiseaseAnnotationModel diseaseAnnotationModel " +
            "join fetch geneGenotype.gene " +
            "join fetch geneGenotype.fishExperiment " +
            "where geneGenotype.fishExperiment = diseaseAnnotationModel.fishExperiment " +
            "and geneGenotype.gene.markerType.name = :genedom ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        //query.setString("genedom", "ZDB-GENE")
        query.setParameter("genedom", Marker.Type.GENE.toString());
        return (List<GeneGenotypeExperiment>) query.list();
    }


    @Override
    public List<GeneGenotypeExperiment> getGeneFishWithPhenotypes(int numfOfRecords) {
        String hql = "select distinct geneGenotype from GeneGenotypeExperiment geneGenotype, PhenotypeStatement phenox " +
            "join fetch geneGenotype.gene " +
            "join fetch geneGenotype.fishExperiment " +
            "where geneGenotype.fishExperiment = phenox.fishExperiment " +
            "and geneGenotype.gene.markerType.name = :genedom ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        //query.setString("genedom", "ZDB-GENE")
        query.setParameter("genedom", Marker.Type.GENE.toString());
        return (List<GeneGenotypeExperiment>) query.list();
    }

    @Override
    public List<PhenotypeObservationStatement> getPhenotypeStatements(String geneID, String termIDs) {
        String[] split = termIDs.split(",");
        String hql = "from PhenotypeObservationStatement as phenoObservation " +
            //"where phenoObservation.gene.zdbID = :geneID " +
            "WHERE phenoObservation.id in (:ids) ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("ids", Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList()));

        return query.list();
    }


    @Override
    public List<OmimPhenotype> getDiseaseModelsFromGenes(int numfOfRecords) {
        String hql = " from OmimPhenotype model " +
            "where model.externalReferences is not empty " +
            "order by model.ortholog.zebrafishGene.abbreviationOrder";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        return (List<OmimPhenotype>) query.list();
    }

    public List<Genotype> getGenotypes(List<String> genotypeExperimentIDs) {
        String hql = "select distinct " +
            "fish.genotype from  FishExperiment genoExp " +
            "where genoExp.zdbID in (:genoxIDs)" +
            " and genoExp.standardOrGenericControl=false";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("genoxIDs", genotypeExperimentIDs);
        return query.list();


    }

    /**
     * Retrieve list of phenotype statements that use obsoleted terms for given ontology.
     *
     * @param ontology ontology
     * @return list of phenotype statements
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesOnObsoletedTerms(Ontology ontology) {
        boolean individualOnly = ontology != null;

        Session session = HibernateUtil.currentSession();
        List<PhenotypeStatement> allPhenotypes = new ArrayList<PhenotypeStatement>();

        if (!individualOnly || ontology.equals(Ontology.QUALITY)) {
            String hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.quality is not null AND phenotype.quality.obsolete = :obsolete";
            Query query = session.createQuery(hql);
            query.setBoolean("obsolete", true);
            allPhenotypes.addAll((List<PhenotypeStatement>) query.list());
            if (individualOnly)
                return allPhenotypes;
        }

        List<String> ontologyList = null;
        if (individualOnly)
            ontologyList = ontology.getIndividualOntologies().stream().map(Ontology::getDbOntologyName).collect(Collectors.toList());
        String hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.entity.superterm is not null AND phenotype.entity.superterm.obsolete = :obsolete ";
        if (individualOnly) {
            hql += "      AND phenotype.entity.superterm.ontology in (:ontologyList)";
        }
        Query queryEntitySuper = session.createQuery(hql);
        queryEntitySuper.setBoolean("obsolete", true);
        if (individualOnly) {
            queryEntitySuper.setParameterList("ontologyList", ontologyList);
        }

        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.entity.subterm is not null AND phenotype.entity.subterm.obsolete = :obsolete";
        if (individualOnly) {
            hql += "      AND phenotype.entity.subterm.ontology in (:ontologyList)";
        }
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("obsolete", true);
        if (individualOnly) {
            queryEntitySub.setParameterList("ontologyList", ontologyList);
        }
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySub.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.relatedEntity.superterm is not null AND phenotype.relatedEntity.superterm.obsolete = :obsolete";
        if (individualOnly) {
            hql += "      AND phenotype.relatedEntity.superterm.ontology in (:ontologyList)";
        }
        Query queryRelatedEntitySuper = session.createQuery(hql);
        queryRelatedEntitySuper.setBoolean("obsolete", true);
        if (individualOnly) {
            queryRelatedEntitySuper.setParameterList("ontologyList", ontologyList);
        }
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.relatedEntity.subterm is not null AND phenotype.relatedEntity.subterm.obsolete = :obsolete";
        if (individualOnly) {
            hql += "      AND phenotype.relatedEntity.subterm.ontology in (:ontologyList)";
        }
        Query queryRelatedEntitySub = session.createQuery(hql);
        queryRelatedEntitySub.setBoolean("obsolete", true);
        if (individualOnly) {
            queryRelatedEntitySub.setParameterList("ontologyList", ontologyList);
        }
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySub.list());

        return allPhenotypes;
    }


    @Override
    public List<PhenotypeStatement> getPhenotypeStatementsByMarker(Marker gene) {

        Session session = HibernateUtil.currentSession();

        String hql = "select distinct phenoStatement from PhenotypeStatement phenoStatement, GeneGenotypeExperiment geneGenox " +
            "WHERE geneGenox.gene = :gene and " +
            "phenoStatement.phenotypeExperiment.fishExperiment = geneGenox.fishExperiment " +
            "and phenoStatement.tag = :tag";
        Query query = session.createQuery(hql);
        query.setParameter("gene", gene);
        query.setParameter("tag", "abnormal");

        return (List<PhenotypeStatement>) query.list();

    }


    public List<GenotypeFigure> getGenotypeFiguresBySTR(SequenceTargetingReagent str) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct genoFig from GenotypeFigure genoFig " +
            "     where genoFig.sequenceTargetingReagent.zdbID = :zdbID";
        Query query = session.createQuery(hql);
        query.setParameter("zdbID", str.getZdbID());

        List<GenotypeFigure> genotypeFigures = (List<GenotypeFigure>) query.list();

        List<GenotypeFigure> notNormalGenotypeFigures = genotypeFigures.stream()
            .filter(genotypeFigure -> genotypeFigure.getPhenotypeStatement().isNotNormal())
            .collect(Collectors.toList());
        return notNormalGenotypeFigures;
    }

    @Override
    public List<SequenceTargetingReagent> getStrList(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select str from SequenceTargetingReagent str , PublicationAttribution attrib " +
            "     where attrib.publication.zdbID = :publicationID AND " +
            "attrib.dataZdbID = str.zdbID " +
            "order by str.abbreviationOrder";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);

        return (List<SequenceTargetingReagent>) query.list();
    }

    @Override
    public boolean createFishIfNotExists(Fish fish, Publication publication) {
        Fish existingFish = getFishByGenoStr(fish);
        boolean newFishCreated = false;
        if (existingFish == null) {
            HibernateUtil.currentSession().save(fish);
            getInfrastructureRepository().insertUpdatesTable(fish, "fish_zdb_id", "create new record", publication.getZdbID(), null);
            newFishCreated = true;
            getInfrastructureRepository().insertRecordAttribution(fish, publication);
        } else {
            getInfrastructureRepository().insertRecordAttribution(existingFish, publication);
        }
        return newFishCreated;
    }

    @Override
    public Fish getFishByGenoStr(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = "select fish from Fish fish " +
            "     where fish.genotype = :genotype ";
        boolean strsAvailable = CollectionUtils.isNotEmpty(fish.getStrList());
        if (strsAvailable) {
            int index = 0;
            for (SequenceTargetingReagent ignored : fish.getStrList())
                hql += " AND :str_" + index++ + " member of fish.strList ";
        }
        hql += " AND (select count(str.id) from Fish zfish" +
            " inner join zfish.strList str " +
            " where zfish.zdbID = fish.zdbID) = :numberOfStrs";

        Query query = session.createQuery(hql);
        query.setParameter("genotype", fish.getGenotype());
        if (strsAvailable) {
            query.setInteger("numberOfStrs", fish.getStrList().size());
        } else {
            query.setInteger("numberOfStrs", 0);
        }
        if (strsAvailable) {
            int index = 0;
            for (SequenceTargetingReagent str : fish.getStrList())
                query.setParameter("str_" + index++, str);
        }
        List<Fish> fishList = query.list();
        session.flush();
        if (CollectionUtils.isEmpty(fishList)) {
            return null;
        } else if (fishList.size() == 1) {
            return fishList.get(0);
        }
        throw new IllegalStateException("Found more than one Fish " + fish);

    }

    @Override

    public void createDiseaseModel(DiseaseAnnotation diseaseAnnotation) {
        DiseaseAnnotation existsDiseaseAnnotation = getMutantRepository().getDiseaseModel(diseaseAnnotation);
        if (existsDiseaseAnnotation == null) {
            HibernateUtil.currentSession().save(diseaseAnnotation);
            getInfrastructureRepository().insertRecordAttribution(diseaseAnnotation.getZdbID(), diseaseAnnotation.getPublication().getZdbID());
        }


        //DiseaseAnnotation existingDiseaseAnnotation = getMutantRepository().getDiseaseModel(diseaseAnnotation);
    }

    @Override
    public List<Publication> getPublicationWithFish(String fishID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select attrib.publication from PublicationAttribution attrib " +
            "     where attrib.dataZdbID = :fishID ";
        Query query = session.createQuery(hql);
        query.setParameter("fishID", fishID);
        return (List<Publication>) query.list();
    }

    @Override
    public FishExperiment getFishModel(String fishID, String expID) {
        Session session = HibernateUtil.currentSession();

        String hql = "from FishExperiment " +
            "     where fish.zdbID = :fishID and" +
            "     experiment.zdbID = :expID ";
        Query query = session.createQuery(hql);
        query.setParameter("fishID", fishID);
        query.setParameter("expID", expID);
        return (FishExperiment) query.uniqueResult();
    }

    @Override
    public DiseaseAnnotation getDiseaseModel(DiseaseAnnotation diseaseAnnotation) {
        Session session = HibernateUtil.currentSession();

        String hql = "from DiseaseAnnotation" +
            "     where disease = :disease and" +
            "     publication = :publication and " +
            "     evidenceCode = :evidenceCode  ";

        Query query = session.createQuery(hql);
        query.setParameter("disease", diseaseAnnotation.getDisease());
        query.setParameter("publication", diseaseAnnotation.getPublication());
        query.setString("evidenceCode", diseaseAnnotation.getEvidenceCode());

        return (DiseaseAnnotation) query.uniqueResult();
    }

    @Override
    public DiseaseAnnotation getDiseaseModelByID(String zdbID) {
        return (DiseaseAnnotation) HibernateUtil.currentSession().get(DiseaseAnnotation.class, zdbID);
    }

    @Override
    public DiseaseAnnotationModel getDiseaseAnnotationModelByID(Long id) {
        return (DiseaseAnnotationModel) HibernateUtil.currentSession().get(DiseaseAnnotationModel.class, id);
    }

    public List<DiseaseAnnotationModel> getDiseaseAnnotationModelByZdb(String zdb) {
        Query query = HibernateUtil.currentSession().createQuery(
            "from DiseaseAnnotationModel  " +
                "where diseaseAnnotation.zdbID = :zdb ");

        query.setParameter("zdb", zdb);


        return (List<DiseaseAnnotationModel>) query.list();
    }

    @Override
    public void deleteDiseaseModel(DiseaseAnnotation diseaseAnnotation) {
        getInfrastructureRepository().deleteRecordAttributionsForData(diseaseAnnotation.getZdbID());
        getInfrastructureRepository().deleteRecordAttributionsForData(diseaseAnnotation.getDisease().getZdbID());
        HibernateUtil.currentSession().delete(diseaseAnnotation);
    }

    @Override
    public void deleteDiseaseAnnotationModel(DiseaseAnnotationModel diseaseAnnotationModel) {
        HibernateUtil.currentSession().delete(diseaseAnnotationModel);
    }

    @Override
    public List<DiseaseAnnotation> getDiseaseModel(String fishID, String pubID) {
        Query query = HibernateUtil.currentSession().createQuery("select dm " +
            "from DiseaseAnnotation dm,DiseaseAnnotationModel dma " +
            "where dm.publication.zdbID = :pubID " +
            "AND dma.diseaseAnnotation=dm " +
            "AND dma.fishExperiment.fish.zdbID = :fishID");
        query.setParameter("fishID", fishID);
        query.setParameter("pubID", pubID);
        return (List<DiseaseAnnotation>) query.list();
    }

    @Override
    public List<Fish> getFishList(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fish from Fish fish, PublicationAttribution attrib " +
            "     where attrib.publication.zdbID = :publicationID AND " +
            "attrib.dataZdbID = fish.zdbID AND " +
            "attrib.sourceType = :sourceType " +
            "order by fish.nameOrder";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        query.setParameter("sourceType", RecordAttribution.SourceType.STANDARD);
        return (List<Fish>) query.list();
    }

    @Override
    public Fish getFish(String fishID) {
        return (Fish) HibernateUtil.currentSession().get(Fish.class, fishID);
    }

    @Override
    public List<Fish> getFishListBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        Session session = HibernateUtil.currentSession();
        String hql = "select fish from Fish fish " +
            " where :str member of fish.strList " +
            " order by fish.name";
        Query query = session.createQuery(hql);
        query.setParameter("str", sequenceTargetingReagent);
        return (List<Fish>) query.list();
    }

    @Override
    public List<Fish> getAllWildtypeFish() {
        Session session = HibernateUtil.currentSession();
        Criteria fishCriteria = session.createCriteria(Fish.class);
        Criteria criteria = fishCriteria.createCriteria("genotype");
        criteria.add(Restrictions.eq("wildtype", true));
        fishCriteria.add(Restrictions.isEmpty("strList"));
        fishCriteria.addOrder(Order.asc("name"));
        return fishCriteria.list();
    }

    @Override
    public List<Fish> getAllFish() {
        Session session = HibernateUtil.currentSession();
        Criteria fishCriteria = session.createCriteria(Fish.class);
        fishCriteria.addOrder(Order.asc("name"));
        return fishCriteria.list();
    }

    @Override
    public List<SequenceTargetingReagent> getAllSTRs() {
        Session session = HibernateUtil.currentSession();
        Criteria strCriteria = session.createCriteria(SequenceTargetingReagent.class);
        strCriteria.addOrder(Order.asc("name"));
        return strCriteria.list();
    }

    @Override
    public List<String> getFishAliases() {
        Session session = HibernateUtil.currentSession();
        Criteria fishCriteria = session.createCriteria(Fish.class);
        fishCriteria.addOrder(Order.asc("name"));
        return fishCriteria.list();
    }

    @Override
    public List<Fish> getFishBackgrounds() {
        Session session = HibernateUtil.currentSession();
        String hql = "";
        Query query = session.createQuery(hql);
        return (List<Fish>) query.list();
    }

    @Override
    public List<Genotype> getGenotypesByFeatureAndBackground(Feature feature, Genotype background, Publication publication) {
        Session session = HibernateUtil.currentSession();
        String hql = "from GenotypeFeature as gf WHERE ";
        boolean hasFirstClause = false;
        if (feature != null) {
            hql += " gf.feature = :feature  ";
            hasFirstClause = true;
        }
        if (background != null) {
            if (hasFirstClause)
                hql += "AND ";
            hql += " :background member of gf.genotype.associatedGenotypes ";
        }
        if (publication != null) {
            hql += "AND gf.genotype.zdbID not in (select dataZdbID from RecordAttribution " +
                "where publication = :publication and sourceType = :standard) ";
        }
        hql += "ORDER BY gf.genotype.nameOrder ";

        Query query = session.createQuery(hql);
        if (feature != null) {
            query.setParameter("feature", feature);
        }
        if (background != null) {
            query.setParameter("background", background);
        }
        if (publication != null) {
            query.setParameter("publication", publication);
            query.setParameter("standard", RecordAttribution.SourceType.STANDARD);
        }
        List<GenotypeFeature> genotypeFeatureList = (List<GenotypeFeature>) query.list();
        if (genotypeFeatureList == null) {
            return null;
        }
        List<Genotype> genotypeList = new ArrayList<>(genotypeFeatureList.size());
        for (GenotypeFeature genotypeFeature : genotypeFeatureList) {
            genotypeList.add(genotypeFeature.getGenotype());
        }
        return genotypeList;
    }

    public FishExperiment getFishExperiment(String zdbID) {

        Session session = HibernateUtil.currentSession();
        return (FishExperiment) session.get(FishExperiment.class, zdbID);

    }

    @Override
    public List<FishExperiment> getFishExperiment(Genotype genotype) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct fishExperiment, fishExperiment.fish.order FROM  FishExperiment fishExperiment " +
            "WHERE fishExperiment.fish.genotype = :genotype " +
            "order by fishExperiment.fish.order";
        Query query = session.createQuery(hql);
        query.setParameter("genotype", genotype);

        List<Object[]> list = query.list();
        List<FishExperiment> result = new ArrayList<>(list.size());
        for (Object[] o : list) {
            result.add((FishExperiment) o[0]);
        }
        return result;
    }

    @Override
    public List<FishExperiment> getFishExperimentsByFish(Fish fish) {
        Query query = HibernateUtil.currentSession().createQuery("from FishExperiment exp where exp.fish = :fish");
        query.setParameter("fish", fish);
        return query.list();
    }

    @Override
    public List<Zygosity> getListOfZygosity() {
        Session session = HibernateUtil.currentSession();
        String hql = "FROM  Zygosity ";
        Query query = session.createQuery(hql);

        return query.list();
    }

    @Override
    public Zygosity getZygosity(String ID) {
        return (Zygosity) HibernateUtil.currentSession().get(Zygosity.class, ID);
    }

    @Override
    public void saveGenotype(Genotype genotype, String publicationID) {
        Session session = HibernateUtil.currentSession();
        session.save(genotype);
        if (genotype.getGenotypeFeatures() != null) {
            for (GenotypeFeature genoFeature : genotype.getGenotypeFeatures())
                session.save(genoFeature);
        }
        getInfrastructureRepository().insertPublicAttribution(genotype.getZdbID(), publicationID, RecordAttribution.SourceType.STANDARD);
        getInfrastructureRepository().insertUpdatesTable(publicationID, "geno_zdb_id", null, genotype.getZdbID(), "create new record");
    }

    public void updateGenotypeNicknameWithHandleForPublication(Publication publication) {
        HibernateUtil.currentSession().createSQLQuery(
                "UPDATE genotype " +
                    "SET geno_nickname = geno_handle " +
                    "WHERE EXISTS ( " +
                    "  SELECT 'x' " +
                    "  FROM record_attribution " +
                    "  WHERE recattrib_data_zdb_id = geno_zdb_id " +
                    "  AND recattrib_source_zdb_id = :pubID " +
                    ");")
            .setString("pubID", publication.getZdbID())
            .executeUpdate();
    }

    public void updateFishAffectedGeneCount(Fish fish) {
        HibernateUtil.currentSession().createSQLQuery(
                "UPDATE fish " +
                    "SET fish_name = fish_name " +
                    "WHERE fish_Zdb_id=:fishID ;")

            .setString("fishID", fish.getZdbID())
            .executeUpdate();
    }


    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeStatementForMarker(Marker marker) {
        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno, GeneGenotypeExperiment gge " +
            "where pheno.phenotypeWarehouse.fishExperiment = gge.fishExperiment " +
            "AND gge.gene = :gene " +
            "AND pheno.tag != :tag";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("gene", marker);
        query.setParameter("tag", "normal");
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    @Override
    public FishExperiment getFishExperimentByFishAndExperimentID(String fishID, String experimentID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(FishExperiment.class);
        criteria.add(Restrictions.eq("fish.zdbID", fishID));
        criteria.add(Restrictions.eq("experiment.zdbID", experimentID));
        return (FishExperiment) criteria.uniqueResult();
    }

    @Override
    public List<Fish> getFishByGenotype(Genotype genotype) {
        String hql = "select fish from Fish as fish " +
            "where fish.genotype = :genotype";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotype", genotype);
        return query.list();
    }

    @Override
    public String getFishByWTGenotype(Genotype genotype) {
        String hql = "select fish.zdbID from Fish as fish " +
            "where fish.genotype = :genotype and " +
            "fish.wildtype is true";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotype", genotype);
        return query.uniqueResult().toString();
    }

    @Override
    public List<Fish> getFishByGenotypeNoExperiment(Genotype genotype) {
        String hql = "select fish from Fish as fish " +
            "where fish.genotype = :genotype and " +
            "fish.fishExperiments is empty";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotype", genotype);
        return query.list();
    }

    @Override
    public long getFishCountByGenotype(String genotypeID, String publicationID) {
        String hql = "select distinct fish from Fish as fish, PublicationAttribution pubAtt " +
            "where fish.genotype.zdbID = :genotypeID AND " +
            "pubAtt.publication.zdbID = :publicationID AND " +
            "pubAtt.dataZdbID = fish.zdbID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotypeID", genotypeID);
        query.setParameter("publicationID", publicationID);
        List<Fish> fishList = query.list();
        if (fishList == null) {
            return 0;
        }
        return (long) fishList.size();
    }

    @Override
    public long getPhenotypeByFishAndPublication(Fish fish, String publicationID) {
        String hql = "from PhenotypeStatement " +
            "where phenotypeExperiment.fishExperiment.fish = :fish AND " +
            "phenotypeExperiment.figure.publication.zdbID = :publicationID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);
        List<Fish> fishList = query.list();
        if (fishList == null) {
            return 0;
        }
        return (long) fishList.size();
    }

    @Override
    public long getInferredFromCountByGenotype(String genotypeID, String publicationID) {
        String hql = "select count(inferred.markerGoTermEvidenceZdbID) from InferenceGroupMember as inferred, MarkerGoTermEvidence as mgt " +
            "where inferred.inferredFrom = :genotypeID AND " +
            "mgt.source.zdbID = :publicationID AND " +
            "inferred.markerGoTermEvidenceZdbID = mgt.zdbID ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("genotypeID", "ZFIN:" + genotypeID);
        query.setParameter("publicationID", publicationID);
        return (long) query.uniqueResult();
    }

    @Override
    public long getFishExperimentCountByGenotype(Fish fish, String publicationID) {
        String hql = "from PhenotypeExperiment " +
            "where fishExperiment.fish = :fish AND " +
            "figure.publication.zdbID = :publicationID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);
        List<Fish> fishList = query.list();
        if (fishList == null) {
            return 0;
        }
        return (long) fishList.size();
    }

    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeObserved(GenericTerm term, Fish fish, boolean includeSubstructures) {
        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeTermFastSearch fastSearch " +
            "where fastSearch.phenotypeObserved = pheno and " +
            "pheno.phenotypeWarehouse.fishExperiment.fish = :fish and " +
            "fastSearch.directAnnotation = :directAnnotation ";

        if (term != null) {
            hql += " and fastSearch.term = :term ";
        }

        Query query = HibernateUtil.currentSession().createQuery(hql);
        if (term != null) {
            query.setParameter("term", term);
        }
        query.setParameter("fish", fish);
        query.setBoolean("directAnnotation", !includeSubstructures);
        return (List<PhenotypeStatementWarehouse>) query.list();
    }
}
