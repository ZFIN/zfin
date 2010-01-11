package org.zfin.mutant.repository;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.Ontology;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.*;
import org.zfin.ontology.GoTerm;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.zfin.framework.HibernateUtil.currentSession;


/**
 *
 */
public class HibernateMutantRepository implements MutantRepository {

    public PaginationResult<Genotype> getGenotypesByAnatomyTerm(AnatomyItem item, boolean wildtype, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        String hql =
                "select distinct genox.genotype from GenotypeExperiment genox, Phenotype pheno " +
                        "WHERE pheno.genotypeExperiment = genox " +
                        "AND (pheno.patoSubTermzdbID = :zdbID or pheno.patoSuperTermzdbID = :zdbID ) " +
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
        query.setString("zdbID", item.getZdbID());
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
    public List<Morpholino> getPhenotypeMorhpolinosByAnatomy(AnatomyItem item, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        // This returns morpholinos by phenote annotations
        StringBuilder hql = new StringBuilder("SELECT distinct genotype ");
        hql.append(getMorpholinoGenotypeByAnatomyTermQueryBlock());
        hql.append(" order by genotype.name ");
        Query query = session.createQuery(hql.toString());
        // ToDo: MOs are not yet linked to an overview page where all all them are listed.
        //. Thus, we need to list them all here.
//        query.setMaxResults(numberOfRecords);
        query.setString("aoZdbID", item.getZdbID());
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
     * Retrieve the genotype objects that are assoicated to a morpholino.
     * Disregard all experiments that have non-morpholino conditions, such as chemical or physical
     * attached.
     *
     * @param item       anatomy structure
     * @param isWildtype wildtype of genotype
     * @return list of genotype object
     */
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, Boolean isWildtype, int numberOfRecords) {
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(numberOfRecords);
        return getGenotypeExperimentMorhpolinosByAnatomy(item, isWildtype, bean);
    }

    @SuppressWarnings("unchecked")
    public List<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, boolean isWildtype) {
        return getGenotypeExperimentMorhpolinosByAnatomy(item, isWildtype, null).getPopulatedResults();
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, Boolean isWildtype, PaginationBean bean) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct genotypeExperiment " +
                "FROM  GenotypeExperiment genotypeExperiment, Experiment exp, Genotype geno, " +
                "      Phenotype pheno, ExperimentCondition con, Marker marker " +
                "WHERE   " +
                "      genotypeExperiment.experiment = exp AND " +
                "       (pheno.patoSubTermzdbID = :aoZdbID or pheno.patoSuperTermzdbID = :aoZdbID) AND " +
                "       pheno.genotypeExperiment = genotypeExperiment AND " +
                "       pheno.tag != :tag AND " +
                "       con.experiment = exp AND " +
                "       genotypeExperiment.genotype = geno AND " +
                "       marker = con.morpholino AND " +
                "       geno.wildtype = :isWildtype AND " +
                "       not exists (select 1 from ExperimentCondition expCon where expCon.experiment = exp AND " +
                "                             expCon.morpholino is null ) ";
        Query query = session.createQuery(hql);
        query.setString("aoZdbID", item.getZdbID());
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
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GoTerm.class);
        criteria.add(Restrictions.like("name", "%" + name + "%"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<GoTerm> getGoTermsByNameAndSubtree(String name, Ontology ontology) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GoTerm.class);
        criteria.add(Restrictions.like("name", "%" + name + "%"));
        if (ontology != null && ontology != Ontology.GO)
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
    public List<Term> getQualityTermsByName(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Term.class);
        criteria.add(Restrictions.like("name", "%" + name + "%"));
        return criteria.list();
    }

    private List<Morpholino> getMorpholinoRecords(List<Marker> markers) {
        List<Morpholino> morphs = new ArrayList<Morpholino>();
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
                "       pheno.patoSuperTermzdbID = :aoZdbID AND " +
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

    public Marker getMarkerbyFeature(Feature feature) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct marker from Marker marker, FeatureMarkerRelationship fmrel" +
                "     where fmrel.featureZdbId = :feature" +
                "           and fmrel.marker = marker " +
                " and fmrel.type='is allele of' " +
                "    order by marker.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("feature", feature.getZdbID());


        return (Marker) query.uniqueResult();
    }


    public List<Marker> getDeletedMarker(Feature feat) {
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
    }

    public List<String> getMappedFeatureLG(Feature feat) {
        Session session = HibernateUtil.currentSession();


        String hql = "select distinct mm.lg" +
                "  from MappedMarker mm" +
                "   where mm.marker.zdbID=:ftr ";
        Query query = session.createQuery(hql);
        query.setString("ftr", feat.getZdbID());
        return (List<String>) query.list();
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
                "           and geno.nickname != :wt " +
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
     * Create a default phenotype record.
     * Default:
     * AO term: unspecified
     * Quality term: quality
     * Tag: normal
     *
     * @param pheno Phenotype
     */
    public void createDefaultPhenotype(Phenotype pheno) {
        if (pheno.getStartStage() == null)
            throw new NullPointerException("Cannot create Phenotype. No start stage provided.");
        if (pheno.getEndStage() == null)
            throw new NullPointerException("Cannot create Phenotype. No end stage provided.");
        if (pheno.getGenotypeExperiment() == null)
            throw new NullPointerException("Cannot create Phenotype. No genotype experiment provided.");


        // check if such a phenotype record already exists.
        ExpressionRepository expRepository = RepositoryFactory.getExpressionRepository();
        Phenotype existingPhenotype = expRepository.getUnspecifiedPhenotypeFromGenoxStagePub(pheno);
        //  if the phenotype exists then only add new figure to it
        if (existingPhenotype != null) {
            for (Figure figure : pheno.getFigures())
                existingPhenotype.addFigure(figure);
            return;
        }

        AnatomyRepository anatRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem unspecified = anatRep.getAnatomyItem(AnatomyItem.UNSPECIFIED);
        Term quality = getQualityTermByName(Term.QUALITY);
        pheno.setAnatomyTerm(unspecified);
        pheno.setTerm(quality);
        pheno.setTag(Phenotype.Tag.NORMAL.toString());
        Session session = HibernateUtil.currentSession();
        session.save(pheno);
    }

    /**
     * Lookup a term by name. Term must not be obsolete.
     *
     * @param termName term name
     * @return Term object
     */
    @SuppressWarnings("unchecked")
    public Term getQualityTermByName
            (String
                    termName) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(Term.class);
        crit.add(Restrictions.eq("name", termName));
        crit.add(Restrictions.eq("obsolete", false));
        return (Term) crit.uniqueResult();
    }


    public void invalidateCachedObjects() {
    }


}
