package org.zfin.mutant.repository;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Experiment;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GoTerm;
import org.zfin.publication.Publication;
import org.zfin.ontology.Ontology;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;


/**
 *
 */
public class HibernateMutantRepository implements MutantRepository {

    public PaginationResult<Genotype> getGenotypesByAnatomyTerm(AnatomyItem item, boolean wildtype, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        String hql =
                "select distinct genox.genotype from GenotypeExperiment genox, AnatomyPhenotype pheno " +
                        "WHERE pheno.genotypeExperiment = genox " +
                        "AND (pheno.anatomySuperTerm = :aoTerm or pheno.anatomySubTerm = :aoTerm ) " +
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
        List<Genotype> genotypes = query.list();
        return genotypes;


    }


    public int getNumberOfImagesPerAnatomyAndMutant(AnatomyItem item, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(distinct image) from Image image, Figure fig, ExpressionResult res, " +
                "                                  ExpressionExperiment exp " +
                "where " +
                "res member of exp.expressionResults AND " +
                "res.anatomyTerm.zdbID = :aoZdbID AND " +
                "fig member of res.figures AND " +
                "image member of fig.images AND " +
                "res.expressionFound = :expressionFound AND " +
                "exp.genotypeExperiment.genotype.zdbID = :genoZdbID ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setString("aoZdbID", item.getZdbID());
        query.setString("genoZdbID", genotype.getZdbID());

        return ((Number) query.uniqueResult()).intValue();
    }

    public int getNumberOfPublicationsPerAnatomyAndMutantWithFigures(AnatomyItem item, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(distinct figure.publication) from Figure figure, ExpressionResult res, " +
                "                                               ExpressionExperiment exp " +
                "where " +
                "res member of exp.expressionResults AND " +
                "res.anatomyTerm.zdbID = :aoZdbID AND " +
                "figure member of res.figures AND " +
                "res.expressionFound = :expressionFound AND " +
                "exp.genotypeExperiment.genotype.zdbID = :genoZdbID ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setString("aoZdbID", item.getZdbID());
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
    public List<Morpholino> getPhenotypeMorpholinos(AnatomyItem item, int numberOfRecords) {
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
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(AnatomyItem item, Boolean isWildtype, int numberOfRecords) {
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(numberOfRecords);
        return getGenotypeExperimentMorpholinos(item, isWildtype, bean);
    }

    public List<GenotypeExperiment> getGenotypeExperimentMorpholinos(AnatomyItem item, boolean isWildtype) {
        return getGenotypeExperimentMorpholinos(item, isWildtype, null).getPopulatedResults();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorpholinos(AnatomyItem item, Boolean isWildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct genotypeExperiment " +
                "FROM  GenotypeExperiment genotypeExperiment, Experiment exp, Genotype geno, " +
                "      AnatomyPhenotype pheno, ExperimentCondition con, Marker marker " +
                "WHERE   " +
                "      genotypeExperiment.experiment = exp AND " +
                "       (pheno.anatomySuperTerm = :aoTerm or pheno.anatomySubTerm = :aoTerm) AND " +
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
    public List<GoTerm> getGoTermsByName(String name) {
        String hql = "select distinct term from GoTerm term  " +
                "where lower(term.name) like :name " +
                " AND term.obsolete = :obsolete " ;
        // we don't order, let client take care of it
//                " order by term.name";

        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setString("name", "%" + name.toLowerCase() + "%");
        query.setBoolean("obsolete", false);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<GoTerm> getGoTermsByNameAndSubtree(String name, OntologyDTO ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GoTerm.class);
        criteria.add(Restrictions.like("name", "%" + name + "%"));
        if (ontology != null && ontology != OntologyDTO.GO)
            criteria.add(Restrictions.eq("subOntology", ontology.getOntologyName()));
        criteria.add(Restrictions.eq("obsolete", false));
        return criteria.list();
    }


    /**
     * @param name go term name
     * @return A unique GoTerm.
     */
    @SuppressWarnings("unchecked")
    public GoTerm getGoTermByName(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GoTerm.class);
        criteria.add(Restrictions.eq("name", name));
        return (GoTerm) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    /**
     * @param name go term name
     * @return A unique GoTerm.
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
                "       pheno.anatomySuperTerm = :aoTerm AND " +
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
                 " and fmrel.type=:relationship "+
                " and fmrel.marker=m "+
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
                "           and pheno.anatomySuperTerm = :unspecified " +
                "           and pheno.term.ID= :qualityZdbID " +
                "           and pheno.tag = :tag " +
                "           and figure member of pheno.figures " +
                "           and figure.zdbID = :figureID";
        AnatomyItem unspecified = RepositoryFactory.getAnatomyRepository().getAnatomyItem(AnatomyItem.UNSPECIFIED);
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


    public List<Feature> getFeaturesForStandardAttribution(Publication publication) {
        String hql = "select f from PublicationAttribution pa , Feature f " +
                " where pa.dataZdbID=f.zdbID and pa.publication.zdbID= :pubZdbID  " +
                " and pa.sourceType= :sourceType  ";
        Query query = HibernateUtil.currentSession().createQuery(hql) ;
        query.setString("pubZdbID",publication.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
    }


    /**
     * Retrieve a Goterm by obo id from the GO term table.
     *
     * @param oboID obo id
     * @return GoTerm
     */
    public GoTerm getGoTermByOboID(String oboID) {
        if (oboID == null)
            return null;
        // strip off GO: string
        oboID = oboID.substring(3);
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GoTerm.class);
        criteria.add(Restrictions.eq("oboID", oboID));
        return (GoTerm) criteria.uniqueResult();
    }


    /**
     * Go terms attributed as evidence to this marker by this pub.
     * todo: Don't include IEA, IC .
     * @param marker
     * @param publication
     * @return
     */
    public List<GoTerm> getGoTermsByMarkerAndPublication(Marker marker, Publication publication) {
        String hql = "select distinct g from PublicationAttribution pa , GoTerm g , MarkerGoTermEvidence ev " +
                " where pa.dataZdbID=ev.zdbID " +
                " and pa.publication.zdbID= :pubZdbID  " +
                " and ev.marker.zdbID = :markerZdbID " +
                " and ev.evidenceCode.code not in (:excludedEvidenceCodes) " +
                " and g.zdbID= ev.goTerm.id " +
                " and pa.sourceType= :sourceType  ";
        Query query = HibernateUtil.currentSession().createQuery(hql) ;
        query.setString("pubZdbID",publication.getZdbID());
        query.setParameterList("excludedEvidenceCodes", new String[]{GoEvidenceCodeEnum.IEA.name(), GoEvidenceCodeEnum.IC.name()} );
        query.setString("markerZdbID",marker.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
}
    
public List<Genotype> getGenotypesForStandardAttribution(Publication publication) {
        String hql = "select distinct g from PublicationAttribution pa , Genotype g " +
                " where pa.dataZdbID=g.zdbID and pa.publication.zdbID= :pubZdbID  " +
                " and pa.sourceType= :sourceType  ";
        Query query = HibernateUtil.currentSession().createQuery(hql) ;
        query.setString("pubZdbID",publication.getZdbID());
        query.setString("sourceType", PublicationAttribution.SourceType.STANDARD.toString());
        return query.list();
        }

    /**
     * Go terms attributed as evidence to this marker by this pub.
     * todo: Don't include IEA, IC .
     * @param marker
     * @param publication
     * @return
     */


    public List<GoTerm> getGoTermsByPhenotypeAndPublication(Publication publication) {
        String hql = "select distinct g from GoPhenotype p , GoTerm g  " +
                " where p.publication.zdbID= :pubZdbID  " +
                " and ( p.goSubTerm = g " +
                " or p.goSuperTerm = g )" ;
        Query query = HibernateUtil.currentSession().createQuery(hql) ;
        query.setString("pubZdbID",publication.getZdbID());
        return query.list();
    }

    public InferenceGroupMember addInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inferenceToAdd) {
        InferenceGroupMember inferenceGroupMember = new InferenceGroupMember();
        inferenceGroupMember.setMarkerGoTermEvidenceZdbID(markerGoTermEvidence.getZdbID());
        inferenceGroupMember.setInferredFrom(inferenceToAdd);
        if(markerGoTermEvidence.getInferredFrom()==null){
            markerGoTermEvidence.setInferredFrom(new HashSet<InferenceGroupMember>());
        }
        markerGoTermEvidence.getInferredFrom().add(inferenceGroupMember) ;

        HibernateUtil.currentSession().save(inferenceGroupMember);
        return inferenceGroupMember ;
    }

    public void removeInferenceToGoMarkerTermEvidence(MarkerGoTermEvidence markerGoTermEvidence, String inference) {
        for(Iterator<InferenceGroupMember> iterator = markerGoTermEvidence.getInferredFrom().iterator(); iterator.hasNext() ; ){
            if(iterator.next().getInferredFrom().equals(inference)){
                iterator.remove();
                Criteria criteria2 = HibernateUtil.currentSession().createCriteria(InferenceGroupMember.class) ;
                criteria2.add(Restrictions.eq("inferredFrom",inference)) ;
                criteria2.add(Restrictions.eq("markerGoTermEvidenceZdbID",markerGoTermEvidence.getZdbID())) ;
                InferenceGroupMember inferenceGroupMember = (InferenceGroupMember) criteria2.uniqueResult();
                HibernateUtil.currentSession().delete(inferenceGroupMember);
                return ;
            }
        }

    }


    public void invalidateCachedObjects() { }








}
