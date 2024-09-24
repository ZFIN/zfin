package org.zfin.mutant.repository;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.expression.*;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
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

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.database.HibernateUpgradeHelper.setTupleResultTransformer;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;


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
        Session session = currentSession();

        String hql = """
            select distinct fishox.fish , fishox.fish.order, fishox.fish.nameOrder from FishExperiment fishox,
                PhenotypeWarehouse phenoSource, PhenotypeStatementWarehouse phenoObserved
                left outer join phenoObserved.e1a as e1a
                left outer join phenoObserved.e1b as e1b
                left outer join phenoObserved.e2a as e2a
                left outer join phenoObserved.e2b as e2b
                left outer join phenoObserved.quality as quality
                WHERE phenoSource.fishExperiment = fishox
                AND phenoObserved.phenotypeWarehouse = phenoSource
                AND (e1a = :aoTerm
                     or e1b = :aoTerm
                     or e2a = :aoTerm
                     or e2b = :aoTerm)
                AND phenoObserved.tag != :tag
                AND exists (select 'x' from GeneGenotypeExperiment where fishExperiment = fishox)
            """;

        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                if (entry.getKey().startsWith("fishox")) {
                    hql += " AND lower(fishox.fish.name) like :" + entry.getKey() + " ";
                }
                if (entry.getKey().startsWith("phenotype")) {
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

        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue().toLowerCase() + "%");
            }
        }

        // have to add extra select because of ordering, but we only want to return the first
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @Override
    public PaginationResult<Fish> getDirtyFishByAnatomyTerm(GenericTerm item, boolean wildtype, PaginationBean bean) {
        Session session = currentSession();

        String hql =
            """
                select distinct fishox.fish, fishox.fish.order, fishox.fish.nameOrder from FishExperiment fishox, 
                    PhenotypeExperiment phenox, PhenotypeStatement phenoeq 
                    WHERE phenox.fishExperiment = fishox 
                    AND phenoeq.phenotypeExperiment = phenox 
                    AND (phenoeq.entity.superterm = :aoTerm 
                         or phenoeq.entity.subterm = :aoTerm 
                         or phenoeq.relatedEntity.superterm = :aoTerm 
                         or phenoeq.relatedEntity.subterm = :aoTerm) 
                    AND phenoeq.tag != :tag 
                    AND fishox.standardOrGenericControl = :standardOrGeneric 
                    AND size(fishox.fish.strList) = 0 
                """;

        if (!wildtype) {
            hql += " AND fishox.fish.genotype.wildtype = false ";
        }
        hql += " ORDER BY fishox.fish.order, fishox.fish.nameOrder ";

        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        query.setParameter("standardOrGeneric", false);

        // have to add extra select because of ordering, but we only want to return the first
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

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
        Session session = currentSession();

        String hql = """
                select distinct fishox.fish , fishox.fish.order, fishox.fish.nameOrder from FishExperiment fishox, 
                    PhenotypeWarehouse phenoSource, PhenotypeStatementWarehouse phenoObserved,
                    TransitiveClosure transitiveClosure 
                    left outer join phenoObserved.e1a as e1a 
                    left outer join phenoObserved.e1b as e1b 
                    left outer join phenoObserved.e2a as e2a 
                    left outer join phenoObserved.e2b as e2b 
                    left outer join phenoObserved.quality as quality 
                    WHERE phenoSource.fishExperiment = fishox 
                    AND phenoObserved.phenotypeWarehouse = phenoSource 
                    AND transitiveClosure.root = :aoTerm and 
                    (e1a = transitiveClosure.child OR e1b = transitiveClosure.child OR 
                     e2a = transitiveClosure.child OR e2b = transitiveClosure.child OR quality = transitiveClosure.child ) 
                    AND phenoObserved.tag != :tag 
                    AND exists (select 'x' from GeneGenotypeExperiment where fishExperiment = fishox) 
            """;

        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                if (entry.getKey().startsWith("fishox")) {
                    hql += " AND lower(fishox.fish.name) like :" + entry.getKey() + " ";
                }
                if (entry.getKey().startsWith("phenotype")) {
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

        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        if (MapUtils.isNotEmpty(bean.getFilterMap())) {
            for (Map.Entry<String, String> entry : bean.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        // have to add extra select because of ordering, but we only want to return the first
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @SuppressWarnings({"unchecked"})
    public List<Genotype> getGenotypesByFeature(Feature feature) {
        Session session = currentSession();

        String hql = """ 
            select distinct geno from Genotype geno, GenotypeFeature genofeat 
            WHERE genofeat.feature = :feature 
            AND genofeat.genotype = geno 
            ORDER BY geno.nameOrder 
            """;

        Query<Genotype> query = session.createQuery(hql, Genotype.class);
        query.setParameter("feature", feature);
        return (List<Genotype>) query.list();
    }

    @SuppressWarnings({"unchecked"})
    public List<Genotype> getGenotypesByFeatureID(String featureID) {
        Feature feature = getFeatureRepository().getFeatureByID(featureID);
        return getGenotypesByFeature(feature);
    }

    public List<GenotypeFeature> getGenotypeFeaturesByGenotype(Genotype genotype) {
        Session session = currentSession();

        String hql = """
                select genoFeat from GenotypeFeature genoFeat, Genotype geno, Feature feat  
                    WHERE  geno.zdbID = :zdbID  
                    AND genoFeat.genotype = geno  
                    AND genoFeat.feature = feat  
                    ORDER by feat.abbreviationOrder  
            """;

        Query<GenotypeFeature> query = session.createQuery(hql, GenotypeFeature.class);
        query.setParameter("zdbID", genotype.getZdbID());
        List<GenotypeFeature> genotypeFeatures = query.list();
        return genotypeFeatures;
    }

    public List<GenotypeFeature> getGenotypeFeaturesByGenotype(String genotypeID) {
        Genotype genotype = new Genotype();
        genotype.setZdbID(genotypeID);
        return getGenotypeFeaturesByGenotype(genotype);
    }

    public GenotypeFeature getGenotypeFeature(String genoFeatId) {
        return currentSession().get(GenotypeFeature.class, genoFeatId);
    }

    public PaginationResult<FishGenotypeFeature> getFishByFeature(String featureId, boolean excludeFishWithSTR, Pagination pagination) {
        String excludeStrClause = "";
        if (excludeFishWithSTR) {
            excludeStrClause = "where str_count = 0 ";
        }

        String sql =
            """
                select * 
                from ( 
                    select 
                      self.genofeat_zdb_id, 
                      fish_zdb_id, 
                      fish_name_order, 
                      zyg_name, 
                      string_agg(distinct affected_gene.mrkr_abbrev_order, ',' order by affected_gene.mrkr_abbrev_order) as affected_list, 
                      count(distinct affected_gene.mrkr_zdb_id) as affected_count, 
                      count(distinct fish_str.mrkr_zdb_id) as str_count, 
                      count(distinct other.genofeat_zdb_id) as feature_count 
                    from fish 
                      inner join genotype_feature self on self.genofeat_geno_zdb_id = fish_genotype_zdb_id 
                      inner join zygocity on genofeat_zygocity = zyg_zdb_id 
                      left outer join fish_components on fc_fish_zdb_id = fish_zdb_id 
                      left outer join marker affected_gene on fc_gene_zdb_id = affected_gene.mrkr_zdb_id 
                      left outer join marker fish_str on fc_affector_zdb_id = fish_str.mrkr_zdb_id 
                      left outer join genotype_feature other on other.genofeat_geno_zdb_id = fish_genotype_zdb_id 
                    where self.genofeat_feature_zdb_id = :feature_id 
                    group by self.genofeat_zdb_id, fish_zdb_id, zyg_name 
                ) as sub 
                """ +
                excludeStrClause +
                """
                    order by 
                      ( 
                        case 
                          when feature_count > 1 then 4 
                          when str_count > 0 then 4 
                          when zyg_name = 'homozygous' then 1 
                          when zyg_name = 'heterozygous' then 2 
                          when zyg_name = 'unknown' then 3 
                          when zyg_name = 'complex' then 4 
                          else 5 
                        end 
                      ) asc, 
                      affected_count asc, 
                      affected_list asc, 
                      fish_name_order asc;
                    """;

        Query query = currentSession().createNativeQuery(sql);
        query.setParameter("feature_id", featureId);

        setTupleResultTransformer(query, (Object[] tuples, String[] aliases) -> {
            String genotypeFeatureId = (String) tuples[0];
            String fishId = (String) tuples[1];

            GenotypeFeature genotypeFeature = getGenotypeFeature(genotypeFeatureId);
            Fish fish = getFish(fishId);

            return new FishGenotypeFeature(fish, genotypeFeature);
        });

        return PaginationResultFactory.createResultFromScrollableResultAndClose(pagination.getStart(), pagination.getEnd(), query.scroll());
    }

    public int getNumberOfImagesPerAnatomyAndMutant(GenericTerm term, Genotype genotype) {
        Session session = currentSession();

        String hql = """
             select count(distinct image) from Image image, Figure fig, ExpressionResult res, 
                                              ExpressionExperiment exp 
            where 
            res member of exp.expressionResults AND 
            res.entity.superterm = :term AND 
            fig member of res.figures AND 
            image member of fig.images AND 
            res.expressionFound = true AND 
            exp.fishExperiment.fish.genotype.zdbID = :genoZdbID 
            """;
        Query<Number> query = session.createQuery(hql, Number.class);
        query.setParameter("term", term);
        query.setParameter("genoZdbID", genotype.getZdbID());

        return query.uniqueResult().intValue();
    }

    public int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(GenericTerm item, Genotype genotype) {
        Session session = currentSession();

        String hql = """
            select count(distinct figure.publication) from Figure figure, ExpressionResult res, 
                                                           ExpressionExperiment exp 
            where 
            res member of exp.expressionResults AND 
            res.entity.superterm = :term AND 
            figure member of res.figures AND 
            res.expressionFound = true AND 
            exp.fishExperiment.fish.genotype.zdbID = :genoZdbID 
            """;
        Query<Number> query = session.createQuery(hql, Number.class);
        query.setParameter("term", item);
        query.setParameter("genoZdbID", genotype.getZdbID());

        return query.uniqueResult().intValue();
    }

    @SuppressWarnings("unchecked")
    /**
     * @param name go term name
     * @return A unique Go Term.
     */
    public List<GenericTerm> getQualityTermsByName(String name) {
        Session session = currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<GenericTerm> query = cb.createQuery(GenericTerm.class);
        Root<GenericTerm> root = query.from(GenericTerm.class);
        query.where(
            cb.and(
                cb.like(root.get("termName"), "%" + name + "%"),
                cb.equal(root.get("ontology"), Ontology.QUALITY)
            )
        );
        return session.createQuery(query).list();
    }

    /**
     * Retrieve a genotype object by PK.
     *
     * @param genotypeZbID pk
     * @return genotype
     */
    public Genotype getGenotypeByID(String genotypeZbID) {
        Session session = currentSession();
        return session.get(Genotype.class, genotypeZbID);
    }

    public Genotype getGenotypeByHandle(String handle) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Genotype> query = cb.createQuery(Genotype.class);
        Root<Genotype> genotypeRoot = query.from(Genotype.class);
        query.where(cb.equal(genotypeRoot.get("handle"), handle));
        return currentSession().createQuery(query).uniqueResult();
    }

    public Genotype getGenotypeByName(String name) {
        CriteriaBuilder cb = currentSession().getCriteriaBuilder();
        CriteriaQuery<Genotype> query = cb.createQuery(Genotype.class);
        Root<Genotype> genotypeRoot = query.from(Genotype.class);
        query.where(cb.equal(genotypeRoot.get("name"), name));
        return currentSession().createQuery(query).uniqueResult();
    }


    public List<String> getDeletedMarkerLG(Feature feat) {
        Session session = currentSession();


        String hql = """ 
                    select  distinct mapdel.lg from MappedDeletion mapdel, Marker m, Feature f
                 where f.name =mapdel.allele 
                 AND mapdel.marker = m 
                 AND f.name=:ftr 
                 AND m.markerType.name =:type
            """;

        Query<String> query = session.createQuery(hql, String.class);
        query.setParameter("ftr", feat.getName());
        query.setParameter("type", Marker.Type.GENE.toString()); //...markerType.name is a String
        return (List<String>) query.list();
    }

    /**
     * Check if for a given figure annotation a pato record (Phenotype)
     *
     * @param efs expressionFigureStage entity
     * @return boolean
     */
    public boolean isPatoExists(ExpressionFigureStage efs) {

        Session session = currentSession();

        String hql = """
                    select count(phenox) from PhenotypeExperiment phenox 
                     where phenox.fishExperiment = :fishExperiment
                           and phenox.startStage = :start 
                           and phenox.endStage = :endStage 
                           and phenox.figure = :figure
            """;


        Query<Number> query = session.createQuery(hql, Number.class);
        query.setParameter("fishExperiment", efs.getExpressionExperiment().getFishExperiment());
        query.setParameter("start", efs.getStartStage());
        query.setParameter("endStage", efs.getEndStage());
        query.setParameter("figure", efs.getFigure());

        long numberOfRecords = query.uniqueResult().longValue();
        return (numberOfRecords > 0);
    }

    /**
     * Retrieve a genotype experiment by PK.
     *
     * @param genotypeExperimentID pk
     * @return genotype experiment
     */
    public FishExperiment getGenotypeExperiment(String genotypeExperimentID) {
        Session session = currentSession();
        return session.get(FishExperiment.class, genotypeExperimentID);
    }

    public FishExperiment getGenotypeExperiment(String genotypeZdbID, String experimentZdbID) {
        String hql = """
                from FishExperiment fe  
                where fe.fish.genotype.zdbID = :genoID  
                and fe.experiment.zdbID = :expID
            """;
        Query<FishExperiment> query = currentSession().createQuery(hql, FishExperiment.class);

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
        Session session = currentSession();
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

    }


    @SuppressWarnings({"unchecked"})
    public List<Genotype> getGenotypesForStandardAttribution(Publication publication) {
        String hql = """
            select distinct g from PublicationAttribution pa , Genotype g 
                 where pa.dataZdbID=g.zdbID and pa.publication.zdbID= :pubZdbID  
                 and pa.sourceType= :sourceType  
                 order by g.handle 
            """;

        Query<Genotype> query = currentSession().createQuery(hql, Genotype.class);
        query.setParameter("pubZdbID", publication.getZdbID());
        query.setParameter("sourceType", PublicationAttribution.SourceType.STANDARD);
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
        String hql = """
                    select distinct g from PublicationAttribution pa , GenericTerm g , MarkerGoTermEvidence ev 
                 where pa.dataZdbID=ev.zdbID 
                 and pa.publication.zdbID= :pubZdbID  
                 and ev.marker.zdbID = :markerZdbID 
                 and ev.evidenceCode.code not in (:excludedEvidenceCodes) 
                 and g.zdbID = ev.goTerm.zdbID 
                 and pa.sourceType= :sourceType  
                 order by g.termName
            """;
        Query<GenericTerm> query = currentSession().createQuery(hql, GenericTerm.class);
        query.setParameter("pubZdbID", publication.getZdbID());
        query.setParameterList("excludedEvidenceCodes", new String[]{GoEvidenceCodeEnum.IEA.name(), GoEvidenceCodeEnum.IC.name()});
        query.setParameter("markerZdbID", marker.getZdbID());
        query.setParameter("sourceType", PublicationAttribution.SourceType.STANDARD);
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

        currentSession().save(inferenceGroupMember);
        return inferenceGroupMember;
    }

    public void removeInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inference) {
        for (Iterator<InferenceGroupMember> iterator = markerGoTermEvidence.getInferredFrom().iterator(); iterator.hasNext(); ) {
            if (iterator.next().getInferredFrom().equals(inference)) {
                iterator.remove();


                CriteriaBuilder cb = currentSession().getCriteriaBuilder();
                CriteriaQuery<InferenceGroupMember> criteria = cb.createQuery(InferenceGroupMember.class);
                Root<InferenceGroupMember> root = criteria.from(InferenceGroupMember.class);
                criteria.select(root);
                criteria.where(cb.equal(root.get("inferredFrom"), inference));
                criteria.where(cb.equal(root.get("markerGoTermEvidenceZdbID"), markerGoTermEvidence.getZdbID()));
                InferenceGroupMember inferenceGroupMember = currentSession().createQuery(criteria).uniqueResult();
                currentSession().delete(inferenceGroupMember);

                return;
            }
        }

    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Genotype> getGenotypesForAttribution(String publicationZdbID) {
        String hql = """
                 select distinct g from Genotype g, RecordAttribution ra 
                 where ra.dataZdbID=g.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID 
                 order by g.handle
            """;

        return currentSession().createQuery(hql, Genotype.class)
            .setParameter("pubZdbID", publicationZdbID)
            .setParameter("standard", RecordAttribution.SourceType.STANDARD)
            .list();
    }

    public FeatureAlias getSpecificDataAlias(Feature feature, String alias) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<FeatureAlias> query = criteriaBuilder.createQuery(FeatureAlias.class);
        Root<FeatureAlias> root = query.from(FeatureAlias.class);
        query.where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("feature"), feature),
                criteriaBuilder.equal(root.get("alias"), alias)
            )
        );
        return session.createQuery(query).uniqueResult();
    }

    public FeatureDBLink getSpecificDBLink(Feature feature, String accessionNumber) {
        String hql = "select distinct ftrDbLink from FeatureDBLink ftrDbLink  where " +
            " ftrDbLink.feature = :feature ";
        Query<FeatureDBLink> query = currentSession().createQuery(hql, FeatureDBLink.class);
        query.setParameter("feature", feature);
        return (FeatureDBLink) query.uniqueResult();
    }

    public int getZFINInferences(String zdbID, String publicationZdbID) {
        return Integer.valueOf(currentSession().createNativeQuery("""
                 SELECT count(*) FROM marker_go_term_evidence  ev  
                 JOIN  inference_group_member inf ON ev.mrkrgoev_zdb_id=inf.infgrmem_mrkrgoev_zdb_id 
                 WHERE 
                 ev.mrkrgoev_source_zdb_id=:pubZdbID 
                 AND 
                 inf.infgrmem_inferred_from=:zdbID 
                """)
            .setParameter("zdbID", InferenceCategory.ZFIN_GENE.prefix() + zdbID)
            .setParameter("pubZdbID", publicationZdbID)
            .uniqueResult().toString()
        );
    }

    /**
     * Uses an alternate key that also includes the "inference" field, as well, which makes things kind
     * of tricky.
     *
     * @param markerGoTermEvidence Evidence to check against.
     * @return True if the exact version exists for this alternate key.
     */
    @SuppressWarnings("unchecked")
    @Override
    public int getNumberMarkerGoTermEvidences(MarkerGoTermEvidence markerGoTermEvidence) {
        String hql = """
                        select ev from MarkerGoTermEvidence ev
                        where ev.marker = :marker
                        and ev.goTerm = :goTerm
                        and ev.source = :publication
                        and ev.evidenceCode.code = :evidenceCode
            """;


        if (markerGoTermEvidence.getFlag() != null) {
            hql += " and ev.flag = :flag ";
        } else {
            hql += " and ev.flag is null ";
        }

        Query<MarkerGoTermEvidence> query = currentSession().createQuery(hql, MarkerGoTermEvidence.class)
            .setParameter("marker", markerGoTermEvidence.getMarker())
            .setParameter("goTerm", markerGoTermEvidence.getGoTerm())
            .setParameter("publication", markerGoTermEvidence.getSource())
            .setParameter("evidenceCode", markerGoTermEvidence.getEvidenceCode().getCode());

        if (markerGoTermEvidence.getFlag() != null) {
            query.setParameter("flag", markerGoTermEvidence.getFlag());
        }
        List<MarkerGoTermEvidence> evidences = query.list();

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
        final String alleleQueryString = """
            select distinct fmrel1.fmrel_ftr_zdb_id, psg_short_name, zdb_id, accession_no as accession_no,
                        e1a.term_ont_id as psg_e1a_id, e1b.term_ont_id as psg_e1b_id, e2a.term_ont_id as psg_e2a_id, e2b.term_ont_id as psg_e2b_id, quality.term_ont_id as psg_quality_id, fish_zdb_id
                        from feature_marker_relationship fmrel1
                        join genotype_feature on fmrel_ftr_zdb_id = genofeat_feature_zdb_id
                        join fish on genofeat_geno_zdb_id = fish_genotype_zdb_id
                        join fish_experiment on fish_zdb_id = genox_fish_zdb_id
                        join mutant_fast_search on mfs_genox_zdb_id = genox_zdb_id
                        join phenotype_source_generated on pg_genox_zdb_id = genox_zdb_id
                        join phenotype_observation_generated on psg_pg_id = pg_id
                        join figure on fig_zdb_id = pg_fig_zdb_id
                        join publication on fig_source_zdb_id = zdb_id
                        join term as e1a on psg_e1a_zdb_id = e1a.term_zdb_id
                        join term as quality on psg_quality_zdb_id = quality.term_zdb_id
                        left outer join term as e1b on e1b.term_zdb_id = psg_e1b_zdb_id
                        left outer join term as e2a on e2a.term_zdb_id = psg_e2a_zdb_id
                        left outer join term as e2b on e2b.term_zdb_id = psg_e2b_zdb_id
                          where get_obj_type(fmrel1.fmrel_ftr_zdb_id) = 'ALT'
                          and fmrel1.fmrel_type = 'is allele of' and not exists
                                       (select 'x' from feature_marker_relationship fmrel2
                                             where fmrel1.fmrel_zdb_id != fmrel2.fmrel_zdb_id and fmrel1.fmrel_ftr_zdb_id = fmrel2.fmrel_ftr_zdb_id
                                             and fmrel2.fmrel_type != 'created by')
            """;

        final String geneQueryString = """
            select distinct mfs_data_zdb_id, psg_short_name, zdb_id, accession_no as accession_no,
                                    e1a.term_ont_id as psg_e1a_id, e1b.term_ont_id as psg_e1b_id, e2a.term_ont_id as psg_e2a_id, 
                                    e2b.term_ont_id as psg_e2b_id, quality.term_ont_id as psg_quality_id, genox_fish_zdb_id
                                            from mutant_fast_search
                                            join phenotype_source_generated on pg_genox_zdb_id = mfs_genox_zdb_id
                                            join fish_experiment on mfs_genox_zdb_id = genox_zdb_id
                                            join phenotype_observation_generated on psg_pg_id = pg_id
                                            join figure on pg_fig_zdb_id = fig_zdb_id
                                            join publication on fig_source_zdb_id = zdb_id 
                                            join term as e1a on e1a.term_zdb_id = psg_e1a_zdb_id
                                            join term as quality on psg_quality_zdb_id = quality.term_zdb_id
                                            left outer join term as e1b on e1b.term_zdb_id = psg_e1b_zdb_id
                                            left outer join term as e2a on e2a.term_zdb_id = psg_e2a_zdb_id
                                            left outer join term as e2b on e2b.term_zdb_id = psg_e2b_zdb_id
                                            where get_obj_type(mfs_data_zdb_id) not in ('CRISPR','TALEN','MRPHLNO')
                        """;


        final String fishQueryString = """
            select distinct fish.fish_zdb_id, psg_short_name, zdb_id, accession_no as accession_no,
                        e1a.term_ont_id as psg_e1a_id, e1b.term_ont_id as psg_e1b_id, e2a.term_ont_id as psg_e2a_id, e2b.term_ont_id as psg_e2b_id, quality.term_ont_id as psg_quality_id, 
                        genox_zdb_id
                        from fish fish 
                        join fish_experiment on fish_zdb_id = genox_fish_zdb_id
                        join phenotype_source_generated on pg_genox_zdb_id = genox_zdb_id
                        join phenotype_observation_generated on psg_pg_id = pg_id
                        join figure on fig_zdb_id = pg_fig_zdb_id
                        join publication on fig_source_zdb_id = zdb_id
                        join term as e1a on psg_e1a_zdb_id = e1a.term_zdb_id
                        join term as quality on psg_quality_zdb_id = quality.term_zdb_id
                        left outer join genotype_feature on genofeat_geno_zdb_id = genox_fish_zdb_id
                        left outer join feature_marker_relationship on fmrel_ftr_zdb_id = genofeat_feature_zdb_id
                        left outer join term as e1b on e1b.term_zdb_id = psg_e1b_zdb_id
                        left outer join term as e2a on e2a.term_zdb_id = psg_e2a_zdb_id
                        left outer join term as e2b on e2b.term_zdb_id = psg_e2b_zdb_id
            """;


        final Query alleleQuery = currentSession().createNativeQuery(alleleQueryString);
        final Query geneQuery = currentSession().createNativeQuery(geneQueryString);
        final Query fishQuery = currentSession().createNativeQuery(fishQueryString);

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
                                DiseaseInfo.populateConditionClassId(expcond, condition);
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
    public List<STRMarkerSequence> getMorpholinosWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = """ 
            select m.zdbID, m.abbreviation, s.sequence from SequenceTargetingReagent m  
            inner join m.sequence s 
            inner join m.firstMarkerRelationships 
            where m.markerType.name = :markerTypeName 
            """;

        final Query<Tuple> query = currentSession().createQuery(queryString, Tuple.class);
        query.setParameter("markerTypeName", Marker.Type.MRPHLNO.name());

        List<Tuple> sequences = query.list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Tuple seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects.get(0).toString());
            strSequence.setName(seqObjects.get(1).toString());
            strSequence.setSequence(seqObjects.get(2).toString());
            strSequences.add(strSequence);
        }
        return strSequences;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<STRMarkerSequence> getCrisprsWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = """
            select m.zdbID, m.abbreviation, s.sequence from SequenceTargetingReagent m 
            inner join m.sequence s
            inner join m.firstMarkerRelationships 
            where m.markerType.name = :markerTypeName 
            """;
        final Query<Tuple> query = currentSession().createQuery(queryString, Tuple.class);
        query.setParameter("markerTypeName", Marker.Type.CRISPR.name());

        List<Tuple> sequences = query.list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Tuple seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects.get(0).toString());
            strSequence.setName(seqObjects.get(1).toString());
            strSequence.setSequence(seqObjects.get(2).toString());
            strSequences.add(strSequence);
        }
        return strSequences;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<STRMarkerSequence> getTalensWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = """
            select m.zdbID, m.abbreviation, s.sequence from SequenceTargetingReagent m
            inner join m.sequence s
            inner join m.firstMarkerRelationships
            where m.markerType.name = :markerTypeName
            """;

        final Query<Tuple> query = currentSession().createQuery(queryString, Tuple.class);
        query.setParameter("markerTypeName", Marker.Type.TALEN.name());

        List<Tuple> sequences = query.list();

        List<STRMarkerSequence> strSequences = new ArrayList<STRMarkerSequence>();
        for (Tuple seqObjects : sequences) {
            STRMarkerSequence strSequence = new STRMarkerSequence();
            strSequence.setZdbID(seqObjects.get(0).toString());
            strSequence.setName(seqObjects.get(1).toString());
            strSequence.setSequence(seqObjects.get(2).toString());
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
        String hql = """
            select distinct pheno from PhenotypeStatementWarehouse pheno where
            (pheno.e1a = :term OR pheno.e1b = :term OR
            pheno.e2a = :term OR pheno.e2b = :term )
            AND tag = :tag""";
        Query<PhenotypeStatementWarehouse> query = currentSession().createQuery(hql, PhenotypeStatementWarehouse.class);
        query.setParameter("term", term);
        query.setParameter("tag", PhenotypeStatement.Tag.ABNORMAL.toString()); //PhenotypeStatementWarehouse.tag is a String
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    public boolean hasPhenotype(GenericTerm term) {
        String hql = "select count(*) from PhenotypeStatementWarehouse where " +
            "(e1a = :term OR e1b = :term OR " +
            "e2a = :term OR e2b = :term ) " +
            "AND tag = :tag";
        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("term", term);
        query.setParameter("tag", PhenotypeStatement.Tag.ABNORMAL.toString()); //PhenotypeStatementWarehouse.tag is a String
        return query.uniqueResult().longValue() > 0;
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
        Query<MarkerGoTermEvidence> query = currentSession().createQuery(hql, MarkerGoTermEvidence.class);
        query.setParameter("term", term);
        return query.list();
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
        Session session = currentSession();

        String hql = "select distinct phenoStatement from PhenotypeStatementWarehouse phenoStatement " +
            "WHERE phenoStatement.phenotypeWarehouse.fishExperiment.fish.genotype = :genotype";

        Query<PhenotypeStatementWarehouse> query = session.createQuery(hql, PhenotypeStatementWarehouse.class);
        query.setParameter("genotype", genotype);

        return query.list();
    }

    public List<PhenotypeStatementWarehouse> getPhenotypeStatementsByFishExperiment(FishExperiment fishExperiment) {
        Session session = currentSession();

        String hql = "select distinct phenoStatement from PhenotypeStatementWarehouse phenoStatement " +
            "WHERE phenoStatement.phenotypeWarehouse.fishExperiment = :fishExperiment";

        Query<PhenotypeStatementWarehouse> query = session.createQuery(hql, PhenotypeStatementWarehouse.class);
        query.setParameter("fishExperiment", fishExperiment);

        return query.list();
    }

    /**
     * Returns list of phenotype statements that are annotated with a term marked secondary.
     *
     * @return list of phenotype statements.
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesOnSecondaryTerms() {
        Session session = currentSession();
        List<PhenotypeStatement> allPhenotypes = new ArrayList<>();

        String hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.quality is not null AND phenotype.quality.secondary = true";
        Query<PhenotypeStatement> query = session.createQuery(hql, PhenotypeStatement.class);

        allPhenotypes.addAll(query.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.entity.superterm is not null AND phenotype.entity.superterm.secondary = true";
        Query<PhenotypeStatement> queryEntitySuper = session.createQuery(hql, PhenotypeStatement.class);
        allPhenotypes.addAll(queryEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.entity.subterm is not null AND phenotype.entity.subterm.secondary = true";
        Query<PhenotypeStatement> queryEntitySub = session.createQuery(hql, PhenotypeStatement.class);
        allPhenotypes.addAll(queryEntitySub.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.relatedEntity.superterm is not null AND phenotype.relatedEntity.superterm.secondary = true";
        Query<PhenotypeStatement> queryRelatedEntitySuper = session.createQuery(hql, PhenotypeStatement.class);
        allPhenotypes.addAll(queryRelatedEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
            "     where phenotype.relatedEntity.subterm is not null AND phenotype.relatedEntity.subterm.secondary = true";
        Query<PhenotypeStatement> queryRelatedEntitySub = session.createQuery(hql, PhenotypeStatement.class);
        allPhenotypes.addAll(queryRelatedEntitySub.list());

        return allPhenotypes;
    }

    /**
     * Returns list of MarkerGoTermEvidence statements that are annotated with a term marked obsolete.
     *
     * @return list of MarkerGoTermEvidence statements.
     */
    @Override
    public List<MarkerGoTermEvidence> getGoEvidenceOnObsoletedTerms() {
        Session session = currentSession();
        String hql = "select goEvidence from MarkerGoTermEvidence goEvidence " +
            "     where goEvidence.goTerm.obsolete = true";
        Query<MarkerGoTermEvidence> query = session.createQuery(hql, MarkerGoTermEvidence.class);

        return query.list();
    }

    /**
     * Retrieve a histogram of phenotype terms usage.
     *
     * @return list of histograms
     */
    @Override
    public Map<TermHistogramBean, Long> getTermPhenotypeUsage() {
        Session session = currentSession();
        Map<TermHistogramBean, Long> histogram = new TreeMap<TermHistogramBean, Long>();

        // retrieve entity.superterm
        String hql = """
                select phenotype.entity.superterm.zdbID, phenotype.entity.superterm.termName,
                count(phenotype.entity.superterm) from PhenotypeStatement phenotype
                 group by phenotype.entity.superterm.zdbID,  phenotype.entity.superterm.termName
                 order by count(phenotype.entity.superterm) desc
            """;

        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        List<Tuple> result = query.list();
        if (result != null) {
            for (Tuple row : result) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row.get(0));
                termUsageHistogram.setTermName((String) row.get(1));
                addToHistogram(termUsageHistogram, (Long) row.get(2), histogram);
            }
        }

        // retrieve entity.subterm
        hql = """
                select phenotype.entity.subterm.zdbID, phenotype.entity.subterm.termName,
                count(phenotype.entity.superterm) from PhenotypeStatement phenotype
                 group by phenotype.entity.subterm.zdbID,  phenotype.entity.subterm.termName
                 order by count(phenotype.entity.subterm) desc
            """;
        Query<Tuple> queryEntitySubterm = session.createQuery(hql, Tuple.class);
        List<Tuple> resultEntitySubterm = queryEntitySubterm.list();
        if (resultEntitySubterm != null) {
            for (Tuple row : resultEntitySubterm) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row.get(0));
                termUsageHistogram.setTermName((String) row.get(1));
                addToHistogram(termUsageHistogram, (Long) row.get(2), histogram);
            }
        }

        // retrieve related entity.superterm
        hql = """
                select phenotype.relatedEntity.superterm.zdbID, phenotype.relatedEntity.superterm.termName,
                count(phenotype.relatedEntity.superterm) from PhenotypeStatement phenotype
                     group by phenotype.relatedEntity.superterm.zdbID,  phenotype.relatedEntity.superterm.termName
                order by count(phenotype.relatedEntity.superterm) desc
            """;
        Query<Tuple> queryRelatedEntitySuperbterm = session.createQuery(hql, Tuple.class);
        List<Tuple> resultRelatedEntitySuperterm = queryRelatedEntitySuperbterm.list();
        if (resultRelatedEntitySuperterm != null) {
            for (Tuple row : resultRelatedEntitySuperterm) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row.get(0));
                termUsageHistogram.setTermName((String) row.get(1));
                addToHistogram(termUsageHistogram, (Long) row.get(2), histogram);
            }
        }

        // retrieve related entity.subterm
        hql = """
                select phenotype.relatedEntity.subterm.zdbID, phenotype.relatedEntity.subterm.termName,
                count(phenotype.relatedEntity.superterm) from PhenotypeStatement phenotype
                     group by phenotype.relatedEntity.subterm.zdbID,  phenotype.relatedEntity.subterm.termName
                 order by count(phenotype.relatedEntity.subterm) desc
            """;
        Query<Tuple> queryRelatedEntitySubterm = session.createQuery(hql, Tuple.class);
        List<Tuple> resultRelatedEntitySubterm = queryRelatedEntitySubterm.list();
        if (resultRelatedEntitySubterm != null) {
            for (Tuple row : resultRelatedEntitySubterm) {
                TermHistogramBean termUsageHistogram = new TermHistogramBean();
                termUsageHistogram.setTermID((String) row.get(0));
                termUsageHistogram.setTermName((String) row.get(1));
                addToHistogram(termUsageHistogram, (Long) row.get(2), histogram);
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
    public List<Feature> getAllelesForMarker(String zdbID, FeatureMarkerRelationshipTypeEnum type) {
        String hql = """
                select distinct 
                feat from FeatureMarkerRelationship fmrel, Feature feat 
                where fmrel.marker.zdbID= :zdbID 
                and fmrel.type=:type 
                and fmrel.feature = feat 
                order by feat.abbreviationOrder
            """;

        Query<Feature> query = currentSession().createQuery(hql, Feature.class);
        query.setParameter("zdbID", zdbID);
        query.setParameter("type", type);
        return query.list();

    }

    @Override
    public List<Marker> getKnockdownReagents(Marker gene) {
        String hql = """
                 select m from Marker m join m.firstMarkerRelationships mr 
                 where mr.secondMarker.zdbID = :markerZdbId  
                 and mr.markerRelationshipType.name = :type 
                 order by m.abbreviationOrder 
            """;
        return currentSession().createQuery(hql, Marker.class)
            .setParameter("markerZdbId", gene.getZdbID())
            .setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE.toString()) //markerRelationshipType.name is a String
            .list();
    }

    @Override
    public List<Genotype> getTransgenicLinesForConstruct(Marker construct) {

        Session session = currentSession();

        String hql = """
                select distinct genofeat.genotype from GenotypeFeature genofeat, FeatureMarkerRelationship featMarker 
                where featMarker.marker = :marker 
                and genofeat.feature = featMarker.feature
            """;

        Query<Genotype> query = session.createQuery(hql, Genotype.class);
        query.setParameter("marker", construct);
        return query.list();
    }

    /**
     * Retrieve phenotype statements by genotype experiment ids
     *
     * @param fishExperimentIDs genox ids
     * @return list of phenotype statements
     */
    public List<PhenotypeStatement> getPhenotypeStatementsByGenotypeExperiments(List<String> fishExperimentIDs) {
        String hql = " from PhenotypeStatement where phenotypeExperiment.fishExperiment.zdbID in (:fishoxIds)";

        Query<PhenotypeStatement> query = currentSession().createQuery(hql, PhenotypeStatement.class);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        return query.list();
    }

    public List<PhenotypeStatement> getPhenotypeStatementsByFish(Fish fish) {
        String hql = " from PhenotypeStatement where phenotypeExperiment.fishExperiment.fish.zdbID = :fishZdbId";

        Query<PhenotypeStatement> query = currentSession().createQuery(hql, PhenotypeStatement.class);
        query.setParameter("fishZdbId", fish.getZdbID());
        return query.list();
    }

    public List<PhenotypeStatementWarehouse> getPhenotypeStatementWarehousesByFish(Fish fish) {
        String hql = " from PhenotypeStatementWarehouse where phenotypeWarehouse.fishExperiment.fish.zdbID = :fishZdbId";

        Query<PhenotypeStatementWarehouse> query = currentSession().createQuery(hql, PhenotypeStatementWarehouse.class);
        query.setParameter("fishZdbId", fish.getZdbID());
        return query.list();
    }

    public Set<String> getGenoxAttributions(List<String> fishExperimentIDs) {
        String hql = "select distinct publication.zdbID from ExpressionExperiment where fishExperiment.zdbID in (:fishoxIds)";

        Query<String> query = currentSession().createQuery(hql, String.class);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        List<String> pubIds = query.list();
        Set<String> distinctPubs = new HashSet<String>(pubIds.size());
        distinctPubs.addAll(pubIds);

        // phenotype experiments
        hql = "select distinct figure.publication.zdbID from PhenotypeExperiment where fishExperiment.zdbID in (:fishoxIds)";
        query = currentSession().createQuery(hql, String.class);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        pubIds = query.list();
        distinctPubs.addAll(pubIds);

        // experiments
        hql = "select distinct experiment.publication.zdbID from FishExperiment where zdbID in (:fishoxIds)";
        query = currentSession().createQuery(hql, String.class);
        query.setParameterList("fishoxIds", fishExperimentIDs);
        pubIds = query.list();
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
        Session session = currentSession();

        // direct attribution
        String hql = "select p.publication  from PublicationAttribution p  where p.dataZdbID = :fishZdbID ";

        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("fishZdbID", fish.getZdbID());
        return query.list();
    }

    /**
     * Retrieve sequence targeting reagent by its id
     *
     * @param sequenceTargetingReagentID equence targeting reagent by its id
     * @return SequenceTargetingReagent object
     */
    public SequenceTargetingReagent getSequenceTargetingReagentByID(String sequenceTargetingReagentID) {
        return (SequenceTargetingReagent) currentSession().get(SequenceTargetingReagent.class, sequenceTargetingReagentID);
    }

    /**
     * Retrieve all wildtype genotypes.
     *
     * @return
     */
    @Override
    public List<Genotype> getAllWildtypeGenotypes() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Genotype> query = criteriaBuilder.createQuery(Genotype.class);
        Root<Genotype> genotypeRoot = query.from(Genotype.class);
        query.where(criteriaBuilder.equal(genotypeRoot.get("wildtype"), true));
        query.orderBy(criteriaBuilder.asc(genotypeRoot.get("nameOrder")));
        return session.createQuery(query).list();
    }

    /**
     * Retrieve a list of expression result records that show expression data for a given fish
     *
     * @return
     */
    @Override
    public List<ExpressionFigureStage> getExpressionSummary(Set<FishExperiment> fishOx, String geneID) {
        if (CollectionUtils.isEmpty(fishOx)) {
            return null;
        }

        String hql = """ 
            select distinct expressionResult from ExpressionFigureStage expressionResult where
            expressionResult.expressionExperiment.fishExperiment in (:fishOx) AND
            expressionResult.expressionExperiment.gene.zdbID
            """ + (geneID == null ? " is not null" : " = :geneID");

        Query<ExpressionFigureStage> query = currentSession().createQuery(hql, ExpressionFigureStage.class);

        query.setParameterList("fishOx", fishOx);
        if (geneID != null) {
            query.setParameter("geneID", geneID);
        }
        return query.list();
    }

    public List<ExperimentCondition> getExperimentConditions(Experiment experiment) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ExperimentCondition> query = criteriaBuilder.createQuery(ExperimentCondition.class);
        Root<ExperimentCondition> root = query.from(ExperimentCondition.class);
        query.select(root)
            .where(criteriaBuilder.equal(root.get("experiment"), experiment));
        return session.createQuery(query).getResultList();
    }

    public List<ExpressionResult2> getConstructExpressionSummary(List<String> genoxIds) {
        if (CollectionUtils.isEmpty(genoxIds)) {
            return null;
        }

        String hql = """
             select distinct expressionResult from ExpressionResult2 expressionResult where
              expressionResult.expressionFigureStage.expressionExperiment.fishExperiment.zdbID in (:genoxIds) AND
               expressionResult.expressionFigureStage.expressionExperiment.gene is not null
            """;

        Query<ExpressionResult2> query = currentSession().createQuery(hql, ExpressionResult2.class);
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

        String sql = """
            SELECT count(*) FROM figure, expression_experiment2, expression_result2, expression_figure_stage, fish_experiment,fish
            WHERE fig_zdb_id = efs_fig_zdb_id
            AND xpatex_zdb_id = efs_xpatex_zdb_id
            AND xpatres_efs_id = efs_pk_id
            AND genox_fish_zdb_id = fish_zdb_id
            AND fish_genotype_zdb_id = :genotypeID
            AND xpatex_genox_zdb_id = genox_zdb_id
            AND genox_zdb_id IN (:fishOxList)
            AND exists (SELECT 'x' FROM image WHERE img_fig_Zdb_id = fig_zdb_id)
            """;

        Query query = currentSession().createNativeQuery(sql);
        query.setParameter("genotypeID", genotypeID);
        query.setParameterList("fishOxList", fishOxList);

        return (((Number) query.uniqueResult()).longValue() > 0);
    }


    public List<PhenotypeStatementWarehouse> getPhenotypeStatementForMutantSummary(GenericTerm term, Genotype genotype, boolean includeSubstructures) {
        String hql = """
                select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeTermFastSearch fastSearch 
                where fastSearch.phenotypeStatement = pheno and 
                pheno.phenotypeWarehouse.fishExperiment.fish.genotype = :genotype and 
                fastSearch.directAnnotation = :directAnnotation 
                AND not exists (select 1 from ExperimentCondition cond where
                 cond.experiment = pheno.phenotypeWarehouse.fishExperiment.experiment 
                AND cond.sequenceTargetingReagent is not null ) 
            """;

        if (term != null) {
            hql += " and fastSearch.term = :term ";
        }

        Query<PhenotypeStatementWarehouse> query = currentSession().createQuery(hql, PhenotypeStatementWarehouse.class);
        if (term != null) {
            query.setParameter("term", term);
        }
        query.setParameter("genotype", genotype);
        query.setParameter("directAnnotation", !includeSubstructures);
        return query.list();
    }

    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeStatementObservedForMutantSummary(GenericTerm term, Fish fish, boolean includeSubstructures) {
        String hql = """
                select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeTermFastSearch fastSearch 
                where fastSearch.phenotypeObserved = pheno and 
                pheno.phenotypeWarehouse.fishExperiment.fish = :fish and 
                fastSearch.directAnnotation = :directAnnotation 
            """;

        if (term != null) {
            hql += " and fastSearch.term = :term ";
        }

        Query<PhenotypeStatementWarehouse> query = currentSession().createQuery(hql, PhenotypeStatementWarehouse.class);
        if (term != null) {
            query.setParameter("term", term);
        }
        query.setParameter("fish", fish);
        query.setParameter("directAnnotation", !includeSubstructures);
        return query.list();
    }

    @Override
    public List<DiseaseAnnotationModel> getDiseaseAnnotationModels(int numfOfRecords) {
        String hql = """
                from DiseaseAnnotationModel model 
                join fetch model.fishExperiment 
                join fetch model.fishExperiment.fish 
                join fetch model.fishExperiment.experiment 
                join fetch model.diseaseAnnotation 
                join fetch model.diseaseAnnotation.disease 
                join fetch model.diseaseAnnotation.publication 
            """;


        Query<DiseaseAnnotationModel> query = currentSession().createQuery(hql, DiseaseAnnotationModel.class);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        return query.list();

    }

    @Override
    public List<DiseaseAnnotationModel> getDiseaseAnnotationModelsNoStd(int numfOfRecords) {
        String hql = " from DiseaseAnnotationModel model where model.fishExperiment.standardOrGenericControl = false";

        Query<DiseaseAnnotationModel> query = currentSession().createQuery(hql, DiseaseAnnotationModel.class);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        return query.list();

    }

    @Override
    public List<GeneGenotypeExperiment> getGeneDiseaseAnnotationModels(int numfOfRecords) {
        String hql = """
                select distinct geneGenotype from GeneGenotypeExperiment geneGenotype, DiseaseAnnotationModel diseaseAnnotationModel
                join fetch geneGenotype.gene
                join fetch geneGenotype.fishExperiment
                where geneGenotype.fishExperiment = diseaseAnnotationModel.fishExperiment
                and geneGenotype.gene.markerType.name = :genedom
            """;

        Query<GeneGenotypeExperiment> query = currentSession().createQuery(hql, GeneGenotypeExperiment.class);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);

        query.setParameter("genedom", Marker.Type.GENE.toString()); //...markerType.name is a String
        return query.list();
    }


    @Override
    public List<GeneGenotypeExperiment> getGeneFishWithPhenotypes(int numfOfRecords) {
        String hql = """
                select distinct geneGenotype from GeneGenotypeExperiment geneGenotype, PhenotypeStatement phenox 
                join fetch geneGenotype.gene 
                join fetch geneGenotype.fishExperiment 
                where geneGenotype.fishExperiment = phenox.fishExperiment 
                and geneGenotype.gene.markerType.name = :genedom 
            """;

        Query<GeneGenotypeExperiment> query = currentSession().createQuery(hql, GeneGenotypeExperiment.class);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);

        query.setParameter("genedom", Marker.Type.GENE.toString()); //...markerType.name is a String
        return query.list();
    }

    @Override
    public List<PhenotypeObservationStatement> getPhenotypeStatements(String geneID, String termIDs) {
        String[] split = termIDs.split(",");
        String hql = "from PhenotypeObservationStatement as phenoObservation WHERE phenoObservation.id in (:ids) ";

        Query<PhenotypeObservationStatement> query = currentSession().createQuery(hql, PhenotypeObservationStatement.class);
        query.setParameterList("ids", Arrays.stream(split).map(Long::valueOf).collect(Collectors.toList()));

        return query.list();
    }

    @Override
    public Experiment getExperiment(String experimentID) {
        return HibernateUtil.currentSession().get(Experiment.class, experimentID);
    }

    @Override
    public List<FishExperiment> getAllFishExperiment() {
        String hql = """
                select fishExp from FishExperiment as fishExp
                where fishExp.diseaseAnnotationModels is not empty
            """;

        Query<FishExperiment> query = currentSession().createQuery(hql, FishExperiment.class);
        return query.list();
    }


    @Override
    public List<OmimPhenotype> getDiseaseModelsFromGenes(int numfOfRecords) {
        String hql = """
                from OmimPhenotype model where model.externalReferences is not empty 
                order by model.ortholog.zebrafishGene.abbreviationOrder
            """;

        Query<OmimPhenotype> query = currentSession().createQuery(hql, OmimPhenotype.class);
        if (numfOfRecords > 0)
            query.setMaxResults(numfOfRecords);
        return query.list();
    }

    public List<Genotype> getGenotypes(List<String> genotypeExperimentIDs) {
        String hql = """
                select distinct fish.genotype from 
                FishExperiment genoExp where genoExp.zdbID in (:genoxIDs) 
                and genoExp.standardOrGenericControl=false
            """;

        Query<Genotype> query = currentSession().createQuery(hql, Genotype.class);
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

        Session session = currentSession();
        List<PhenotypeStatement> allPhenotypes = new ArrayList<PhenotypeStatement>();

        if (!individualOnly || ontology.equals(Ontology.QUALITY)) {
            String hql = """
                    select phenotype from PhenotypeStatement phenotype 
                    where phenotype.quality is not null AND phenotype.quality.obsolete = true
                """;
            Query<PhenotypeStatement> query = session.createQuery(hql, PhenotypeStatement.class);
            allPhenotypes.addAll(query.list());
            if (individualOnly) {
                return allPhenotypes;
            }
        }

        List<String> ontologyList = null;
        if (individualOnly) {
            ontologyList = ontology.getIndividualOntologies().stream().map(Ontology::getDbOntologyName).collect(Collectors.toList());
        }

        String hql = """
                select phenotype from PhenotypeStatement phenotype      
                where phenotype.entity.superterm is not null AND phenotype.entity.superterm.obsolete = true 
            """;
        if (individualOnly) {
            hql += " AND phenotype.entity.superterm.ontology in (:ontologyList)";
        }
        Query<PhenotypeStatement> queryEntitySuper = session.createQuery(hql, PhenotypeStatement.class);
        if (individualOnly) {
            queryEntitySuper.setParameterList("ontologyList", ontologyList);
        }

        allPhenotypes.addAll(queryEntitySuper.list());

        hql = """
            select phenotype from PhenotypeStatement phenotype
            where phenotype.entity.subterm is not null 
            AND phenotype.entity.subterm.obsolete = true
            """;
        if (individualOnly) {
            hql += " AND phenotype.entity.subterm.ontology in (:ontologyList)";
        }
        Query<PhenotypeStatement> queryEntitySub = session.createQuery(hql, PhenotypeStatement.class);
        if (individualOnly) {
            queryEntitySub.setParameterList("ontologyList", ontologyList);
        }
        allPhenotypes.addAll(queryEntitySub.list());

        hql = """
                select phenotype from PhenotypeStatement phenotype 
                where phenotype.relatedEntity.superterm is not null 
                AND phenotype.relatedEntity.superterm.obsolete = true
            """;
        if (individualOnly) {
            hql += " AND phenotype.relatedEntity.superterm.ontology in (:ontologyList)";
        }
        Query<PhenotypeStatement> queryRelatedEntitySuper = session.createQuery(hql, PhenotypeStatement.class);
        if (individualOnly) {
            queryRelatedEntitySuper.setParameterList("ontologyList", ontologyList);
        }
        allPhenotypes.addAll(queryRelatedEntitySuper.list());

        hql = """
                select phenotype from PhenotypeStatement phenotype  
                where phenotype.relatedEntity.subterm is not null 
                AND phenotype.relatedEntity.subterm.obsolete = true
            """;
        if (individualOnly) {
            hql += "      AND phenotype.relatedEntity.subterm.ontology in (:ontologyList)";
        }
        Query<PhenotypeStatement> queryRelatedEntitySub = session.createQuery(hql, PhenotypeStatement.class);
        if (individualOnly) {
            queryRelatedEntitySub.setParameterList("ontologyList", ontologyList);
        }
        allPhenotypes.addAll(queryRelatedEntitySub.list());

        return allPhenotypes;
    }


    @Override
    public List<PhenotypeStatement> getPhenotypeStatementsByMarker(Marker gene) {

        Session session = currentSession();

        String hql = """
                select distinct phenoStatement from PhenotypeStatement phenoStatement, GeneGenotypeExperiment geneGenox 
                WHERE geneGenox.gene = :gene and 
                phenoStatement.phenotypeExperiment.fishExperiment = geneGenox.fishExperiment 
                and phenoStatement.tag = :tag
            """;
        Query<PhenotypeStatement> query = session.createQuery(hql, PhenotypeStatement.class);
        query.setParameter("gene", gene);
        query.setParameter("tag", "abnormal");

        return query.list();

    }


    public List<GenotypeFigure> getGenotypeFiguresBySTR(SequenceTargetingReagent str) {
        Session session = currentSession();

        String hql = """
                select distinct genoFig from GenotypeFigure genoFig 
                where genoFig.sequenceTargetingReagent.zdbID = :zdbID
            """;
        Query<GenotypeFigure> query = session.createQuery(hql, GenotypeFigure.class);
        query.setParameter("zdbID", str.getZdbID());

        List<GenotypeFigure> genotypeFigures = query.list();

        List<GenotypeFigure> notNormalGenotypeFigures = genotypeFigures.stream()
            .filter(genotypeFigure -> genotypeFigure.getPhenotypeStatement().isNotNormal())
            .collect(Collectors.toList());
        return notNormalGenotypeFigures;
    }

    @Override
    public List<SequenceTargetingReagent> getStrList(String publicationID) {
        Session session = currentSession();

        String hql = """
                select str from SequenceTargetingReagent str , PublicationAttribution attrib 
                     where attrib.publication.zdbID = :publicationID AND 
                attrib.dataZdbID = str.zdbID 
                order by str.abbreviationOrder
            """;
        Query<SequenceTargetingReagent> query = session.createQuery(hql, SequenceTargetingReagent.class);
        query.setParameter("publicationID", publicationID);

        return query.list();
    }

    @Override
    public boolean createFishIfNotExists(Fish fish, Publication publication) {
        Fish existingFish = getFishByGenoStr(fish);
        boolean newFishCreated = false;
        if (existingFish == null) {
            currentSession().save(fish);
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
        Session session = currentSession();

        String hql = "select fish from Fish fish where fish.genotype = :genotype ";
        boolean strsAvailable = CollectionUtils.isNotEmpty(fish.getStrList());
        if (strsAvailable) {
            int index = 0;
            for (SequenceTargetingReagent ignored : fish.getStrList())
                hql += " AND :str_" + index++ + " member of fish.strList ";
        }
        hql += """
              AND (select count(str.id) from Fish zfish
              inner join zfish.strList str 
              where zfish.zdbID = fish.zdbID) = :numberOfStrs
            """;

        Query<Fish> query = session.createQuery(hql, Fish.class);
        query.setParameter("genotype", fish.getGenotype());
        if (strsAvailable) {
            Long numberOfStrs = Long.valueOf(fish.getStrList().size());
            query.setParameter("numberOfStrs", numberOfStrs);
        } else {
            query.setParameter("numberOfStrs", 0L);
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
            currentSession().save(diseaseAnnotation);
            getInfrastructureRepository().insertRecordAttribution(diseaseAnnotation.getZdbID(), diseaseAnnotation.getPublication().getZdbID());
        }


        //DiseaseAnnotation existingDiseaseAnnotation = getMutantRepository().getDiseaseModel(diseaseAnnotation);
    }

    @Override
    public List<Publication> getPublicationWithFish(String fishID) {
        Session session = currentSession();

        String hql = "select attrib.publication from PublicationAttribution attrib where attrib.dataZdbID = :fishID ";
        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("fishID", fishID);
        return (List<Publication>) query.list();
    }

    @Override
    public FishExperiment getFishModel(String fishID, String expID) {
        Session session = currentSession();

        String hql = "from FishExperiment where fish.zdbID = :fishID and experiment.zdbID = :expID ";
        Query<FishExperiment> query = session.createQuery(hql, FishExperiment.class);
        query.setParameter("fishID", fishID);
        query.setParameter("expID", expID);
        return (FishExperiment) query.uniqueResult();
    }

    @Override
    public DiseaseAnnotation getDiseaseModel(DiseaseAnnotation diseaseAnnotation) {
        Session session = currentSession();

        String hql = """
                from DiseaseAnnotation 
                where disease = :disease 
                and publication = :publication 
                and evidenceCode = :evidenceCode
            """;

        Query<DiseaseAnnotation> query = session.createQuery(hql, DiseaseAnnotation.class);
        query.setParameter("disease", diseaseAnnotation.getDisease());
        query.setParameter("publication", diseaseAnnotation.getPublication());
        query.setParameter("evidenceCode", diseaseAnnotation.getEvidenceCode());

        return (DiseaseAnnotation) query.uniqueResult();
    }

    @Override
    public DiseaseAnnotation getDiseaseModelByID(String zdbID) {
        return currentSession().get(DiseaseAnnotation.class, zdbID);
    }

    @Override
    public DiseaseAnnotationModel getDiseaseAnnotationModelByID(Long id) {
        return currentSession().get(DiseaseAnnotationModel.class, id);
    }

    public List<DiseaseAnnotationModel> getDiseaseAnnotationModelByZdb(String zdb) {
        String hql = " from DiseaseAnnotationModel where diseaseAnnotation.zdbID = :zdb ";

        Query<DiseaseAnnotationModel> query = currentSession().createQuery(hql, DiseaseAnnotationModel.class);

        query.setParameter("zdb", zdb);


        return query.list();
    }

    @Override
    public void deleteDiseaseModel(DiseaseAnnotation diseaseAnnotation) {
        getInfrastructureRepository().deleteRecordAttributionsForData(diseaseAnnotation.getZdbID());
        getInfrastructureRepository().deleteRecordAttributionsForData(diseaseAnnotation.getDisease().getZdbID());
        currentSession().delete(diseaseAnnotation);
    }

    @Override
    public void deleteDiseaseAnnotationModel(DiseaseAnnotationModel diseaseAnnotationModel) {
        currentSession().delete(diseaseAnnotationModel);
    }

    @Override
    public List<DiseaseAnnotation> getDiseaseModel(String fishID, String pubID) {
        String hql = """
                select dm 
                from DiseaseAnnotation dm, DiseaseAnnotationModel dma 
                where dm.publication.zdbID = :pubID 
                AND dma.diseaseAnnotation=dm 
                AND dma.fishExperiment.fish.zdbID = :fishID
            """;
        Query<DiseaseAnnotation> query = currentSession().createQuery(hql, DiseaseAnnotation.class);
        query.setParameter("fishID", fishID);
        query.setParameter("pubID", pubID);
        return query.list();
    }

    @Override
    public List<Fish> getFishList(String publicationID) {
        Session session = currentSession();

        String hql = """
                select distinct fish from Fish fish, PublicationAttribution attrib 
                 where attrib.publication.zdbID = :publicationID AND 
                attrib.dataZdbID = fish.zdbID AND 
                attrib.sourceType = :sourceType 
                order by fish.nameOrder
            """;
        Query<Fish> query = session.createQuery(hql, Fish.class);
        query.setParameter("publicationID", publicationID);
        query.setParameter("sourceType", RecordAttribution.SourceType.STANDARD);
        return query.list();
    }

    @Override
    public Fish getFish(String fishID) {
        return currentSession().get(Fish.class, fishID);
    }

    @Override
    public List<Fish> getFishListBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        Session session = currentSession();
        String hql = """
                select fish from Fish fish 
                 where :str member of fish.strList 
                 order by fish.name
            """;
        Query<Fish> query = session.createQuery(hql, Fish.class);
        query.setParameter("str", sequenceTargetingReagent);
        return query.list();
    }

    @Override
    public List<Fish> getAllWildtypeFish() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Fish> query = criteriaBuilder.createQuery(Fish.class);
        Root<Fish> root = query.from(Fish.class);
        Join<Fish, Genotype> genotypeJoin = root.join("genotype");
        query.select(root)
            .where(criteriaBuilder.and(
                criteriaBuilder.equal(genotypeJoin.get("wildtype"), true),
                criteriaBuilder.isEmpty(root.get("strList"))
            ))
            .orderBy(criteriaBuilder.asc(root.get("name")));
        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Fish> getAllFish() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Fish> query = criteriaBuilder.createQuery(Fish.class);
        Root<Fish> root = query.from(Fish.class);
        query.orderBy(criteriaBuilder.asc(root.get("name")));
        return session.createQuery(query).getResultList();
    }

    @Override
    public List<SequenceTargetingReagent> getAllSTRs() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<SequenceTargetingReagent> query = criteriaBuilder.createQuery(SequenceTargetingReagent.class);
        Root<SequenceTargetingReagent> root = query.from(SequenceTargetingReagent.class);
        query.orderBy(criteriaBuilder.asc(root.get("name")));
        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Genotype> getGenotypesByFeatureAndBackground(Feature feature, Genotype background, Publication publication) {
        Session session = currentSession();
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

        Query<GenotypeFeature> query = session.createQuery(hql, GenotypeFeature.class);
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

        Session session = currentSession();
        return session.get(FishExperiment.class, zdbID);

    }

    @Override
    public List<FishExperiment> getFishExperiment(Genotype genotype) {
        Session session = currentSession();
        String hql = """
                select distinct fishExperiment, fishExperiment.fish.order FROM  FishExperiment fishExperiment 
                WHERE fishExperiment.fish.genotype = :genotype 
                order by fishExperiment.fish.order
            """;
        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("genotype", genotype);

        List<Tuple> list = query.list();
        List<FishExperiment> result = new ArrayList<>(list.size());
        for (Tuple tuple : list) {
            result.add((FishExperiment) tuple.get(0));
        }
        return result;
    }

    @Override
    public List<FishExperiment> getFishExperimentsByFish(Fish fish) {
        String hql = "from FishExperiment exp where exp.fish = :fish";
        Query<FishExperiment> query = currentSession().createQuery(hql, FishExperiment.class);
        query.setParameter("fish", fish);
        return query.list();
    }

    @Override
    public List<Zygosity> getListOfZygosity() {
        Session session = currentSession();
        String hql = "FROM  Zygosity ";
        Query<Zygosity> query = session.createQuery(hql, Zygosity.class);

        return query.list();
    }

    @Override
    public Zygosity getZygosity(String ID) {
        return currentSession().get(Zygosity.class, ID);
    }

    @Override
    public void saveGenotype(Genotype genotype, String publicationID) {
        Session session = currentSession();
        session.save(genotype);
        if (genotype.getGenotypeFeatures() != null) {
            for (GenotypeFeature genoFeature : genotype.getGenotypeFeatures())
                session.save(genoFeature);
        }
        getInfrastructureRepository().insertPublicAttribution(genotype.getZdbID(), publicationID, RecordAttribution.SourceType.STANDARD);
        getInfrastructureRepository().insertUpdatesTable(publicationID, "geno_zdb_id", null, genotype.getZdbID(), "create new record");
    }

    public void updateGenotypeNicknameWithHandleForPublication(Publication publication) {
        currentSession().createNativeQuery(
                "UPDATE genotype " +
                    "SET geno_nickname = geno_handle " +
                    "WHERE EXISTS ( " +
                    "  SELECT 'x' " +
                    "  FROM record_attribution " +
                    "  WHERE recattrib_data_zdb_id = geno_zdb_id " +
                    "  AND recattrib_source_zdb_id = :pubID " +
                    ");")
            .setParameter("pubID", publication.getZdbID())
            .executeUpdate();
    }

    public void updateFishAffectedGeneCount(Fish fish) {
        currentSession().createNativeQuery(
                "UPDATE fish " +
                    "SET fish_name = fish_name " +
                    "WHERE fish_Zdb_id=:fishID ;")

            .setParameter("fishID", fish.getZdbID())
            .executeUpdate();
    }


    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeStatementForMarker(Marker marker) {
        String hql = """
                select distinct pheno from PhenotypeStatementWarehouse pheno, GeneGenotypeExperiment gge 
                where pheno.phenotypeWarehouse.fishExperiment = gge.fishExperiment 
                AND gge.gene = :gene 
                AND pheno.tag != :tag
            """;
        Query<PhenotypeStatementWarehouse> query = currentSession().createQuery(hql, PhenotypeStatementWarehouse.class);
        query.setParameter("gene", marker);
        query.setParameter("tag", "normal");
        return query.list();
    }

    @Override
    public FishExperiment getFishExperimentByFishAndExperimentID(String fishID, String experimentID) {
        Session session = currentSession();
        Query<FishExperiment> query = session.createQuery("""
                FROM FishExperiment fe 
                   WHERE fe.fish.zdbID = :fishID 
                   AND fe.experiment.zdbID = :experimentID
                """,
            FishExperiment.class
        );
        query.setParameter("fishID", fishID);
        query.setParameter("experimentID", experimentID);
        return query.uniqueResult();
    }

    @Override
    public List<Fish> getFishByGenotype(Genotype genotype) {
        String hql = "select fish from Fish as fish where fish.genotype = :genotype";
        Query<Fish> query = currentSession().createQuery(hql, Fish.class);
        query.setParameter("genotype", genotype);
        return query.list();
    }

    @Override
    public String getFishByWTGenotype(Genotype genotype) {
        String hql = "select fish.zdbID from Fish as fish where fish.genotype = :genotype and fish.wildtype is true";
        Query<String> query = currentSession().createQuery(hql, String.class);
        query.setParameter("genotype", genotype);
        return query.uniqueResult().toString();
    }

    @Override
    public List<Fish> getFishByGenotypeNoExperiment(Genotype genotype) {
        String hql = "select fish from Fish as fish where fish.genotype = :genotype and fish.fishExperiments is empty";
        Query<Fish> query = currentSession().createQuery(hql, Fish.class);
        query.setParameter("genotype", genotype);
        return query.list();
    }

    @Override
    public long getFishCountByGenotype(String genotypeID, String publicationID) {
        String hql = """
                select distinct fish from Fish as fish, PublicationAttribution pubAtt 
                where fish.genotype.zdbID = :genotypeID AND 
                pubAtt.publication.zdbID = :publicationID AND 
                pubAtt.dataZdbID = fish.zdbID
            """;

        Query<Fish> query = currentSession().createQuery(hql, Fish.class);
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
        String hql = """
                from PhenotypeStatement 
                where phenotypeExperiment.fishExperiment.fish = :fish AND 
                phenotypeExperiment.figure.publication.zdbID = :publicationID
            """;
        Query<PhenotypeStatement> query = currentSession().createQuery(hql, PhenotypeStatement.class);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);

        List<PhenotypeStatement> phenotypeStatements = query.list();
        if (phenotypeStatements == null) {
            return 0;
        }
        return (long) phenotypeStatements.size();
    }

    @Override
    public long getInferredFromCountByGenotype(String genotypeID, String publicationID) {
        String hql = """
                select count(inferred.markerGoTermEvidenceZdbID) from InferenceGroupMember as inferred, MarkerGoTermEvidence as mgt 
                where inferred.inferredFrom = :genotypeID AND 
                mgt.source.zdbID = :publicationID AND 
                inferred.markerGoTermEvidenceZdbID = mgt.zdbID
            """;
        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("genotypeID", "ZFIN:" + genotypeID);
        query.setParameter("publicationID", publicationID);
        return (long) query.uniqueResult();
    }

    @Override
    public long getFishExperimentCountByGenotype(Fish fish, String publicationID) {
        String hql = "from PhenotypeExperiment where fishExperiment.fish = :fish AND figure.publication.zdbID = :publicationID";
        Query<PhenotypeExperiment> query = currentSession().createQuery(hql, PhenotypeExperiment.class);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);
        List<PhenotypeExperiment> fishList = query.list();
        if (fishList == null) {
            return 0;
        }
        return (long) fishList.size();
    }

    @Override
    public List<PhenotypeStatementWarehouse> getPhenotypeObserved(GenericTerm term, Fish fish, boolean includeSubstructures) {
        String hql = """
                select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeTermFastSearch fastSearch 
                where fastSearch.phenotypeObserved = pheno and 
                pheno.phenotypeWarehouse.fishExperiment.fish = :fish and 
                fastSearch.directAnnotation = :directAnnotation 
            """;

        if (term != null) {
            hql += " and fastSearch.term = :term ";
        }

        Query<PhenotypeStatementWarehouse> query = currentSession().createQuery(hql, PhenotypeStatementWarehouse.class);
        if (term != null) {
            query.setParameter("term", term);
        }
        query.setParameter("fish", fish);
        query.setParameter("directAnnotation", !includeSubstructures);
        return query.list();
    }
}
