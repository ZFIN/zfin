package org.zfin.mutant.repository;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.stereotype.Repository;
import org.zfin.database.InformixUtil;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.PostComposedPresentationBean;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.publication.presentation.PublicationLink;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Class defines methods to retrieve phenotypic data for annotation purposes
 */
@Repository
public class HibernatePhenotypeRepository implements PhenotypeRepository {

    private static Logger LOG = Logger.getLogger(HibernatePhenotypeRepository.class);

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    @SuppressWarnings("unchecked")
    public List<PhenotypeStructure> retrievePhenotypeStructures(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select structure from PhenotypeStructure structure where structure.publication.zdbID = :pubID " +
                "       order by structure.entity.superterm.termName";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        return (List<PhenotypeStructure>) query.list();
    }

    /**
     * Create a new phenotype structure on the pile.
     * The time of creation and the security user info is added to the structure.
     *
     * @param structure Phenotype Structure
     */
    public void createPhenotypeStructure(PhenotypeStructure structure, String publicationID) {
        Publication pub = publicationRepository.getPublication(publicationID);
        structure.setPublication(pub);
        structure.setDate(new Date());
        if (isPhenotypeStructureOnPile(structure))
            return;
        structure.setPerson(ProfileService.getCurrentSecurityUser());
        Session session = HibernateUtil.currentSession();
        session.save(structure);
    }

    // NOTE: Unfortunately, Hibernate does not allow to pass a null into the setParameter() method
    // which prevents me from always assigning entity and relatedEntity in the HQL.
    // Another bug does not allow to write a criteria object that depending on null values
    // creates the where clause line items. The problem is the component setup in the mapping file!!!
    // So, fall back to this ugly HQL code!

    public boolean isPhenotypeStructureOnPile(PhenotypeStructure structure) {
        Session session = HibernateUtil.currentSession();

        String hql = "select structure from PhenotypeStructure as structure where " +
                "       structure.entity.superterm.zdbID = :entitySupertermID";
        if (structure.getEntity() != null && structure.getEntity().getSubterm() != null) {
            hql += " AND structure.entity.subterm.zdbID = :entitySubtermID";
        } else {
            hql += " AND structure.entity.subterm is null ";
        }
        if (structure.getRelatedEntity() != null && structure.getRelatedEntity().getSuperterm() != null) {
            hql += " AND structure.relatedEntity.superterm.zdbID = :relatedEntitySupertermID";
        } else {
            hql += " AND structure.relatedEntity.superterm is null ";
        }
        if (structure.getRelatedEntity() != null && structure.getRelatedEntity().getSubterm() != null) {
            hql += " AND structure.relatedEntity.subterm.zdbID = :relatedEntitySubtermID";
        } else {
            hql += " AND structure.relatedEntity.subterm is null ";
        }
        if (structure.getQualityTerm() != null) {
            hql += " AND structure.qualityTerm = :quality";
        }
        if (structure.getTag() != null) {
            hql += " AND structure.tag = :tag";
        }
        hql += " AND structure.publication = :publication";
        Query query = session.createQuery(hql);
        query.setParameter("entitySupertermID", structure.getEntity().getSuperterm().getZdbID());
        if (structure.getEntity() != null && structure.getEntity().getSubterm() != null)
            query.setParameter("entitySubtermID", structure.getEntity().getSubterm().getZdbID());
        if (structure.getRelatedEntity() != null && structure.getRelatedEntity().getSuperterm() != null)
            query.setParameter("relatedEntitySupertermID", structure.getRelatedEntity().getSuperterm().getZdbID());
        if (structure.getRelatedEntity() != null && structure.getRelatedEntity().getSubterm() != null)
            query.setParameter("relatedEntitySubtermID", structure.getRelatedEntity().getSubterm().getZdbID());
        if (structure.getQualityTerm() != null)
            query.setParameter("quality", structure.getQualityTerm());
        if (structure.getTag() != null)
            query.setParameter("tag", structure.getTag());
        query.setParameter("publication", structure.getPublication());
        List list = query.list();
        if (list == null || list.isEmpty())
            return false;
        return true;
    }

    /**
     * Check if a phenotype structure is already on the pile.
     *
     * @param phenotypeStructure PhenotypeStructure
     * @param publicationID      Publication
     * @return boolean
     */
    @SuppressWarnings({"unchecked"})
    public boolean isPhenotypePileStructureExists(PhenotypeStructure phenotypeStructure, String publicationID) {
        boolean hasEntitySubTerm = phenotypeStructure.getEntity() != null && phenotypeStructure.getEntity().getSubterm() != null;
        boolean hasRelatedEntitySuperTerm = phenotypeStructure.getRelatedEntity() != null && phenotypeStructure.getRelatedEntity().getSuperterm() != null;
        boolean hasRelatedEntitySubTerm = phenotypeStructure.getRelatedEntity() != null && phenotypeStructure.getRelatedEntity().getSubterm() != null;

        Session session = HibernateUtil.currentSession();
        String hql = "select structure from PhenotypeStructure as structure where " +
                "       structure.entity.superterm.termName = :entitySupertermName";
        if (hasEntitySubTerm)
            hql += " AND structure.entity.subterm.termName = :entitySubtermName ";
        if (hasRelatedEntitySuperTerm)
            hql += " AND structure.relatedEntity.superterm.termName = :relatedEntitySupertermName ";
        if (hasRelatedEntitySubTerm)
            hql += " AND structure.relatedEntity.subterm.termName = :relatedEntitySubtermName ";
        hql += " AND structure.qualityTerm.termName = :qualityName ";
        hql += " AND structure.publication.zdbID = :publicationID ";
        hql += " AND structure.tag = :tag ";
        Query query = session.createQuery(hql);
        query.setParameter("entitySupertermName", phenotypeStructure.getEntity().getSuperterm().getTermName());
        if (hasEntitySubTerm)
            query.setParameter("entitySubtermName", phenotypeStructure.getEntity().getSubterm().getTermName());
        if (hasRelatedEntitySuperTerm)
            query.setParameter("relatedEntitySupertermName", phenotypeStructure.getRelatedEntity().getSuperterm().getTermName());
        if (hasRelatedEntitySubTerm)
            query.setParameter("relatedEntitySubtermName", phenotypeStructure.getRelatedEntity().getSubterm().getTermName());
        query.setParameter("qualityName", phenotypeStructure.getQualityTerm().getTermName());
        query.setParameter("publicationID", publicationID);
        query.setParameter("tag", phenotypeStructure.getTag());
        List<PhenotypeStructure> structures = (List<PhenotypeStructure>) query.list();


        return false;
    }

    /**
     * Retrieve a phenotype structure by given primary key.
     *
     * @param zdbID primary key
     * @return phenotype structure
     */
    public PhenotypeStructure getPhenotypeStructureByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (PhenotypeStructure) session.load(PhenotypeStructure.class, zdbID);
    }

    /**
     * Delete a phenotype structure object given by its primary key.
     *
     * @param zdbID primary key
     */
    public void deletePhenotypeStructureById(String zdbID) {
        Session session = HibernateUtil.currentSession();
        PhenotypeStructure structure = getPhenotypeStructureByID(zdbID);
        if (structure != null)
            session.delete(structure);
    }

    /**
     * Retrieve all phenotypes for a given publication filtered by figure and fish.
     *
     * @param publicationID publication
     * @param figureID      figure
     * @param fishID        genotype
     * @param featureID     feature
     * @return list of phenotype experiment figure stage records
     */
    @SuppressWarnings("unchecked")
    public List<PhenotypeExperiment> getMutantExpressionsByFigureFish(String publicationID, String figureID, String fishID, String featureID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select phenox from PhenotypeExperiment phenox";
        if (featureID != null) {
            hql += ", GenotypeFeature genoFeature ";
        }
        hql += "       left join phenox.fishExperiment fishox ";
        if (fishID != null) {
            hql += "       join phenox.fishExperiment.fish fish";
        }
        hql += "     where phenox.figure.publication.zdbID = :pubID ";
        if (fishID != null)
            hql += "           and fish.zdbID = :fishID ";
        if (figureID != null)
            hql += "           and phenox.figure.zdbID = :figureID ";
        if (featureID != null) {
            hql += "           and fishox.fish.genotype = genoFeature.genotype ";
            hql += "           and genoFeature.feature.zdbID = :featureID ";
        }
        hql += "    order by phenox.figure.orderingLabel, " +
                "             fishox.fish.name, " +
                "             phenox.startStage.abbreviation ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        if (fishID != null)
            query.setString("fishID", fishID);
        if (figureID != null)
            query.setString("figureID", figureID);
        if (featureID != null)
            query.setString("featureID", featureID);

        return (List<PhenotypeExperiment>) query.list();
    }

    /**
     * Retrieve phenotype experiment record from the unique key
     *
     * @param phenotypeExperimentFilter phenotype experiment:
     *                                  unique figure, stage, genotype, environment
     * @return full mutant figure stage record
     */
    @SuppressWarnings({"unchecked"})
    public PhenotypeExperiment getPhenotypeExperiment(PhenotypeExperiment phenotypeExperimentFilter) {
        Session session = HibernateUtil.currentSession();

        String hql = "select phenoExperiment from PhenotypeExperiment phenoExperiment";
        hql += "       join phenoExperiment.fishExperiment.fish fish";
        hql += "       join phenoExperiment.fishExperiment.experiment environment";
        hql += "     where phenoExperiment.figure.zdbID = :figureID ";
        hql += "           and fish.zdbID = :fishID ";
        hql += "           and environment.zdbID = :environmentID ";
        hql += "           and phenoExperiment.startStage.zdbID= :startID ";
        hql += "           and phenoExperiment.endStage.zdbID= :endID ";
        Query query = session.createQuery(hql);
        query.setString("fishID", phenotypeExperimentFilter.getFishExperiment().getFish().getZdbID());
        query.setString("environmentID", phenotypeExperimentFilter.getFishExperiment().getExperiment().getZdbID());
        query.setString("figureID", phenotypeExperimentFilter.getFigure().getZdbID());
        query.setString("startID", phenotypeExperimentFilter.getStartStage().getZdbID());
        query.setString("endID", phenotypeExperimentFilter.getEndStage().getZdbID());
        List<PhenotypeExperiment> phenoExperiments = (List<PhenotypeExperiment>) query.list();

        if (phenoExperiments == null || phenoExperiments.size() == 0)
            return null;
        if (phenoExperiments.size() > 1)
            throw new RuntimeException("Found more than one phenotype experiment for given filter: " + phenotypeExperimentFilter);

        PhenotypeExperiment phenoExperiment = phenoExperiments.get(0);
        FishExperiment fishExperiment = phenoExperiment.getFishExperiment();
        phenoExperiment.setFishExperiment(fishExperiment);
        phenoExperiment.setStartStage(phenoExperiment.getStartStage());
        phenoExperiment.setEndStage(phenoExperiment.getEndStage());
        phenoExperiment.setFigure(phenoExperiment.getFigure());
        return phenoExperiment;
    }

    /**
     * Retrieve a pile phenotype structure
     *
     * @param pileStructureID primary key
     * @return PhenotypeStructure
     */
    public PhenotypeStructure getPhenotypePileStructure(String pileStructureID) {
        Session session = HibernateUtil.currentSession();
        return (PhenotypeStructure) session.get(PhenotypeStructure.class, pileStructureID);
    }

    /**
     * @param phenotypeExperiment Phenotype
     */
    public void runRegenGenotypeFigureScript(PhenotypeExperiment phenotypeExperiment) {
        InformixUtil.runInformixProcedure("regen_genofig_genotype", phenotypeExperiment.getId() + "");

        // Todo: need to refactor the following code, since some properties and the related methods are not there any more

        /*
        Set<ExperimentCondition> conditions = phenotypeExperiment.getFishExperiment().getExperiment().getSequenecTargetingReagentConditions();
        if (conditions != null) {
            for (ExperimentCondition condition : conditions) {
                SequenceTargetingReagent sequenceTargetingReagent = condition.getSequenceTargetingReagent();
                InformixUtil.runInformixProcedure("regen_genox_marker", sequenceTargetingReagent.getZdbID());
                List<Marker> targetGenes = markerRepository.getTargetGenesAsMarkerForSequenceTargetingReagent(sequenceTargetingReagent);
                for (Marker targetGene : targetGenes)
                    InformixUtil.runInformixProcedure("regen_genox_marker", targetGene.getZdbID());
            }
        }
        */
    }

    /**
     * Retrieve a list of phenotypes used for a given publication.
     *
     * @param publicationID publication
     * @return set of phenotypes
     */
    @SuppressWarnings("unchecked")
    public List<PhenotypeExperiment> getAllPhenotypes(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct experiment from PhenotypeExperiment experiment where" +
                "          experiment.figure.publication.zdbID = :publicationID";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        return (List<PhenotypeExperiment>) query.list();
    }

    /**
     * Create the Phenotype pile structure pile if it does not already exist.
     * After closing a publication the structure pile is removed and needs to be
     * re-created when re-opened so curators do not have to re-create the pile manually.
     *
     * @param publicationID publication
     */
    public void createPhenotypePile(String publicationID) {
        List<PhenotypeExperiment> phenotypes = getAllPhenotypes(publicationID);
        if (phenotypes == null || phenotypes.size() == 0)
            return;
        Publication publication = publicationRepository.getPublication(publicationID);
        for (PhenotypeExperiment phenotypeExperiments : phenotypes) {
            for (PhenotypeStatement phenotype : phenotypeExperiments.getPhenotypeStatements()) {
                PhenotypeStructure structure = instantiatePhenotypeStructureFromPheno(publication, phenotype);
                createPhenotypeStructure(structure, publicationID);
            }
        }
    }

    /**
     * Create a new phenotype statement.
     *
     * @param phenotypeStatement PhenotypeStatement
     */
    @Override
    public void createPhenotypeStatement(PhenotypeStatement phenotypeStatement) {
        // check if the default statement is present
        Session session = HibernateUtil.currentSession();
        phenotypeStatement.setDateCreated(new Date());
        session.save(phenotypeStatement);
    }

    /**
     * Delete an existing phenotype statement.
     *
     * @param phenoStatement PhenotypeStatement
     */
    @Override
    public void deletePhenotypeStatement(PhenotypeStatement phenoStatement) {
        Session session = HibernateUtil.currentSession();
        phenoStatement.setDateCreated(new Date());
        session.delete(phenoStatement);
    }

    /**
     * Create a new phenotype experiment record.
     * If no phenotype statement is found the default one is created.
     *
     * @param phenotypeExperiment PhenotypeExperiment
     */
    @Override
    public void createPhenotypeExperiment(PhenotypeExperiment phenotypeExperiment) {
        if (phenotypeExperiment == null)
            return;

        Session session = HibernateUtil.currentSession();
        phenotypeExperiment.setDateCreated(new Date());
        session.save(phenotypeExperiment);
    }

    /**
     * Retrieve phenotype experiment by id.
     *
     * @param id PK
     * @return Phenotype experiment
     */
    @Override
    public PhenotypeExperiment getPhenotypeExperiment(long id) {
        Session session = HibernateUtil.currentSession();
        return (PhenotypeExperiment) session.get(PhenotypeExperiment.class, id);
    }

    /**
     * Retrieve a list of phenotype experiment objects that do not have a phenotype statements.
     *
     * @param publicationID pub id
     * @return list of phenotype experiments
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public List<PhenotypeExperiment> getPhenotypeExperimentsWithoutAnnotation(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct experiment from PhenotypeExperiment experiment where" +
                "          experiment.figure.publication.zdbID = :publicationID " +
                "          and experiment.phenotypeStatements is empty ";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        return (List<PhenotypeExperiment>) query.list();
    }

    /**
     * Retrieve all phenotype experiments that have been created in the last n days.
     *
     * @return list of phenotype experiments
     */
    @Override
    public List<PhenotypeExperiment> getLatestPhenotypeExperiments(int days) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct experiment from PhenotypeExperiment experiment where " +
                "          experiment.dateCreated > :days ";
        Query query = session.createQuery(hql);
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -days);
        query.setDate("days", cal.getTime());
        return (List<PhenotypeExperiment>) query.list();
    }

    /**
     * Retrieve all phenotype statements that have been created in the last n days.
     *
     * @param experimentID phenotype Experiment ID
     * @param days         in the last days
     * @return list of phenotype statements
     */
    @Override
    public List<PhenotypeStatement> getLatestPhenotypeStatements(int experimentID, int days) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct statement from PhenotypeStatement statement where " +
                "          statement.dateCreated > :days ";
        if (experimentID > 0)
            hql += "AND statement.phenotypeExperiment.id =  :experimentID order by statement.dateCreated desc";
        hql += " order by statement.dateCreated desc";
        Query query = session.createQuery(hql);
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, -days);
        query.setDate("days", cal.getTime());
        if (experimentID > 0)
            query.setInteger("experimentID", experimentID);
        return (List<PhenotypeStatement>) query.list();
    }

    private PhenotypeStructure instantiatePhenotypeStructureFromPheno(Publication publication, PhenotypeStatement phenotype) {
        PhenotypeStructure structure = new PhenotypeStructure();
        structure.setDate(new Date());
        structure.setPerson(ProfileService.getCurrentSecurityUser());
        structure.setPublication(publication);
        structure.setEntity(phenotype.getEntity());
        structure.setRelatedEntity(phenotype.getRelatedEntity());
        structure.setQualityTerm(phenotype.getQuality());
        structure.setTag(PhenotypeStatement.Tag.getTagFromName(phenotype.getTag()));
        return structure;
    }

    @Override
    public int getNumPhenotypeFigures(Marker gene) {
        String sql = " select count(*) from ( " +
                "     select distinct phenox_fig_zdb_id  " +
                "      from phenotype_experiment, mutant_fast_search  " +
                "     where mfs_mrkr_zdb_id =  :markerZdbId " +
                "       and phenox_genox_zdb_id = mfs_genox_zdb_id  " +
                "       and exists (select NOTnormal.phenos_pk_id  " +
                "                     from phenotype_statement NOTnormal  " +
                "                    where NOTnormal.phenos_phenox_pk_id = phenox_pk_id  " +
                "                      and NOTnormal.phenos_tag != \"normal\") " +
                " ) " +
                " ";
        return Integer.parseInt(HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", gene.getZdbID())
                .uniqueResult().toString());
    }

    @Override
    public FigureLink getPhenotypeFirstFigure(Marker gene) {
        String sql = "  " +
                "     select " +
                "phenox_fig_zdb_id, f.fig_label " +
                "from phenotype_experiment, mutant_fast_search, figure f " +
                "where mfs_mrkr_zdb_id = :markerZdbId " +
                "and f.fig_zdb_id= phenox_fig_zdb_id " +
                "and phenox_genox_zdb_id = mfs_genox_zdb_id " +
                "and exists " +
                "( " +
                "   select " +
                "   NOTnormal.phenos_pk_id " +
                "   from phenotype_statement NOTnormal " +
                "   where NOTnormal.phenos_phenox_pk_id = phenox_pk_id " +
                "   and NOTnormal.phenos_tag != \"normal\" " +
                ") ";
        return (FigureLink) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", gene.getZdbID())
                .setMaxResults(1)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        FigureLink figureLink = new FigureLink();
                        figureLink.setFigureZdbId(tuple[0].toString());
                        if (tuple[1] == null) {
                            figureLink.setLinkContent("Text only");
                        } else {
                            figureLink.setLinkContent(tuple[1].toString());
                        }
                        figureLink.setLinkValue(
                                FigurePresentation.getLink(figureLink.getFigureZdbId(), figureLink.getLinkContent())
                        );

                        return figureLink;
                    }
                })
                .uniqueResult();
    }

    /**
     * Retrieve all phenotype statements for a given figure.
     *
     * @param figure figure
     * @return list of phenotype statements
     */
    public List<PhenotypeStatement> getPhenotypeStatements(Figure figure) {
        if (figure == null || figure.getZdbID() == null)
            return null;

        Session session = HibernateUtil.currentSession();
        String sql = "from PhenotypeStatement where " +
                " phenotypeExperiment.figure = :figure";

        Query query = session.createQuery(sql);
        query.setParameter("figure", figure);
        return query.list();
    }

    /**
     * Retrieve all phenotypes for a given genotype experiment.
     *
     * @param genoxID genotype experiment id
     * @return list of phenotypes
     */
    public List<PhenotypeStatement> getPhenotypeStatements(FishExperiment genoxID) {
        if (genoxID == null)
            return null;

        Session session = HibernateUtil.currentSession();
        String sql = "from PhenotypeStatement where " +
                " phenotypeExperiment.fishExperiment = :genoxID";

        Query query = session.createQuery(sql);
        query.setParameter("genoxID", genoxID);
        return query.list();
    }


    /**
     * Here we assume that there is only one.
     *
     * @param gene
     * @return
     */
    @Override
    public PublicationLink getPhenotypeFirstPublication(Marker gene) {
        String sql = " " +
                "   select p.zdb_id , p.pub_mini_ref " +
                "        from phenotype_experiment, figure, publication p, mutant_fast_search  " +
                "       where mfs_mrkr_zdb_id = :markerZdbId  " +
                "         and mfs_genox_zdb_id = phenox_genox_zdb_id  " +
                "         and phenox_fig_zdb_id = fig_zdb_id  " +
                "         and fig_source_zdb_id = zdb_id  " +
                "         and exists (select NOTnormal.phenos_pk_id  " +
                "                     from phenotype_statement NOTnormal  " +
                "                    where NOTnormal.phenos_phenox_pk_id = phenox_pk_id  " +
                "                      and NOTnormal.phenos_tag != \"normal\")     " +
                "        order by p.pub_date asc " +
                "  " +
                " ";
        return (PublicationLink) HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", gene.getZdbID())
                .setMaxResults(1)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        PublicationLink publicationLink = new PublicationLink();
                        publicationLink.setPublicationZdbId(tuple[0].toString());
                        publicationLink.setLinkContent(tuple[1].toString());
                        return publicationLink;
                    }
                })
                .uniqueResult();
    }

    @Override
    public int getNumPhenotypePublications(Marker gene) {
        String sql = " select count(*) from ( " +
                "   select distinct pub_mini_ref, zdb_id  " +
                "        from phenotype_experiment, figure, publication, mutant_fast_search  " +
                "       where mfs_mrkr_zdb_id = :markerZdbId  " +
                "         and mfs_genox_zdb_id = phenox_genox_zdb_id  " +
                "         and phenox_fig_zdb_id = fig_zdb_id  " +
                "         and fig_source_zdb_id = zdb_id  " +
                "         and exists (select NOTnormal.phenos_pk_id  " +
                "                     from phenotype_statement NOTnormal  " +
                "                    where NOTnormal.phenos_phenox_pk_id = phenox_pk_id  " +
                "                      and NOTnormal.phenos_tag != \"normal\")     " +
                " ) " +
                " ";
        return Integer.parseInt(HibernateUtil.currentSession().createSQLQuery(sql)
                .setString("markerZdbId", gene.getZdbID())
                .uniqueResult().toString());
    }

    private class PostComposedResultTransformer extends BasicTransformerAdapter {
        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            PostComposedPresentationBean bean = new PostComposedPresentationBean();
            bean.setSuperTermZdbId(tuple[0].toString());
            bean.setSuperOntologyId(tuple[1].toString());
            if (tuple[2] != null) {
                bean.setSubTermZdbId(tuple[2].toString());
                bean.setSubOntologyId(tuple[3].toString());
                bean.setSubTermName(tuple[5].toString());
            }
            bean.setSuperTermName(tuple[4].toString());
            return bean;
        }

    }

    @Override
    public List<PostComposedPresentationBean> getPhenotypeAnatomy(Marker gene) {

        PostComposedResultTransformer postComposedResultTransformer = new PostComposedResultTransformer();

        Set<PostComposedPresentationBean> phenotypes = new TreeSet<PostComposedPresentationBean>();

        // NOTE: need to use the "AS" notation, otherwise it assumes that both are the same
        String sql1 = " select distinct super.term_zdb_id as sup_zdbid, super.term_ont_id as sup_ontid, " +
                " sub.term_zdb_id as sub_zdbid, sub.term_ont_id as sub_ontid, " +
                " super.term_name as super_name , sub.term_name as sub_name " +
                "               from phenotype_statement ps  " +
                " join term super on ps.phenos_entity_1_superterm_zdb_id = super.term_zdb_id   " +
                " left OUTER join term sub on ps.phenos_entity_1_subterm_zdb_id = sub.term_zdb_id " +
                "	  where ps.phenos_tag != 'normal' " +
                "     and ps.phenos_phenox_pk_id in ( " +
                "           select px.phenox_pk_id " +
                "             from " +
                "                mutant_fast_search mfs , " +
                "                phenotype_experiment px               " +
                "            where " +
                "                mfs.mfs_mrkr_Zdb_id = :markerZdbId               " +
                "                and mfs.mfs_genox_zdb_id = px.phenox_genox_zdb_id     " +
                "     ) " +
                " ";
        phenotypes.addAll(HibernateUtil.currentSession().createSQLQuery(sql1)
                .setString("markerZdbId", gene.getZdbID())
                .setResultTransformer(postComposedResultTransformer)
                .list());


        String sql2 = " select distinct super.term_zdb_id as sup_zdbid, super.term_ont_id as sup_ontid, " +
                " sub.term_zdb_id as sub_zdbid, sub.term_ont_id as sub_ontid, " +
                " super.term_name as super_name , sub.term_name as sub_name " +
                "               from  phenotype_statement ps  " +
                "              join term super on ps.phenos_entity_2_superterm_zdb_id = super.term_zdb_id " +
                "              left OUTER join term sub  on ps.phenos_entity_2_subterm_zdb_id = sub.term_zdb_id " +
                "     where ps.phenos_tag != 'normal'   " +
                "     and ps.phenos_phenox_pk_id in ( " +
                "           select px.phenox_pk_id " +
                "             from " +
                "                mutant_fast_search mfs , " +
                "                phenotype_experiment px               " +
                "            where " +
                "                mfs.mfs_mrkr_Zdb_id = :markerZdbId               " +
                "                and mfs.mfs_genox_zdb_id = px.phenox_genox_zdb_id     " +
                "     ) " +
                " ";

        phenotypes.addAll(HibernateUtil.currentSession().createSQLQuery(sql2)
                .setString("markerZdbId", gene.getZdbID())
                .setResultTransformer(postComposedResultTransformer)
                .list());

        return new ArrayList<PostComposedPresentationBean>(phenotypes);
    }


    @Override
    public List<Figure> getPhenoFiguresByGenotype(String genotypeID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fig from Figure fig, GenotypeFigure genoFig " +
                "      where genoFig.genotype.zdbID = :genoID " +
                "        and genoFig.figure = fig";

        Query query = session.createQuery(hql);
        query.setString("genoID", genotypeID);
        return (List<Figure>) query.list();
    }

    /**
     * Retrieve phenotype statement for a given figure and fish.
     * @param figure figure
     * @param fishID fish ID
     * @return list of phenotype statements
     */
    public List<PhenotypeStatement> getPhenotypeStatements(Figure figure, String fishID) {
        Fish fish=getMutantRepository().getFish(fishID);
        Set<FishExperiment> fishOx = getMutantRepository().getFish(fishID).getFishExperiments();
        List<String>fishoxID=new ArrayList<>(fishOx.size());
        for (FishExperiment genoID : getMutantRepository().getFish(fishID).getFishExperiments()) {
            fishoxID.add(genoID.getZdbID());
        }
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct pheno from PhenotypeStatement pheno  " +
                "      where pheno.phenotypeExperiment.figure = :figure " +
                "        and pheno.phenotypeExperiment.genotypeExperiment.zdbID in (:genoxIDs)";

        Query query = session.createQuery(hql);
        query.setParameter("figure", figure);
        query.setParameterList("genoxIDs", fishoxID);
        return (List<PhenotypeStatement>) query.list();
    }

    /**
     * Retrieve phenotype figures for a given genotype.
     * @param genotype genotype
     * @return list of figures
     */
    public List<Figure> getPhenotypeFiguresForGenotype(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct genofig.figure from GenotypeFigure genofig " +
                "where genofig.genotype = :genotype";

        Query query = session.createQuery(hql);
        query.setParameter("genotype", genotype);

        return (List<Figure>) query.list();
    }

    /**
     * Retrieve phenotype figures for a given genotype.
     * @param fish fish
     * @return list of figures
     */
    public List<Figure> getPhenotypeFiguresForFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct genofig.figure from GenotypeFigure genofig " +
                "where genofig.fish = :fish";

        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);

        return (List<Figure>) query.list();
    }

    /**
     * Retrieve phenotype statement for a given figure and genotype.
     * @param figure figure
     * @param genotype genotype
     * @return list of phenotype statements
     */
    public List<PhenotypeStatement> getPhenotypeStatementsForFigureAndGenotype(Figure figure, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct pheno from PhenotypeStatement pheno, PhenotypeExperiment phenoExp, GenotypeFigure genoFig  " +
                "      where pheno.phenotypeExperiment = phenoExp " +
                "        and genoFig.phenotypeExperiment = phenoExp " +
                "        and genoFig.genotype = :genotype " +
                "        and genoFig.figure = :figure";

        Query query = session.createQuery(hql);
        query.setParameter("figure", figure);
        query.setParameter("genotype", genotype);
        return (List<PhenotypeStatement>) query.list();
    }

    public List<PhenotypeStatement> getPhenotypeStatementsForFigureAndFish(Figure figure, Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct pheno from PhenotypeStatement pheno, PhenotypeExperiment phenoExp, GenotypeFigure genoFig  " +
                "      where pheno.phenotypeExperiment = phenoExp " +
                "        and genoFig.phenotypeExperiment = phenoExp " +
                "        and genoFig.fish = :fish" +
                "        and genoFig.figure = :figure";

        Query query = session.createQuery(hql);
        query.setParameter("figure", figure);
        query.setParameter("fish", fish);
        return (List<PhenotypeStatement>) query.list();
    }

    @Override
    public List<GenericTerm> getHumanDiseases(String publicationID) {

        String hql = "select term from TermAttribution as termAtt where " +
                "       termAtt.publication.zdbID = :publicationID " +
                "     order by termAtt.term.termName";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("publicationID", publicationID);

        return (List<GenericTerm>) query.list();
    }

    @Override
    public List<DiseaseModel> getHumanDiseaseModels(String publicationID) {
        String hql = "from DiseaseModel as disease where" +
                " disease.publication.zdbID = :publicationID " +
                "order by disease.disease.termName";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("publicationID", publicationID);
        return (List<DiseaseModel>) query.list();
    }

    @Override
    public List<DiseaseModel> getHumanDiseaseModelsByFish(String fishID) {
        String hql = "from DiseaseModel as disease where" +
                " disease.fishExperiment.fish.zdbID = :fishID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fishID", fishID);
        return (List<DiseaseModel>) query.list();
    }

    @Override
    public List<DiseaseModel> getHumanDiseaseModels(GenericTerm disease) {
        String hql = "from DiseaseModel where" +
                " disease = :disease";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("disease", disease);
        return (List<DiseaseModel>) query.list();
    }
}
