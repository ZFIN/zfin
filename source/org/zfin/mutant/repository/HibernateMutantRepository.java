package org.zfin.mutant.repository;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.InferenceCategory;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerSequenceMarker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MorpholinoSequence;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;


/**
 *
 */
public class HibernateMutantRepository implements MutantRepository {

    public PaginationResult<Genotype> getGenotypesByAnatomyTerm(Term item, boolean wildtype, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        String hql =
                "select distinct genox.genotype from GenotypeExperiment genox, Phenotype pheno " +
                        "WHERE pheno.genotypeExperiment = genox " +
                        "AND (pheno.superterm = :aoTerm or pheno.subterm = :aoTerm ) " +
                        "AND pheno.tag != :tag " +
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
        query.setParameter("tag", Phenotype.Tag.NORMAL.toString());
        query.setParameterList("condition", Experiment.STANDARD_CONDITIONS);

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
                        "AND type.name=feat.featureType.name " +
                        "ORDER by type.significance, geno.nameOrder ";


        Query query = session.createQuery(hql);
        query.setString("zdbID", feature.getZdbID());
        return (List<Genotype>) query.list();
    }


    public int getNumberOfImagesPerAnatomyAndMutant(Term term, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(distinct image) from Image image, Figure fig, ExpressionResult res, " +
                "                                  ExpressionExperiment exp " +
                "where " +
                "res member of exp.expressionResults AND " +
                "res.superterm = :term AND " +
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

    public int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(Term item, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(distinct figure.publication) from Figure figure, ExpressionResult res, " +
                "                                               ExpressionExperiment exp " +
                "where " +
                "res member of exp.expressionResults AND " +
                "res.superterm = :term AND " +
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
    public List<Morpholino> getPhenotypeMorpholinos(Term item, int numberOfRecords) {
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
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(Term item, Boolean isWildtype, int numberOfRecords) {
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(numberOfRecords);
        return getGenotypeExperimentMorpholinos(item, isWildtype, bean);
    }

    public List<GenotypeExperiment> getGenotypeExperimentMorpholinos(Term item, boolean isWildtype) {
        return getGenotypeExperimentMorpholinos(item, isWildtype, null).getPopulatedResults();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(Term item, Boolean isWildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct genotypeExperiment " +
                "FROM  GenotypeExperiment genotypeExperiment, Experiment exp, Genotype geno, " +
                "      Phenotype pheno, ExperimentCondition con, Marker marker " +
                "WHERE   " +
                "      genotypeExperiment.experiment = exp AND " +
                "       (pheno.superterm = :aoTerm or pheno.subterm = :aoTerm) AND " +
                "       pheno.genotypeExperiment = genotypeExperiment AND " +
                "       pheno.tag != :tag AND " +
                "       con.experiment = exp AND " +
                "       genotypeExperiment.genotype = geno AND " +
                "       marker = con.morpholino AND " +
                "       geno.wildtype = :isWildtype AND " +
                "       not exists (select 1 from ExperimentCondition expCon where expCon.experiment = exp AND " +
                "                             expCon.morpholino is null ) ";
        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", item);
        query.setParameter("tag", Phenotype.Tag.NORMAL.toString());
        query.setBoolean("isWildtype", isWildtype);

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
                "      Phenotype pheno, ExperimentCondition con, GenotypeExperiment geno " +
                "WHERE   " +
                "       geno.experiment = exp AND " +
                "       (pheno.superterm = :aoTerm OR pheno.subterm = :aoTerm )AND " +
                "       pheno.genotypeExperiment = geno AND " +
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

    @SuppressWarnings("unchecked")

    public Feature getFeatureByID(String featureZdbID) {
        Session session = HibernateUtil.currentSession();
        return (Feature) session.get(Feature.class, featureZdbID);
    }

    public List<Marker> getMarkerbyFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                "     where fmrel.feature.zdbID = :feat" +
                " and fmrel.type in (:relation, :relationship1, :relationship2) " +
                " and fmrel.marker=m ";


        Query query = session.createQuery(hql);

        query.setString("feat", feature.getZdbID());
        query.setString("relation", FeatureMarkerRelationship.Type.IS_ALLELE_OF.toString());
        query.setString("relationship1", FeatureMarkerRelationship.Type.MARKERS_PRESENT.toString());
        query.setString("relationship2", FeatureMarkerRelationship.Type.MARKERS_MISSING.toString());
        //query.setParameter("type", Marker.Type.GENE);
        //query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();
    }

    public List<Marker> getMarkerPresent(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmrel.marker from  FeatureMarkerRelationship fmrel, Marker m" +
                "     where fmrel.feature.zdbID = :feat" +
                " and fmrel.type=:relationship " +
                " and fmrel.marker=m " +
                " and m.type=:type";


        Query query = session.createQuery(hql);
        query.setParameter("relationship", FeatureMarkerRelationship.Type.MARKERS_PRESENT.toString());
        query.setString("feat", feature.getZdbID());


        //query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();
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

    public TreeSet<String> getFeatureLG(Feature feat) {
        Session session = HibernateUtil.currentSession();
        TreeSet<String> lgList = new TreeSet<String>();


        String hql = "select distinct mm.lg" +
                "  from MappedMarker mm" +
                "   where mm.marker.zdbID=:ftr ";
        Query query = session.createQuery(hql);
        query.setString("ftr", feat.getZdbID());
        lgList.addAll(query.list());

        query = session.createQuery(
                "select l.lg " +
                        "from Linkage l join l.linkageMemberFeatures as m " +
                        " where m.zdbID = :zdbId ");
        query.setParameter("zdbId", feat.getZdbID());
        lgList.addAll(query.list());
        return lgList;
    }


    public List<Feature> getFeaturesByAbbreviation(String name) {
        List<Feature> features = new ArrayList<Feature>();
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Feature.class);
        criteria1.add(Restrictions.like("abbreviation", name, MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviationOrder"));
        features.addAll(criteria1.list());

        Criteria criteria2 = session.createCriteria(Feature.class);
        criteria2.add(Restrictions.like("abbreviation", name, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.like("abbreviation", name, MatchMode.START)));
        criteria2.addOrder(Order.asc("abbreviationOrder"));
        features.addAll(criteria2.list());
        return features;
    }

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
     * @return boolean
     */
    public boolean isPatoExists(String genotypeExperimentID, String figureID, String startID, String endID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select pheno from Phenotype pheno, Figure figure " +
                "     where pheno.genotypeExperiment.zdbID = :genoxID" +
                "           and pheno.startStage.zdbID = :startID " +
                "           and pheno.endStage.zdbID = :endID " +
                "           and figure member of pheno.figures " +
                "           and figure.zdbID = :figureID";
        Query query = session.createQuery(hql);
        query.setString("genoxID", genotypeExperimentID);
        query.setString("startID", startID);
        query.setString("endID", endID);
        query.setString("figureID", figureID);

        List list = query.list();
        return (list != null && list.size() > 0);

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
     * Retrieve the default phenotype (AO:unspecified - quality [abnormal]) for a given figure and genotype experiment.
     * If it does not exist, it return null.
     *
     * @param genotypeExperiment genotype Experiment
     * @param figureID           figure id
     * @return phenotype
     */
    public Phenotype getDefaultPhenotype(GenotypeExperiment genotypeExperiment, String figureID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select pheno from Phenotype pheno, Figure figure " +
                "     where pheno.genotypeExperiment = :genox" +
                "           and pheno.superterm = :unspecified " +
                "           and pheno.term.ID= :qualityZdbID " +
                "           and pheno.tag = :tag " +
                "           and figure member of pheno.figures " +
                "           and figure.zdbID = :figureID";
        Term unspecified = getOntologyRepository().getTermByName(Term.UNSPECIFIED, Ontology.ANATOMY);
        GenericTerm quality = RepositoryFactory.getInfrastructureRepository().getTermByName("quality", Ontology.QUALITY);
        Query query = session.createQuery(hql);
        query.setParameter("genox", genotypeExperiment);
        query.setString("figureID", figureID);
        query.setParameter("unspecified", unspecified);
        query.setString("qualityZdbID", quality.getID());
        query.setString("tag", Phenotype.Tag.ABNORMAL.name());
        return (Phenotype) query.uniqueResult();
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

    /**
     * Remove a mutant figure stage record:
     * 1) All matching phenotypes and their association to figures.
     * 2) The genotype experiment if left unused after removing phenotypes.
     *
     * @param mutant Mutants
     */
    public void deleteMutantFigureStage(MutantFigureStage mutant) {
        if (mutant == null)
            throw new NullPointerException("No Mutant Figure Annotation provided");
        GenotypeExperiment genotypeExperiment = mutant.getGenotypeExperiment();
        if (genotypeExperiment == null || genotypeExperiment.getZdbID() == null)
            throw new NullPointerException("No genotype experiment provided");

        Set<Phenotype> phenotypes = mutant.getMatchingMutantPhenotypes();
        if (phenotypes == null || phenotypes.isEmpty())
            return;

        boolean allPhenotypesAffected = genotypeExperiment.getPhenotypes().size() == phenotypes.size();

        // delete all phenotype records.
        Session session = HibernateUtil.currentSession();
        for (Phenotype phenotype : phenotypes) {
            // remove only when there is exactly one figure associated.
            if (phenotype.getFigures().size() == 1)
                session.delete(phenotype);
                // otherwise remove association to figures
            else {
                phenotype.getFigures().remove(mutant.getFigure());
                allPhenotypesAffected = false;
            }
        }

        // delete genotype experiment if it has no more phenotypes associated
        // and if it is not used in FX (expression_experiment)
        if (allPhenotypesAffected && genotypeExperiment.getExpressionExperiments() == null)
            session.delete(genotypeExperiment);


    }

    @SuppressWarnings({"unchecked"})
    public List<Feature> getFeaturesForStandardAttribution(Publication publication) {
        String hql = "select f from PublicationAttribution pa , Feature f " +
                " where pa.dataZdbID=f.zdbID and pa.publication.zdbID= :pubZdbID  " +
                " and pa.sourceType= :sourceType  ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubZdbID", publication.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
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
    public List<Term> getGoTermsByMarkerAndPublication(Marker marker, Publication publication) {
        String hql = "select distinct g from PublicationAttribution pa , GenericTerm g , MarkerGoTermEvidence ev " +
                " where pa.dataZdbID=ev.zdbID " +
                " and pa.publication.zdbID= :pubZdbID  " +
                " and ev.marker.zdbID = :markerZdbID " +
                " and ev.evidenceCode.code not in (:excludedEvidenceCodes) " +
                " and g.ID= ev.goTerm.ID " +
                " and pa.sourceType= :sourceType  " +
                " order by g.termName  ";
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
    public List<Term> getGoTermsByPhenotypeAndPublication(Publication publication) {
        String hql = "select distinct phenotype from Phenotype phenotype , GenericTerm g  " +
                " where phenotype.publication.zdbID= :pubZdbID  " +
                " and ( phenotype.subterm = g and g.oboID like :oboIDLike" +
                " or phenotype.superterm = g )";
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
    public List<Feature> getFeaturesForAttribution(String publicationZdbID) {
        String hql = "" +
                " select distinct f from Feature f , RecordAttribution ra " +
                " where ra.dataZdbID=f.zdbID and ra.sourceType = :standard and ra.sourceZdbID = :pubZdbID " +
                " order by f.abbreviationOrder " +
                " ";

        return (List<Feature>) HibernateUtil.currentSession().createQuery(hql)
                .setString("pubZdbID", publicationZdbID)
                .setString("standard", RecordAttribution.SourceType.STANDARD.toString())
                .list();
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

    @Override
    public Feature getFeatureByAbbreviation(String name) {
        return (Feature) currentSession().createCriteria(Feature.class)
                .add(Restrictions.eq("abbreviation", name))
                .uniqueResult()
                ;
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


    public void invalidateCachedObjects() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MorpholinoSequence> getMorpholinosWithMarkerRelationships() {

        // using this type of query for both speed (an explicit join)
        // and because createSQLQuery had trouble binding the lvarchar of s.sequence
        List<Object[]> sequences = HibernateUtil.currentSession().createQuery(
                "select m.zdbID ,m.abbreviation, s.sequence   from MarkerSequenceMarker m  " +
                        "inner join m.sequences s " +
                        "inner join m.firstMarkerRelationships  " +
                        "where m.markerType =  '" + Marker.Type.MRPHLNO + "' " +
                        "")
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
    public List<Phenotype> getPhenotypeWithEntity(Term term) {
        String hql = "select distinct pheno from Phenotype pheno where " +
                "(superterm = :term OR subterm = :term) " +
                "AND tag = :tag";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        query.setString("tag", Phenotype.Tag.ABNORMAL.toString());
        return (List<Phenotype>) query.list();
    }

    /**
     * Retrieve all distinct marker go evidence objects for a given term.
     *
     * @param term term
     * @return list of marker go
     */
    @Override
    public List<MarkerGoTermEvidence> getMarkerGoEvidence(Term term) {
        String hql = "select distinct evidence from MarkerGoTermEvidence evidence where " +
                " goTerm = :term ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        return (List<MarkerGoTermEvidence>) query.list();
    }

    @Override
    public List<Phenotype> getPhenotypeWithEntity(List<Term> terms) {
        List<Phenotype> allPhenotypes = new ArrayList<Phenotype>(50);
        for (Term term : terms) {
            List<Phenotype> phenotypes = getPhenotypeWithEntity(term);
            allPhenotypes.addAll(phenotypes);
        }
        List<Phenotype> nonDuplicateExpressions = removeDuplicates(allPhenotypes);
        Collections.sort(nonDuplicateExpressions);
        return nonDuplicateExpressions;

    }

    @Override
    public List<MarkerGoTermEvidence> getMarkerGoEvidence(List<Term> terms) {
        List<MarkerGoTermEvidence> allMarkerGo = new ArrayList<MarkerGoTermEvidence>();
        for (Term term : terms) {
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
            String id = evidence.getMarker().getZdbID() + ":" + evidence.getGoTerm().getID();
            if (!uniqueID.contains(id)) {
                phenotypeArrayList.add(evidence);
                uniqueID.add(id);
            }
        }
        return phenotypeArrayList;
    }


    private List<Phenotype> removeDuplicates(List<Phenotype> allPhenotypes) {
        Set<Phenotype> phenos = new HashSet<Phenotype>();
        for (Phenotype pheno : allPhenotypes) {
            phenos.add(pheno);
        }
        ArrayList<Phenotype> phenotypeArrayList = new ArrayList<Phenotype>(phenos.size());
        phenotypeArrayList.addAll(phenos);
        return phenotypeArrayList;
    }


}
