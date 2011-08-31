package org.zfin.mutant.repository;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.Transformers;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.database.DbSystemUtil;
import org.zfin.expression.Experiment;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureAlias;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.presentation.TermHistogramBean;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.MorpholinoSequence;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;


/**

 */

public class HibernateMutantRepository implements MutantRepository {
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    private Logger logger = Logger.getLogger(HibernateMutantRepository.class);


    public PaginationResult<Genotype> getGenotypesByAnatomyTerm(GenericTerm item, boolean wildtype, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        String hql =
                "select distinct genox.genotype , genox.genotype.nameOrder from GenotypeExperiment genox, " +
                        "PhenotypeExperiment phenox, PhenotypeStatement phenoeq " +
                        "WHERE phenox.genotypeExperiment = genox " +
                        "AND phenoeq.phenotypeExperiment = phenox " +
                        "AND (phenoeq.entity.superterm = :aoTerm " +
                        "     or phenoeq.entity.subterm = :aoTerm " +
                        "     or phenoeq.relatedEntity.superterm = :aoTerm " +
                        "     or phenoeq.relatedEntity.subterm = :aoTerm) " +
                        "AND phenoeq.tag != :tag " +
                        "AND genox.experiment.name in (:condition) " +
                        "AND not exists (select 1 from ExperimentCondition cond where" +
                        " cond.experiment = genox.experiment " +
                        " AND cond.morpholino is not null ) ";

        if (!wildtype) {
            hql += "AND genox.genotype.wildtype = 'f' ";
        }
        hql += "ORDER BY genox.genotype.nameOrder asc";

        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        query.setParameterList("condition", Experiment.STANDARD_CONDITIONS);
        // have to add extra select because of ordering, but we only want to return the first
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple[0] ;
            }
        });

        return PaginationResultFactory.createResultFromScrollableResultAndClose(numberOfRecords, query.scroll());
    }

    @SuppressWarnings({"unchecked"})
    public List<Genotype> getGenotypesByFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql =
                "select  geno from Genotype geno, GenotypeFeature genofeat, Feature feat, FeatureType type " +
                        "WHERE  genofeat.feature.zdbID= :zdbID " +
                        "AND genofeat.genotype =geno " +
                        "AND genofeat.feature =feat " +
                        "AND type.name=feat.type " +
//                        "AND type.name=feat.type.name " + // HQL interpets this wrong, but the above right
                        "ORDER by type.significance, geno.nameOrder ";


        Query query = session.createQuery(hql);
        query.setString("zdbID", feature.getZdbID());
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
                "exp.genotypeExperiment.genotype.zdbID = :genoZdbID ";
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
                "exp.genotypeExperiment.genotype.zdbID = :genoZdbID ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setParameter("term", item);
        query.setString("genoZdbID", genotype.getZdbID());

        return ((Number) query.uniqueResult()).intValue();
    }

    /**
     * Retrieve all morpholinos that have a phenotype annotation for a given
     * anatomical structure. Gene expressions are not included in this list.
     * ToDo: number of Records is not used yet until an overview page of all
     * morpholinos is working.
     *
     * @param item            anatomical structure
     * @param numberOfRecords number
     * @return list of statistics
     */
    public List<Morpholino> getPhenotypeMorpholinos(GenericTerm item, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        // This returns morpholinos by phenote annotations
        StringBuilder hql = new StringBuilder("SELECT distinct genotype ");
        hql.append(getMorpholinoGenotypeByAnatomyTermQueryBlock());
        hql.append(" order by genotype.name ");
        Query query = session.createQuery(hql.toString());
        // ToDo: MOs are not yet linked to an overview page where all all them are listed.
        //. Thus, we need to list them all here.
//        query.setMaxResults(numberOfRecords);
        query.setParameter("aoTerm", item);
        query.setBoolean("isWildtype", true);

        @SuppressWarnings("unchecked")
        List<Morpholino> morphs = new ArrayList<Morpholino>();
        morphs.addAll(getMorpholinoRecords(null));

        //retrieve morpholinos annotated through the expression section
/*
        String expressionHql = "select distinct marker FROM  Marker marker, Experiment exp, " +
                "      ExperimentCondition con, GenotypeExperiment geno, " +
                "      ExpressionExperiment xpat, ExpressionResult result " +
                "WHERE   " +
                "       geno.experiment = exp AND " +
                "       xpat.genotypeExperiment = geno AND " +
                "       result.expressionExperiment = xpat AND " +
                "       result.anatomyTerm = :aoZdbID AND " +
                "       con.experiment = exp AND " +
                "       marker = con.morpholino ";
*/
        return morphs;
    }

    /**
     * Retrieve the genotype objects that are associated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @return list of genotype object
     */
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(GenericTerm item, Boolean isWildtype, int numberOfRecords) {
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(numberOfRecords);
        return getGenotypeExperimentMorpholinos(item, isWildtype, bean);
    }

    public List<GenotypeExperiment> getGenotypeExperimentMorpholinos(GenericTerm item, Boolean isWildtype) {
        return getGenotypeExperimentMorpholinos(item, isWildtype, null).getPopulatedResults();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(GenericTerm item, Boolean isWildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct genotypeExperiment " +
                "FROM  GenotypeExperiment genotypeExperiment, Experiment exp, Genotype geno, " +
                "      PhenotypeExperiment phenox, PhenotypeStatement phenoeq, ExperimentCondition con, Marker marker " +
                "WHERE   " +
                "      genotypeExperiment.experiment = exp AND phenoeq.phenotypeExperiment = phenox AND " +
                "       (phenoeq.entity.superterm = :aoTerm " +
                "        or phenoeq.entity.subterm = :aoTerm " +
                "        or phenoeq.relatedEntity.superterm = :aoTerm " +
                "        or phenoeq.relatedEntity.subterm = :aoTerm ) AND " +
                "       phenox.genotypeExperiment = genotypeExperiment AND " +
                "       phenoeq.tag != :tag AND " +
                "       con.experiment = exp AND " +
                "       genotypeExperiment.genotype = geno AND " +
                "       marker = con.morpholino AND " +
                "       not exists (select 1 from ExperimentCondition expCon where expCon.experiment = exp AND " +
                "                             expCon.morpholino is null ) ";
        if(isWildtype != null){
            hql += " AND geno.wildtype = :isWildtype ";
        }
        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", PhenotypeStatement.Tag.NORMAL.toString());
        if (isWildtype != null){
            query.setBoolean("isWildtype", isWildtype);
        }

        // no boundaries defined, all records
        if (bean == null) {
            @SuppressWarnings("unchecked")
            List<GenotypeExperiment> genos = (List<GenotypeExperiment>) query.list();
            return new PaginationResult<GenotypeExperiment>(genos);
        }

        // use boundary definitions
        return PaginationResultFactory.createResultFromScrollableResultAndClose(bean, query.scroll());
    }

    @SuppressWarnings("unchecked")
    public List<Morpholino> getMorpholinosByGenotype(Genotype genotype, AnatomyItem item, boolean isWildtype) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("SELECT distinct marker ");
        hql.append("FROM  Marker marker, Experiment exp, Genotype geno, ");
        hql.append("      Phenotype pheno, ExperimentCondition con, GenotypeExperiment genox ");
        hql.append("WHERE   ");
        hql.append("       genox.experiment = exp AND ");
        hql.append("       genox member of geno.genotypeExperiments AND ");
        hql.append("       pheno.genotypeExperiment = genox AND ");
        hql.append("       pheno.patoSuperTermzdbID = :aoZdbID AND ");
        hql.append("       geno = :genotype AND ");
        hql.append("       con.experiment = exp AND ");
        hql.append("       geno.wildtype = :isWildtype AND ");
        hql.append("       marker = con.morpholino ");
        hql.append(" order by marker.name ");
        Query query = session.createQuery(hql.toString());
        query.setBoolean("isWildtype", isWildtype);
        query.setString("aoZdbID", item.getZdbID());
        query.setParameter("genotype", genotype);

        return (List<Morpholino>) query.list();
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

    private List<Morpholino> getMorpholinoRecords(List<Marker> markers) {
        List<Morpholino> morphs = new ArrayList<Morpholino>(5);
        if (markers != null) {
            // ToDo: Integrate Morpholinos better in the Marker: Inherit from it an map it better in Hibernate.
            for (Marker marker : markers) {
                Morpholino morph = new Morpholino();
                morph.setMarkerType(marker.getMarkerType());
                morph.setAbbreviation(marker.getAbbreviation());
                morph.setZdbID(marker.getZdbID());

                Set<MarkerRelationship> rels = marker.getFirstMarkerRelationships();
                if (rels != null) {
                    for (MarkerRelationship rel : rels) {
                        if (rel.getType() == MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE)
                            morph.setTargetGene(rel.getSecondMarker());
                    }
                }
                morphs.add(morph);
            }
        }
        return morphs;
    }

    // ToDo: See FogBugz 1926: Include morpholinos from expression object.

    private String getMorpholinosByAnatomyTermQueryBlock() {
        String hql = "FROM  Marker marker, Experiment exp, " +
                "      Phenotype pheno, ExperimentCondition con, GenotypeExperiment geno " +
                "WHERE   " +
                "       geno.experiment = exp AND " +
                "       pheno.patoSuperTermzdbID = :aoZdbID AND " +
                "       pheno.genotypeExperiment = geno AND " +
                "       con.experiment = exp AND " +
                "       marker = con.morpholino ";
        return hql;
    }

    private String getMorpholinoGenotypeByAnatomyTermQueryBlock() {
        String hql = "FROM  Genotype genotype, Experiment exp, " +
                "      PhenotypeStatement pheno, ExperimentCondition con, GenotypeExperiment geno " +
                "WHERE   " +
                "       geno.experiment = exp AND " +
                "       (pheno.entity.superterm = :aoTerm OR pheno.entity.subterm = :aoTerm ) AND " +
                "       pheno.phenotypeExperiment.genotypeExperiment = geno AND " +
                "       con.experiment = exp AND " +
                "       geno.genotype = genotype AND" +
                "       genotype.wildtype = :isWildtype";
        return hql;
    }

    public int getMorhpolinoCountByAnatomy(AnatomyItem item, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();
        StringBuilder hql = new StringBuilder("SELECT marker ");
        hql.append(getMorpholinosByAnatomyTermQueryBlock());
        hql.append(" order by marker.abbreviation ");
        Query query = session.createQuery(hql.toString());
        query.setMaxResults(numberOfRecords);
        query.setString("aoZdbID", item.getZdbID());
        return ((Number) query.uniqueResult()).intValue();
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

    /* public List<Marker> getDeletedMarker(Feature feat) {
        Session session = HibernateUtil.currentSession();

        String hql = "select  mapdel.marker from MappedDeletion mapdel, Marker m, Feature f" +
                " where f.name =mapdel.allele " +
                " AND f.name=:ftr" +
                " AND m.markerType.name =:type" +
                " AND mapdel.marker =m";


        Query query = session.createQuery(hql);
        query.setString("ftr", feat.getName());
        query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();


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
    }*/

    /**
     * Retrieve all distinct wild-type genotypes.
     *
     * @return list of wildtype fish
     */
    @SuppressWarnings("unchecked")
    public List<Genotype> getAllWildtypeGenotypes() {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct geno from Genotype geno" +
                "     where geno.wildtype = :isWildtype" +
                "           and geno.handle != :wt " +
                "    order by geno.nameOrder ";
        Query query = session.createQuery(hql);
        query.setBoolean("isWildtype", true);
        query.setString("wt", Genotype.WT);

        return (List<Genotype>) query.list();
    }

    /**
     * Check if for a given figure annotation a pato record (Phenotype)
     *
     * @param genotypeExperimentID expression experiment
     * @param figureID             figure
     * @param startID              start   stage
     * @param endID                end     stage
     * @param publicationID        publication
     * @return boolean
     */
    public boolean isPatoExists(String genotypeExperimentID, String figureID, String startID, String endID, String publicationID) {
        if (genotypeExperimentID == null) {
            throw new NullPointerException("Invalid call to method: genoxExperimentID is null");
        }
        if (figureID == null) {
            throw new NullPointerException("Invalid call to method: figureID is null");
        }
        if (startID == null) {
            throw new NullPointerException("Invalid call to method: startID is null");
        }
        if (endID == null) {
            throw new NullPointerException("Invalid call to method: endID is null");
        }
        if (publicationID == null) {
            throw new NullPointerException("Invalid call to method: publicationID is null");
        }
        long start = System.currentTimeMillis();
        Session session = HibernateUtil.currentSession();

        String hql = "select count(phenox) from PhenotypeExperiment phenox, Figure figure " +
                "     where phenox.genotypeExperiment.zdbID = :genoxID" +
                "           and phenox.startStage.zdbID = :startID " +
                "           and phenox.endStage.zdbID = :endID " +
                "           and phenox.figure.zdbID = :figureID " +
                "           and figure.publication.zdbID = :publicationID ";
        Query query = session.createQuery(hql);
        query.setString("genoxID", genotypeExperimentID);
        query.setString("startID", startID);
        query.setString("endID", endID);
        query.setString("figureID", figureID);
        query.setString("publicationID", publicationID);

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
    public GenotypeExperiment getGenotypeExperiment(String genotypeExperimentID) {
        Session session = HibernateUtil.currentSession();
        return (GenotypeExperiment) session.get(GenotypeExperiment.class, genotypeExperimentID);
    }

    public GenotypeExperiment getGenotypeExperiment(String genotypeZdbID, String experimentZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(GenotypeExperiment.class);
        crit.createAlias("genotype", "geno");
        crit.createAlias("experiment", "exp");
        crit.add(Restrictions.eq("geno.zdbID", genotypeZdbID));
        crit.add(Restrictions.eq("exp.zdbID", experimentZdbID));
        return (GenotypeExperiment) crit.uniqueResult();
    }

    /**
     * Remove a PhenotypeExperiment record:
     * 1) All matching phenotype statements
     * 2) The genotype experiment if left unused after removing phenotypes.
     *
     * @param phenoExperiment Mutants
     */
    public void deletePhenotypeExperiment(PhenotypeExperiment phenoExperiment) {
        if (phenoExperiment == null)
            throw new NullPointerException("No PhenotypeExperiment provided");
        GenotypeExperiment genotypeExperiment = phenoExperiment.getGenotypeExperiment();
        if (genotypeExperiment == null || genotypeExperiment.getZdbID() == null)
            throw new NullPointerException("No genotype experiment provided");

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
        if (genotypeExperiment.getExpressionExperiments() == null)
            session.delete(genotypeExperiment);
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
        for (Iterator<InferenceGroupMember> iterator = markerGoTermEvidence.getInferredFrom().iterator(); iterator.hasNext();) {
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
        Criteria criteria = session.createCriteria(DataAlias.class);
        criteria.add(Restrictions.eq("feature", feature));
        criteria.add(Restrictions.eq("alias", alias));
        return (FeatureAlias) criteria.uniqueResult();
    }

    public FeatureDBLink getSpecificDBLink(Feature feature, String accessionNumber) {
         Session session = HibernateUtil.currentSession();
          String hql = "select distinct ftrDbLink from FeatureDBLink ftrDbLink  where " +
                " ftrDbLink.feature = :feature " ;
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("feature", feature);
        return (FeatureDBLink) query.uniqueResult();



    }


    public int getZFINInferences(String zdbID, String publicationZdbID) {
        return Integer.valueOf(HibernateUtil.currentSession().createSQLQuery("" +
                " select count(*) from marker_go_term_evidence  ev  " +
                " join  inference_group_member inf on ev.mrkrgoev_zdb_id=inf.infgrmem_mrkrgoev_zdb_id " +
                " where " +
                " ev.mrkrgoev_source_zdb_id=:pubZdbID " +
                " and " +
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


    @SuppressWarnings("unchecked")
    @Override
    public List<MorpholinoSequence> getMorpholinosWithMarkerRelationships() {


        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        final String queryString = "select m.zdbID ,m.abbreviation, s.sequence   from MarkerSequenceMarker m  " +
                "inner join m.sequences s " +
                "inner join m.firstMarkerRelationships  " +
                "where m.markerType =  :markerType ";

        final Query query = HibernateUtil.currentSession().createQuery(queryString);
        query.setString("markerType", Marker.Type.MRPHLNO.toString());
        List<Object[]> sequences = query
                .list();

        List<MorpholinoSequence> morpholinoSequences = new ArrayList<MorpholinoSequence>();
        for (Object[] seqObjects : sequences) {
            MorpholinoSequence morpholinoSequence = new MorpholinoSequence();
            morpholinoSequence.setZdbID(seqObjects[0].toString());
            morpholinoSequence.setName(seqObjects[1].toString());
            morpholinoSequence.setSequence(seqObjects[2].toString());
            morpholinoSequences.add(morpholinoSequence);
        }
        return morpholinoSequences;
    }

    /**
     * Retrieve phenotypes that have an annotation to a given term
     * with tag=abnormal and the term either in super or sub position
     *
     * @param term Term
     * @return list of phenotypes
     */
    @Override
    public List<PhenotypeStatement> getPhenotypeWithEntity(GenericTerm term) {
        String hql = "select distinct pheno from PhenotypeStatement pheno where " +
                "(pheno.entity.superterm = :term OR pheno.entity.subterm = :term OR " +
                "pheno.relatedEntity.superterm = :term OR pheno.relatedEntity.subterm = :term ) " +
                "AND tag = :tag";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        query.setString("tag", PhenotypeStatement.Tag.ABNORMAL.toString());
        return (List<PhenotypeStatement>) query.list();
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
    public List<PhenotypeStatement> getPhenotypeWithEntity(List<GenericTerm> terms) {
        List<PhenotypeStatement> allPhenotypes = new ArrayList<PhenotypeStatement>(50);
        for (GenericTerm term : terms) {
            List<PhenotypeStatement> phenotypes = getPhenotypeWithEntity(term);
            allPhenotypes.addAll(phenotypes);
        }
        List<PhenotypeStatement> nonDuplicateExpressions = removeDuplicates(allPhenotypes);
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


    private List<PhenotypeStatement> removeDuplicates(List<PhenotypeStatement> allPhenotypes) {
        Set<PhenotypeStatement> phenos = new HashSet<PhenotypeStatement>();
        for (PhenotypeStatement pheno : allPhenotypes) {
            phenos.add(pheno);
        }
        ArrayList<PhenotypeStatement> phenotypeArrayList = new ArrayList<PhenotypeStatement>(phenos.size());
        phenotypeArrayList.addAll(phenos);
        return phenotypeArrayList;
    }

    public List<GenotypeFigure> getCleanGenoFigsByGenotype(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select cleanPheno from GenotypeFigure cleanPheno " +
                "     where cleanPheno.genotype.zdbID = :genotypeID";
        Query query = session.createQuery(hql);
        query.setString("genotypeID", genotype.getZdbID());

        return (List<GenotypeFigure>) query.list();
    }

    /**
     * get a phenotype statement by id
     */
    public PhenotypeStatement getPhenotypeStatementById(Long id) {
        Session session = currentSession();
        return (PhenotypeStatement) session.get(PhenotypeStatement.class, id);
    }

    /**
     * Retrieve the phenotypes that are annotated with obsoleted terms.
     *
     * @return list of phenotypes
     */
    @Override
    public List<PhenotypeStatement> getPhenotypesOnObsoletedTerms() {
        Session session = HibernateUtil.currentSession();
        List<PhenotypeStatement> allPhenotypes = new ArrayList<PhenotypeStatement>();

        String hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.quality is not null AND phenotype.quality.obsolete = :obsolete";
        Query query = session.createQuery(hql);
        query.setBoolean("obsolete", true);

        allPhenotypes.addAll((List<PhenotypeStatement>) query.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.entity.superterm is not null AND phenotype.entity.superterm.obsolete = :obsolete";
        Query queryEntitySuper = session.createQuery(hql);
        queryEntitySuper.setBoolean("obsolete", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.entity.subterm is not null AND phenotype.entity.subterm.obsolete = :obsolete";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("obsolete", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryEntitySub.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.relatedEntity.superterm is not null AND phenotype.relatedEntity.superterm.obsolete = :obsolete";
        Query queryRelatedEntitySuper = session.createQuery(hql);
        queryRelatedEntitySuper.setBoolean("obsolete", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySuper.list());

        hql = "select phenotype from PhenotypeStatement phenotype " +
                "     where phenotype.relatedEntity.subterm is not null AND phenotype.relatedEntity.subterm.obsolete = :obsolete";
        Query queryRelatedEntitySub = session.createQuery(hql);
        queryRelatedEntitySub.setBoolean("obsolete", true);
        allPhenotypes.addAll((List<PhenotypeStatement>) queryRelatedEntitySub.list());

        return allPhenotypes;
    }

    /**
     * Returns a list phenotype statements that are related to
     * a given genotype.
     *
     * @param genotype Genotype
     * @return list of phenotype statement objects
     */
    public List<PhenotypeStatement> getPhenotypeStatementsByGenotype(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct phenoStatement from PhenotypeStatement phenoStatement " +
                "WHERE phenoStatement.phenotypeExperiment.genotypeExperiment.genotype = :genotype";

        Query query = session.createQuery(hql);
        query.setParameter("genotype", genotype);

        return (List<PhenotypeStatement>) query.list();
    }


    public void runFeatureNameFastSearchUpdate(Feature feature) {
        Session session = currentSession();
        Connection connection = session.connection();
        CallableStatement statement = null;
        String sql = "execute procedure regen_names_feature(?)";
        try {
            statement = connection.prepareCall(sql);
            String zdbID = feature.getZdbID();
            statement.setString(1, zdbID);
            statement.execute();
            logger.info("Execute stored procedure: " + sql + " with the argument " + zdbID);
        } catch (SQLException e) {
            logger.error("Could not run: " + sql, e);
            logger.error(DbSystemUtil.getLockInfo());
        } finally {
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
        }
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
        if (termUsage != null)
            histogram.put(termUsageHistogram, termUsage + number);
        else
            histogram.put(termUsageHistogram, number);
    }

    @Override
    public String getMutantLinesDisplay(String zdbID) {
        return HibernateUtil.currentSession()
                .createSQLQuery("execute function get_mutants_html_link( :markerZdbId)")
                .setString("markerZdbId",zdbID)
                .uniqueResult().toString();
    }

    @Override
    public List<FeaturePresentationBean> getAllelesForMarker(String zdbID) {
        String sql = "select feature_abbrev,feature_zdb_id from feature, feature_marker_relationship, feature_marker_relationship_type " +
                "              where fmrel_ftr_zdb_id=feature_zdb_id " +
                "              and fmrel_mrkr_zdb_id=:markerZdbId " +
                "              and fmrel_type=fmreltype_name " +
                "              and  fmreltype_produces_affected_marker='t'	" +
                "              order  by feature_abbrev	" +
                " ";
        List<FeaturePresentationBean> list = (List<FeaturePresentationBean>) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId",zdbID)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        FeaturePresentationBean featurePresentationBean =new FeaturePresentationBean();
                        featurePresentationBean.setAbbrevation(tuple[0].toString());
                        featurePresentationBean.setFeatureZdbId(tuple[1].toString());
                        return featurePresentationBean ;
                    }
                })
                .list();
        return list ;
    }

    @Override
    public List<Marker> getKnockdownReagents(Marker gene) {
        String hql = " select m from Marker m join m.firstMarkerRelationships mr " +
                " where mr.secondMarker.zdbID = :markerZdbId  " +
                " and mr.markerRelationshipType.name = :type " +
                " order by m.abbreviationOrder " +
                "" ;
        return HibernateUtil.currentSession().createQuery(hql)
                .setString("markerZdbId", gene.getZdbID())
                .setString("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE.toString())
                .list();
    }

    @Override
    public List<String> getTransgenicLines(Marker construct){
        String sql = " " +
                "select distinct get_geno_name_with_bg_html_link(genofeat_geno_zdb_id) " +
                "   from genotype_feature, feature_marker_relationship " +
                "   where fmrel_mrkr_zdb_id = :markerZdbID " +
                "   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id " +
                "   and fmrel_type like 'contains%'";
        return HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbID",construct.getZdbID())
                .list();
    }

}
