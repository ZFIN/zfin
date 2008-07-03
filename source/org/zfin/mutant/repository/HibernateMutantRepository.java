package org.zfin.mutant.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Criteria;
import static org.zfin.framework.HibernateUtil.currentSession;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.*;
import org.zfin.ontology.GoTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class HibernateMutantRepository implements MutantRepository {

    // Use this bean to set pagination parameters.
    private PaginationBean paginationBean;

    public List<Genotype> getGenotypesByAnatomyTerm(AnatomyItem item, boolean wildtype, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder();
        hql.append("select distinct geno from Genotype geno, Phenotype pheno ");
        hql.append("WHERE  pheno.genotypeExperiment member of geno.genotypeExperiments ");
        hql.append("AND (pheno.patoEntityAzdbID = :zdbID or pheno.patoEntityBzdbID = :zdbID ) ");
        hql.append("AND pheno.tag is not :tag ");
        if (!wildtype)
            hql.append("AND geno.wildtype = 'f' ");
        hql.append("ORDER BY geno.nameOrder asc");

        Query query = session.createQuery(hql.toString());
        query.setString("zdbID", item.getZdbID());
        query.setParameter("tag", Phenotype.Tag.NORMAL.toString());
        query.setFirstResult(0);
        query.setMaxResults(numberOfRecords);
        List<Genotype> genotypes = query.list();
        return genotypes;
    }

    public int getNumberOfMutants(String zdbID, boolean wildtype) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder();
        hql.append("select count(distinct geno) from Genotype geno, Phenotype pheno ");
        hql.append("where pheno.genotypeExperiment member of geno.genotypeExperiments ");
        hql.append("AND (pheno.patoEntityAzdbID = :zdbID or pheno.patoEntityBzdbID = :zdbID )");
        if (!wildtype)
            hql.append("AND geno.wildtype = 'f' ");

        Query query = session.createQuery(hql.toString());
        query.setString("zdbID", zdbID);
        return (Integer) query.uniqueResult();
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

        return (Integer) query.uniqueResult();
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

        return (Integer) query.uniqueResult();
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

        List<Genotype> genotypes = query.list();
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
     * @param item            anatomy structure
     * @param isWildtype      wildtype of genotype
     * @return list of genotype object
     */
    public List<GenotypeExperiment> getGenotypeExperimentMorhpolinosByAnatomy(AnatomyItem item, boolean isWildtype) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("SELECT distinct genotypeExperiment ");
        morpholinoExperimentsPerAnatomyTermQuery(hql);
        Query query = session.createQuery(hql.toString());
        query.setString("aoZdbID", item.getZdbID());
        query.setBoolean("isWildtype", isWildtype);
        setPaginationParameters(query);
        List<GenotypeExperiment> genotypes = query.list();

/*
        List<GenotypeExperiment> genotypes = new ArrayList<GenotypeExperiment>();
        List<Object[]> objects = query.list();
        if(objects!= null){
            for(Object[] o :objects){
                genotypes.add((GenotypeExperiment) o[0]);
            }
        }
*/
        return genotypes;
    }

    private void setPaginationParameters(Query query) {
        if (paginationBean != null) {
            query.setMaxResults(paginationBean.getMaxDisplayRecords());
            // Hibernate ecxpects a '0' for the first record.
            query.setFirstResult(paginationBean.getFirstRecord()-1);
        }
    }

    private void morpholinoExperimentsPerAnatomyTermQuery(StringBuilder hql) {
        hql.append("FROM  GenotypeExperiment genotypeExperiment, Experiment exp, Genotype geno, ");
        hql.append("      Phenotype pheno, ExperimentCondition con, Marker marker ");
        hql.append("WHERE   ");
        hql.append("      genotypeExperiment.experiment = exp AND ");
        hql.append("       (pheno.patoEntityAzdbID = :aoZdbID or pheno.patoEntityBzdbID = :aoZdbID) AND ");
        hql.append("       pheno.genotypeExperiment = genotypeExperiment AND ");
        hql.append("       con.experiment = exp AND ");
        hql.append("       genotypeExperiment.genotype = geno AND");
        hql.append("       marker = con.morpholino AND ");
        hql.append("       geno.wildtype = :isWildtype AND ");
        hql.append("       not exists (select 1 from ExperimentCondition expCon where expCon.experiment = exp AND " +
                "                             expCon.morpholino is null ) ");
    }

    public List<Morpholino> getMorpholinosByGenotype(Genotype genotype, AnatomyItem item, boolean isWildtype) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("SELECT distinct marker ");
        hql.append("FROM  Marker marker, Experiment exp, Genotype geno, ");
        hql.append("      Phenotype pheno, ExperimentCondition con, GenotypeExperiment genox ");
        hql.append("WHERE   ");
        hql.append("       genox.experiment = exp AND ");
        hql.append("       genox member of geno.genotypeExperiments AND ");
        hql.append("       pheno.genotypeExperiment = genox AND ");
        hql.append("       pheno.patoEntityAzdbID = :aoZdbID AND ");
        hql.append("       geno = :genotype AND ");
        hql.append("       con.experiment = exp AND ");
        hql.append("       geno.wildtype = :isWildtype AND ");
        hql.append("       marker = con.morpholino ");
        hql.append(" order by marker.name ");
        Query query = session.createQuery(hql.toString());
        query.setBoolean("isWildtype", isWildtype);
        query.setString("aoZdbID", item.getZdbID());
        query.setParameter("genotype", genotype);

        List<Morpholino> morpholinos = query.list();
        return morpholinos;
    }

    /**
     * Retrieve the number of Morpholino Experiments for a given anatomy term.
     *
     * @param item       AO term
     * @param isWildtype wildtyep or not
     * @return number of morpholinos
     */
    public int getNumberOfMorpholinoExperiments(AnatomyItem item, boolean isWildtype) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("SELECT count (distinct genotypeExperiment) ");
        morpholinoExperimentsPerAnatomyTermQuery(hql);
        Query query = session.createQuery(hql.toString());
        query.setString("aoZdbID", item.getZdbID());
        query.setBoolean("isWildtype", isWildtype);

        return (Integer) query.uniqueResult();
    }

    public void setPaginationParameters(PaginationBean paginationBean) {
        this.paginationBean = paginationBean;
    }

    public List<GoTerm> getGoTermsByName(String name) {
        Session session = HibernateUtil.currentSession() ;
        Criteria criteria = session.createCriteria(GoTerm.class) ;
        criteria.add(Restrictions.like("name","%"+name+"%")) ;
        return criteria.list() ;
    }

    public List<Term> getQualityTermsByName(String name) {
        Session session = HibernateUtil.currentSession() ;
        Criteria criteria = session.createCriteria(Term.class) ;
        criteria.add(Restrictions.like("name","%"+name+"%")) ;
        return criteria.list() ;
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
                "       pheno.patoEntityAzdbID = :aoZdbID AND " +
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
                "       pheno.patoEntityAzdbID = :aoZdbID AND " +
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
        return (Integer) query.uniqueResult();
    }

    /**
     * Retrieve a genotype object by PK.
     *
     * @param genoteypZbID pk
     * @return genotype
     */
    public Genotype getGenotypeByID(String genoteypZbID) {
        Session session = HibernateUtil.currentSession();
        return (Genotype) session.load(Genotype.class, genoteypZbID);
    }

    public List<Feature> getFeaturesByAbbreviation(String name) {
        List<Feature> features = new ArrayList<Feature>() ;
        Session session = currentSession();

        Criteria criteria1 = session.createCriteria(Feature.class);
        criteria1.add(Restrictions.like("abbreviation", name, MatchMode.START));
        criteria1.addOrder(Order.asc("abbreviationOrder")) ;
        features.addAll(criteria1.list()) ;

        Criteria criteria2 = session.createCriteria(Feature.class);
        criteria2.add(Restrictions.like("abbreviation", name, MatchMode.ANYWHERE));
        criteria2.add(Restrictions.not(Restrictions.like("abbreviation", name, MatchMode.START)));
        criteria2.addOrder(Order.asc("abbreviationOrder")) ;
        features.addAll(criteria2.list()) ;
        return features ;
    }


    public void invalidateCachedObjects() {
    }
}
