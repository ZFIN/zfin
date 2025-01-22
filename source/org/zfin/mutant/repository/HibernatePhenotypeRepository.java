package org.zfin.mutant.repository;

import jakarta.persistence.TemporalType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.zfin.database.InformixUtil;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.PostComposedPresentationBean;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.HumanGeneDetail;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.publication.presentation.PublicationLink;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.database.HibernateUpgradeHelper.setTupleResultTransformer;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Class defines methods to retrieve phenotypic data for annotation purposes
 */
@Repository
public class HibernatePhenotypeRepository implements PhenotypeRepository {

    private static Logger LOG = LogManager.getLogger(HibernatePhenotypeRepository.class);

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    @SuppressWarnings("unchecked")
    public boolean hasPhenotypeStructures(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(structure) from PhenotypeStructure structure " +
                     " where structure.publication.zdbID = :pubID ";
        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);
        Long count = (Long) query.uniqueResult();
        return count > 0;
    }

    public List<PhenotypeStructure> retrievePhenotypeStructures(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select structure from PhenotypeStructure structure " +
                     "       join fetch structure.qualityTerm " +
                     "       join fetch structure.entity.superterm " +
                     "       left outer join fetch structure.entity.subterm " +
                     " where structure.publication.zdbID = :pubID " +
                     "       order by structure.entity.superterm.termName";
        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);
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

    public boolean hasMutantExpressions(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(phenox) from PhenotypeExperiment phenox";
        hql += "     where phenox.figure.publication.zdbID = :pubID ";
        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);

        Long count = (Long) query.uniqueResult();
        return count > 0;
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

        String hql = "select phenox, phenox.fishExperiment.fish from PhenotypeExperiment phenox";
        if (featureID != null) {
            hql += ", GenotypeFeature genoFeature ";
        }
        hql += "       join fetch phenox.startStage ";
        hql += "       join fetch phenox.endStage ";
        hql += "       join fetch phenox.figure ";
        hql += "       left join fetch phenox.phenotypeStatements ";
        hql += "       left join fetch phenox.fishExperiment fishox";
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
               "             phenox.fishExperiment.fish.handle, " +
               "             phenox.startStage.abbreviation ";
        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationID);
        if (fishID != null)
            query.setParameter("fishID", fishID);
        if (figureID != null)
            query.setParameter("figureID", figureID);
        if (featureID != null)
            query.setParameter("featureID", featureID);

        List<Object[]> list = query.list();
        Set<PhenotypeExperiment> phenoSet = new LinkedHashSet<>();
        for (Object[] object : list) {
            phenoSet.add((PhenotypeExperiment) object[0]);
        }
        return new ArrayList<>(phenoSet);
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
        query.setParameter("fishID", phenotypeExperimentFilter.getFishExperiment().getFish().getZdbID());
        query.setParameter("environmentID", phenotypeExperimentFilter.getFishExperiment().getExperiment().getZdbID());
        query.setParameter("figureID", phenotypeExperimentFilter.getFigure().getZdbID());
        query.setParameter("startID", phenotypeExperimentFilter.getStartStage().getZdbID());
        query.setParameter("endID", phenotypeExperimentFilter.getEndStage().getZdbID());
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
        InformixUtil.runProcedure("regen_genofig_phenox", phenotypeExperiment.getId() + "");
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

    public List<PhenotypeExperiment> getPhenoByExperimentID(String experimentID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct experiment from PhenotypeExperiment experiment where" +
                     "          experiment.fishExperiment.experiment.zdbID = :experimentID";
        Query query = session.createQuery(hql);
        query.setParameter("experimentID", experimentID);
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
        String hql = """
                    SELECT DISTINCT experiment FROM PhenotypeExperiment experiment
                    WHERE experiment.dateCreated > :startDate
                    """;
        Query query = session.createQuery(hql);
        query.setParameter("startDate", DateUtils.addDays(new Date(), -days), TemporalType.DATE);
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
        String hql = """
                    SELECT DISTINCT statement FROM PhenotypeStatement statement
                    WHERE statement.dateCreated > :startDate
                    AND (:experimentID = 0 OR statement.phenotypeExperiment.id = :experimentID)
                    ORDER BY statement.dateCreated DESC
                    """;
        Query query = session.createQuery(hql);
        query.setParameter("startDate", DateUtils.addDays(new Date(), -days), TemporalType.DATE);
        query.setParameter("experimentID", experimentID);
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
                     "     select distinct pg_fig_zdb_id  " +
                     "      from phenotype_source_generated, mutant_fast_search  " +
                     "     where mfs_data_zdb_id =  :markerZdbId " +
                     "       and pg_genox_zdb_id = mfs_genox_zdb_id  " +
                     "       and exists (select NOTnormal.psg_id  " +
                     "                     from phenotype_observation_generated NOTnormal  " +
                     "                    where NOTnormal.psg_pg_id = pg_id  " +
                     "                      and NOTnormal.psg_tag != 'normal') " +
                     " ) countingTable" +
                     " ";
        return Integer.parseInt(HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("markerZdbId", gene.getZdbID())
            .uniqueResult().toString());
    }

    @Override
    public FigureLink getPhenotypeFirstFigure(Marker gene) {
        String sql = """  
                         select 
                    pg_fig_zdb_id, f.fig_label 
                    from phenotype_source_generated, mutant_fast_search, figure f 
                    where mfs_data_zdb_id = :markerZdbId 
                    and f.fig_zdb_id= pg_fig_zdb_id 
                    and pg_genox_zdb_id = mfs_genox_zdb_id 
                    and exists 
                    ( 
                       select 
                       NOTnormal.psg_id 
                       from phenotype_observation_generated NOTnormal 
                       where NOTnormal.psg_pg_id = pg_id 
                       and NOTnormal.psg_tag != 'normal' 
                    )
                    """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql)
                .setParameter("markerZdbId", gene.getZdbID())
                .setMaxResults(1);

        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> {
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
                });
        return (FigureLink) query.uniqueResult();
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
        String sql ="""
                        select p.zdb_id , p.pub_mini_ref 
                             from phenotype_source_generated, figure, publication p, mutant_fast_search  
                            where mfs_data_zdb_id = :markerZdbId  
                              and mfs_genox_zdb_id = pg_genox_zdb_id  
                              and pg_fig_zdb_id = fig_zdb_id  
                              and fig_source_zdb_id = zdb_id  
                              and exists (select NOTnormal.psg_id  
                                          from phenotype_observation_generated NOTnormal  
                                         where NOTnormal.psg_pg_id = pg_id  
                                           and NOTnormal.psg_tag != 'normal')     
                             order by p.pub_date asc 
                     """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql)
                .setParameter("markerZdbId", gene.getZdbID())
                .setMaxResults(1);
        return (PublicationLink) setTupleResultTransformer(query,
                (Object[] tuple, String[] aliases) -> {
                    PublicationLink publicationLink = new PublicationLink();
                    publicationLink.setPublicationZdbId(tuple[0].toString());
                    publicationLink.setLinkContent(tuple[1].toString());
                    return publicationLink;
                }).uniqueResult();
    }

    @Override
    public int getNumPhenotypePublications(Marker gene) {
        String sql = """
                      select count(*) from ( 
                        select distinct pub_mini_ref, zdb_id  
                             from phenotype_source_generated, figure, publication, mutant_fast_search  
                            where mfs_data_zdb_id = :markerZdbId  
                              and mfs_genox_zdb_id = pg_genox_zdb_id  
                              and pg_fig_zdb_id = fig_zdb_id  
                              and fig_source_zdb_id = zdb_id  
                              and exists (select NOTnormal.psg_id  
                                          from phenotype_observation_generated NOTnormal  
                                         where NOTnormal.psg_pg_id = pg_id  
                                           and NOTnormal.psg_tag != 'normal')     
                      ) countingTable
                     """;
        return Integer.parseInt(HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("markerZdbId", gene.getZdbID())
            .uniqueResult().toString());
    }

    private class PostComposedResultTransformer  {
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
    public PhenotypeWarehouse getPhenotypeWarehouseBySourceID(String psgID) {

        String[] psgAttributes = psgID.split("\\|");

        String id = psgAttributes[0];
        String figureZdbID = psgAttributes[1];
        String startStageZdbID = psgAttributes[2];
        String endStageZdbID = psgAttributes[3];

        Session session = HibernateUtil.currentSession();

        String hql = """
                     select pgcm.phenoWarehouse from PhenotypeCurationSearch pgcm
                      where pgcm.phenoOrExpID=:psgID  
                                       and pgcm.phenoWarehouse.figure.zdbID=:figureZdbID 
                                       and pgcm.phenoWarehouse.start.zdbID=:startStageZdbID
                                       and pgcm.phenoWarehouse.end.zdbID=:endStageZdbID
                     """;

        Query<PhenotypeWarehouse> query = session.createQuery(hql, PhenotypeWarehouse.class);
        query.setParameter("psgID", id);
        query.setParameter("figureZdbID", figureZdbID);
        query.setParameter("startStageZdbID", startStageZdbID);
        query.setParameter("endStageZdbID", endStageZdbID);
        return query.uniqueResult();


    }

    public List<PostComposedPresentationBean> getPhenotypeAnatomy(Marker gene) {

        PostComposedResultTransformer postComposedResultTransformer = new PostComposedResultTransformer();

        Set<PostComposedPresentationBean> phenotypes = new TreeSet<PostComposedPresentationBean>();

        // NOTE: need to use the "AS" notation, otherwise it assumes that both are the same
        String sql1 = """
                        select distinct super.term_zdb_id as sup_zdbid, super.term_ont_id as sup_ontid,
                        sub.term_zdb_id as sub_zdbid, sub.term_ont_id as sub_ontid,
                        super.term_name as super_name , sub.term_name as sub_name
                                     from phenotype_observation_generated pog 
                        join term super on pog.psg_e1a_zdb_id = super.term_zdb_id  
                        left OUTER join term sub on pog.psg_e1b_zdb_id = sub.term_zdb_id
                          where pog.psg_tag != 'normal'
                           and pog.psg_pg_id in (
                                 select psg.pg_id
                                   from
                                      mutant_fast_search mfs ,
                                      phenotype_source_generated psg              
                                  where
                                      mfs.mfs_data_Zdb_id = :markerZdbId              
                                      and mfs.mfs_genox_zdb_id = psg.pg_genox_zdb_id    
                           )
                """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql1)
                .setParameter("markerZdbId", gene.getZdbID());
        setTupleResultTransformer(query, postComposedResultTransformer::transformTuple);
        phenotypes.addAll(query.list());


        String sql2 = """ 
                       select distinct super.term_zdb_id as sup_zdbid, super.term_ont_id as sup_ontid,
                       sub.term_zdb_id as sub_zdbid, sub.term_ont_id as sub_ontid,
                       super.term_name as super_name , sub.term_name as sub_name
                                     from  phenotype_observation_generated pog  
                                    join term super on pog.psg_e2a_zdb_id = super.term_zdb_id
                                    left OUTER join term sub  on pog.psg_e2b_zdb_id = sub.term_zdb_id
                           where pog.psg_tag != 'normal'  
                           and pog.psg_pg_id in (
                                 select psg.pg_id
                                   from
                                      mutant_fast_search mfs ,
                                      phenotype_source_generated psg              
                                  where
                                      mfs.mfs_data_Zdb_id = :markerZdbId              
                                      and mfs.mfs_genox_zdb_id = psg.pg_genox_zdb_id   
                           )
                      """;
        Query query2 = HibernateUtil.currentSession().createNativeQuery(sql2)
                .setParameter("markerZdbId", gene.getZdbID());
        setTupleResultTransformer(query2, postComposedResultTransformer::transformTuple);
        phenotypes.addAll(query2.list());

        return new ArrayList<PostComposedPresentationBean>(phenotypes);
    }


    @Override
    public List<Figure> getPhenoFiguresByGenotype(String genotypeID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fig from Figure fig, GenotypeFigure genoFig " +
                     "      where genoFig.genotype.zdbID = :genoID " +
                     "        and genoFig.figure = fig";

        Query query = session.createQuery(hql);
        query.setParameter("genoID", genotypeID);
        return (List<Figure>) query.list();
    }

    /**
     * Retrieve phenotype statement for a given figure and fish.
     *
     * @param figure figure
     * @param fishID fish ID
     * @return list of phenotype statements
     */
    public List<PhenotypeStatementWarehouse> getPhenotypeStatements(Figure figure, String fishID) {
        Fish fish = getMutantRepository().getFish(fishID);

        if (fish == null) {
            return null;
        }

        List<String> fishoxID = new ArrayList<>();
        for (FishExperiment genoID : fish.getFishExperiments()) {
            fishoxID.add(genoID.getZdbID());
        }
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno  " +
                     "      where pheno.phenotypeWarehouse.figure = :figure " +
                     "        and pheno.phenotypeWarehouse.fishExperiment.zdbID in (:genoxIDs)";

        Query query = session.createQuery(hql);
        query.setParameter("figure", figure);
        query.setParameterList("genoxIDs", fishoxID);
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    /**
     * Retrieve phenotype figures for a given genotype.
     *
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
     *
     * @param fish fish
     * @return list of figures
     */
    public List<Figure> getPhenotypeFiguresForFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct genofig.figure from GenotypeFigure genofig where genofig.fish = :fish";

        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);

        return (List<Figure>) query.list();
    }

    /**
     * Retrieve phenotype statement for a given figure and genotype.
     *
     * @param figure   figure
     * @param genotype genotype
     * @return list of phenotype statements
     */
    public List<PhenotypeStatementWarehouse> getPhenotypeStatementsForFigureAndGenotype(Figure figure, Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql ="""
                      select distinct pheno from PhenotypeStatementWarehouse pheno, GenotypeFigure genoFig  
                           where genoFig.phenotypeWarehouse = pheno.phenotypeWarehouse 
                             and genoFig.genotype = :genotype 
                             and genoFig.figure = :figure
                     """;

        Query query = session.createQuery(hql);
        query.setParameter("figure", figure);
        query.setParameter("genotype", genotype);
        return (List<PhenotypeStatementWarehouse>) query.list();
    }

    public List<PhenotypeStatement> getPhenotypeStatementsForFigureAndFish(Figure figure, Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct pheno from PhenotypeStatement pheno, PhenotypeExperiment phenoExp, GenotypeFigure genoFig  
                           where pheno.phenotypeExperiment = phenoExp 
                             and genoFig.phenotypeExperiment = phenoExp 
                             and genoFig.fish = :fish
                             and genoFig.figure = :figure
                     """;

        Query<PhenotypeStatement> query = session.createQuery(hql, PhenotypeStatement.class);
        query.setParameter("figure", figure);
        query.setParameter("fish", fish);
        return query.list();
    }

    @Override
    public List<GenericTerm> getHumanDiseases(String publicationID) {

        String hql = """
                     select term from TermAttribution as termAtt where 
                            termAtt.publication.zdbID = :publicationID 
                          order by termAtt.term.termName
                     """;
        Query<GenericTerm> query = HibernateUtil.currentSession().createQuery(hql, GenericTerm.class);
        query.setParameter("publicationID", publicationID);

        return query.list();
    }

    @Override
    public List<DiseaseAnnotation> getHumanDiseaseModels(String publicationID) {
        String hql = """
                     from DiseaseAnnotation as disease where
                      disease.publication.zdbID = :publicationID 
                     order by disease.disease.termName
                     """;
        Query<DiseaseAnnotation> query = HibernateUtil.currentSession().createQuery(hql, DiseaseAnnotation.class);
        query.setParameter("publicationID", publicationID);
        return query.list();
    }

    @Override
    public List<DiseaseAnnotationModel> getHumanDiseaseAnnotationModels(String publicationID) {
        String hql = """
            from DiseaseAnnotation as model where
             model.publication.zdbID = :publicationID
            order by model.disease.termName
            """;
        Query<DiseaseAnnotation> query = HibernateUtil.currentSession().createQuery(hql, DiseaseAnnotation.class);
        query.setParameter("publicationID", publicationID);
        List<DiseaseAnnotation> list = query.list();

        return list.stream()
            .map(diseaseAnnotation -> {
                if (CollectionUtils.isEmpty(diseaseAnnotation.getDiseaseAnnotationModel())) {
                    DiseaseAnnotationModel model = new DiseaseAnnotationModel();
                    model.setDiseaseAnnotation(diseaseAnnotation);
                    return List.of(model);
                }
                return diseaseAnnotation.getDiseaseAnnotationModel();
            }).flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public List<DiseaseAnnotationModel> getAllHumanDiseaseAnnotationModels() {
        String hql = """
            from DiseaseAnnotation as model
            order by model.disease.termName
            """;
        Query<DiseaseAnnotation> query = HibernateUtil.currentSession().createQuery(hql, DiseaseAnnotation.class);
        //query.setParameter("publicationID", publicationID);
        List<DiseaseAnnotation> list = query.list();

        return list.stream()
            .map(diseaseAnnotation -> {
                if (CollectionUtils.isEmpty(diseaseAnnotation.getDiseaseAnnotationModel())) {
                    DiseaseAnnotationModel model = new DiseaseAnnotationModel();
                    model.setDiseaseAnnotation(diseaseAnnotation);
                    return List.of(model);
                }
                return diseaseAnnotation.getDiseaseAnnotationModel();
            }).flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public List<DiseaseAnnotationModel> getHumanDiseaseModelsByFish(String fishID) {
        String hql = "from DiseaseAnnotationModel as disease where" +
                     " disease.fishExperiment.fish.zdbID = :fishID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fishID", fishID);
        return (List<DiseaseAnnotationModel>) query.list();
    }

    public List<DiseaseAnnotationModel> getHumanDiseaseModelsByExperiment(String exptID) {
        String hql = "from DiseaseAnnotationModel as disease where" +
                     " disease.fishExperiment.experiment.zdbID = :exptID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("exptID", exptID);
        return (List<DiseaseAnnotationModel>) query.list();
    }

    @Override
    public List<DiseaseAnnotationModel> getHumanDiseaseModels(GenericTerm disease, boolean includeChildren, Pagination pagination) {
        return getHumanDiseaseModels(disease, null, includeChildren, pagination);
    }

    @Override
    public List<DiseaseAnnotationModel> getHumanDiseaseModelsByFish(Fish fish, Pagination pagination) {
        return null;
    }

    public List<DiseaseAnnotationModel> getHumanDiseaseModels(GenericTerm disease, Fish fish, boolean includeChildren, Pagination pagination) {
        String hql = """
            select damo  from DiseaseAnnotationModel damo
            left join fetch damo.fishExperiment fx
            left join fetch fx.experiment exp
            left join fetch fx.fish
            left join fetch exp.experimentConditions conditions
            left join fetch damo.diseaseAnnotation da
            left join fetch da.disease term
            left join fetch da.publication
            left join fetch conditions.zecoTerm zecoTerm
            left join fetch conditions.chebiTerm chebiTerm
            left join fetch conditions.aoTerm aoTerm
            left join fetch conditions.spatialTerm spatialTerm
            left join fetch conditions.goCCTerm goCCTerm
            left join fetch conditions.taxaonymTerm taxonomyTerm
            """;
        if (disease != null || fish != null)
            hql += " WHERE ";
        if (disease != null) {
            hql += " da.disease = :disease ";
        }

        if (includeChildren) {
            hql = """
                select damo  from DiseaseAnnotationModel damo, TransitiveClosure tc
                left join fetch damo.fishExperiment fx
                left join fetch fx.experiment exp
                left join fetch fx.fish
                left join fetch exp.experimentConditions conditions
                left join fetch damo.diseaseAnnotation da
                left join fetch da.publication
                left join fetch conditions.zecoTerm zecoTerm
                left join fetch conditions.chebiTerm chebiTerm
                left join fetch conditions.aoTerm aoTerm
                left join fetch conditions.spatialTerm spatialTerm
                left join fetch conditions.goCCTerm goCCTerm
                left join fetch conditions.taxaonymTerm taxonomyTerm
                where
                damo.diseaseAnnotation.disease = tc.child AND
                tc.root = :disease
                """;
        }
        if (fish != null) {
            if (includeChildren || disease != null) {
                hql += "and ";
            }
            hql += " fx.fish = :fish ";
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                if (entry.getKey().startsWith("fish")) {
                    hql += " AND lower(fx.fish.name) like :" + entry.getKey() + " ";
                }
                if (entry.getKey().startsWith("condition")) {
                    hql += " AND (";
                    hql += " lower(chebiTerm.termName) like :" + entry.getKey() + " ";
                    hql += " OR ";
                    hql += " lower(zecoTerm.termName) like :" + entry.getKey() + " ";
                    hql += " OR ";
                    hql += " lower(aoTerm.termName) like :" + entry.getKey() + " ";
                    hql += " OR ";
                    hql += " lower(spatialTerm.termName) like :" + entry.getKey() + " ";
                    hql += " OR ";
                    hql += " lower(goCCTerm.termName) like :" + entry.getKey() + " ";
                    hql += " OR ";
                    hql += " lower(taxonomyTerm.termName) like :" + entry.getKey() + " ";
                    hql += " ) ";
                }
                if (entry.getKey().startsWith("diseaseModels")) {
                    if (includeChildren) {
                        hql += " AND  lower(tc.child.termName) like :" + entry.getKey() + " ";
                    } else {
                        hql += " AND  lower(term.termName) like :" + entry.getKey() + " ";
                    }
                }
            }
        }

        Query<DiseaseAnnotationModel> query = HibernateUtil.currentSession().createQuery(hql, DiseaseAnnotationModel.class);
        if (disease != null) {
            query.setParameter("disease", disease);
        }
        if (fish != null) {
            query.setParameter("fish", fish);
        }
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue().toLowerCase() + "%");
            }
        }
        return query.list();
    }

    @Override
    public List<PhenotypeStatementWarehouse> getAllPhenotypeStatementsForSTR(SequenceTargetingReagent sequenceTargetingReagent) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct pheno from PhenotypeStatementWarehouse pheno, FishStr fishStr " +
                     "      where fishStr.fishID = pheno.phenotypeWarehouse.fishExperiment.fish.zdbID " +
                     "      AND fishStr.strID = :strID ";

        Query query = session.createQuery(hql);
        query.setParameter("strID", sequenceTargetingReagent.getZdbID());

        List<PhenotypeStatementWarehouse> strPhenotypeStatementsFish = (List<PhenotypeStatementWarehouse>) query.list();

        hql = "select distinct pheno from PhenotypeStatementWarehouse pheno, PhenotypeWarehouse phenoExp, FishExperiment fishExp, Fish fish, GenotypeFeature genoFeat, FeatureMarkerRelationship featMrkr  " +
              " where pheno.phenotypeWarehouse = phenoExp " +
              "   and phenoExp.fishExperiment = fishExp " +
              "   and fishExp.fish = fish" +
              "   and fish.genotype = genoFeat.genotype " +
              "   and genoFeat.feature = featMrkr.feature" +
              "   and featMrkr.marker = :str " +
              "   and featMrkr.featureMarkerRelationshipType.name = :createdBy ";

        query = session.createQuery(hql);
        query.setParameter("str", sequenceTargetingReagent);
        query.setParameter("createdBy", "created by");

        List<PhenotypeStatementWarehouse> strPhenotypeStatementsCreatedBy = (List<PhenotypeStatementWarehouse>) query.list();

        if (strPhenotypeStatementsCreatedBy != null && strPhenotypeStatementsCreatedBy.size() > 0)
            strPhenotypeStatementsFish.addAll(strPhenotypeStatementsCreatedBy);

        List<PhenotypeStatementWarehouse> notNormalPhenotypeStatements = new ArrayList<>();
        for (PhenotypeStatementWarehouse phenotypeStatement : strPhenotypeStatementsFish) {
            if (phenotypeStatement.isNotNormal()) {
                notNormalPhenotypeStatements.add(phenotypeStatement);
            }
        }

        return notNormalPhenotypeStatements;
    }

    @Override
    public List<PhenotypeWarehouse> getPhenotypeWarehouse(String figureID) {
        String hql = "from PhenotypeWarehouse where " +
                     " figure.zdbID = :figureID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("figureID", figureID);
        return (List<PhenotypeWarehouse>) query.list();
    }

    /**
     * Retrieve the status of the pheno mart
     *
     * @return status
     */
    @Override
    public ZdbFlag getPhenoMartStatus() {
        Session session = HibernateUtil.currentSession();
        Query<ZdbFlag> query = session.createQuery("from ZdbFlag where type = :type", ZdbFlag.class);
        query.setParameter("type", ZdbFlag.Type.REGEN_PHENOTYPEMART);
        return query.uniqueResult();
    }

    @Override
    public List<DiseaseAnnotationModel> getHumanDiseaseModelsByFish(String fishID, String publicationID) {
        String hql = "from DiseaseAnnotationModel as disease where" +
                     " disease.fishExperiment.fish.zdbID = :fishID AND " +
                     " disease.diseaseAnnotation.publication.zdbID = :pubID";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("fishID", fishID);
        query.setParameter("pubID", publicationID);
        return (List<DiseaseAnnotationModel>) query.list();
    }

    @Override
    public List<DiseaseAnnotationModel> getDiseaseAnnotationModelsByGene(Marker gene) {
        String sql = """
                     select distinct damo_pk_id 
                       from disease_annotation_model, mutant_fast_search 
                      where mfs_genox_zdb_id = damo_genox_zdb_id 
                        and mfs_data_zdb_id = :geneZdbID 
                     """;

        List<Long> modelZdbIds = HibernateUtil.currentSession().createNativeQuery(sql, Long.class)
            .setParameter("geneZdbID", gene.getZdbID())
            .list();

        String hql = "from DiseaseAnnotationModel where ID in (:modelZdbIds)";

        return HibernateUtil.currentSession().createQuery(hql, DiseaseAnnotationModel.class)
                .setParameterList("modelZdbIds", modelZdbIds)
                .list();
    }

    @Override
    public List<HumanGeneDetail> getHumanGeneDetailList() {
        return HibernateUtil.currentSession().createQuery("from HumanGeneDetail", HumanGeneDetail.class).list();
    }

    @Override
    public boolean hasObsoletePhenotype(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select phenox from PhenotypeExperiment phenox
                 where phenox.figure.publication.zdbID = :pubID
                 """;
        Query<PhenotypeExperiment> query = session.createQuery(hql, PhenotypeExperiment.class);
        query.setParameter("pubID", publicationID);

        List<PhenotypeExperiment> structures = query.list();
        return structures.stream().anyMatch(PhenotypeExperiment::hasObsoletePhenotype);
    }
}
