package org.zfin.mutant.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;
import org.zfin.mutant.Phenotype;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class HibernateMutantRepository implements MutantRepository {

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
     * @param item            anatomical structure
     * @param numberOfRecords number
     * @return list of statistics
     */
    public List<Morpholino> getPhenotypeMorhpolinosByAnatomy(AnatomyItem item, int numberOfRecords) {
        Session session = HibernateUtil.currentSession();

        // This returns morpholinos by phenote annotations
        StringBuilder hql = new StringBuilder("SELECT distinct marker ");
        hql.append(getMorpholinosByAnatomyTermQueryBlock());
        hql.append(" order by marker.abbreviation ");
        Query query = session.createQuery(hql.toString());
        // ToDo: MOs are not yet linked to an overview page where all all them are listed.
        //. Thus, we need to list them all here.
//        query.setMaxResults(numberOfRecords);
        query.setString("aoZdbID", item.getZdbID());
        List<Marker> markers = query.list();
        List<Morpholino> morphs = new ArrayList<Morpholino>();
        morphs.addAll(getMorpholinoRecords(markers));

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

    public void invalidateCachedObjects() {
    }
}
