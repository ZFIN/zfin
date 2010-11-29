package org.zfin.mutant.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.database.InformixUtil;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.marker.Marker;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.MutantFigureStage;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Class defines methods to retrieve phenotypic data for annotation purposes
 */
public class HibernatePhenotypeRepository implements PhenotypeRepository {

    private static Logger LOG = Logger.getLogger(HibernatePhenotypeRepository.class);

    @SuppressWarnings("unchecked")
    public List<PhenotypeStructure> retrievePhenotypeStructures(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select structure from PhenotypeStructure structure where structure.publication.zdbID = :pubID " +
                "       order by structure.superterm.termName";
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
        Publication pub = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        structure.setPublication(pub);
        structure.setDate(new Date());
        if (isPhenotypeStructureOnPile(structure))
            return;
        structure.setPerson(Person.getCurrentSecurityUser());
        Session session = HibernateUtil.currentSession();
        session.save(structure);
    }

    public boolean isPhenotypeStructureOnPile(PhenotypeStructure structure) {
        Session session = HibernateUtil.currentSession();

        String hql = "select structure from PhenotypeStructure as structure where " +
                "       structure.superterm.ID = :supertermID";
        if (structure.getSubterm() != null)
            hql += " AND structure.subterm.ID = :subtermID ";
        hql += " AND structure.quality.ID = :qualityID ";
        hql += " AND structure.tag = :tag ";
        hql += " AND structure.publication.zdbID = :publicationID ";
        Query query = session.createQuery(hql);
        query.setParameter("supertermID", structure.getSuperterm().getID());
        query.setParameter("qualityID", structure.getQuality().getID());
        query.setParameter("tag", structure.getTag());
        query.setParameter("publicationID", structure.getPublication().getZdbID());
        if (structure.getSubterm() != null)
            query.setParameter("subtermID", structure.getSubterm().getID());
        List list = query.list();
        if (list == null || list.isEmpty())
            return false;
        return true;
    }

    /**
     * Check if a phenotype structure is already on the pile.
     *
     * @param phenotypeTerm phenotype structure
     * @param publicationID Publication
     * @return boolean
     */
    public boolean isPhenotypePileStructureExists(PhenotypeTermDTO phenotypeTerm, String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select structure from PhenotypeStructure as structure where " +
                "       structure.superterm.termName = :supertermName";
        if (phenotypeTerm.getSubterm() != null)
            hql += " AND structure.subterm.termName = :subtermName ";
        hql += " AND structure.quality.termName = :qualityName ";
        hql += " AND structure.publication.zdbID = :publicationID ";
        hql += " AND structure.tag = :tag ";
        Query query = session.createQuery(hql);
        query.setParameter("supertermName", phenotypeTerm.getSuperterm().getTermName());
        query.setParameter("qualityName", phenotypeTerm.getQuality().getTermName());
        query.setParameter("publicationID", publicationID);
        query.setParameter("tag", Phenotype.Tag.getTagFromName(phenotypeTerm.getTag()));
        if (phenotypeTerm.getSubterm() != null)
            query.setParameter("subtermName", phenotypeTerm.getSubterm().getTermName());
        List<PhenotypeStructure> structures = (List<PhenotypeStructure>) query.list();


        if (CollectionUtils.isNotEmpty(structures)) {
            // this is most cases
            for (PhenotypeStructure structure : structures) {
                // for fogbugz case 5722
                // if your hit has a substructure, but you do not, then
                if ((phenotypeTerm.getSubterm() == null && structure.getSubterm() == null)
                        || (phenotypeTerm.getSubterm() != null && structure.getSubterm() != null)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
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
    public List<MutantFigureStage> getMutantExpressionsByFigureFish(String publicationID, String figureID, String fishID, String featureID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select phenotype, figure from Phenotype phenotype, Figure figure";
        if (featureID != null) {
            hql += ", GenotypeFeature genoFeature ";
        }
        hql += "       left join fetch phenotype.startStage ";
        hql += "       left join fetch phenotype.endStage ";
        hql += "       left join fetch phenotype.figures ";
        hql += "       left join fetch phenotype.genotypeExperiment";
        hql += "       left join fetch phenotype.genotypeExperiment.genotype ";
        if (fishID != null) {
            hql += "       join phenotype.genotypeExperiment.genotype geno";
        }
        hql += "     where phenotype.publication.zdbID = :pubID ";
        if (fishID != null)
            hql += "           and geno.zdbID = :fishID ";
        if (figureID != null)
            hql += "           and figure.zdbID = :figureID ";
        if (featureID != null) {
            hql += "           and phenotype.genotypeExperiment.genotype = genoFeature.genotype ";
            hql += "           and genoFeature.feature.zdbID = :featureID ";
        }
        hql += " AND figure member of phenotype.figures ";
        hql += "    order by figure.orderingLabel, " +
                "             phenotype.genotypeExperiment.genotype.nickname, " +
                "             phenotype.startStage.abbreviation ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        if (fishID != null)
            query.setString("fishID", fishID);
        if (figureID != null)
            query.setString("figureID", figureID);
        if (featureID != null)
            query.setString("featureID", featureID);

        //query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<Object[]> objects = (List<Object[]>) query.list();
        if (objects == null)
            return null;

        return populatePhenotypeExperimentFigureStage(objects);
    }

    /**
     * Retrieve Mutant Figure Stage record
     *
     * @param genotypeID genotype
     * @param figureID   figure
     * @param startID    start stage
     * @param endID      end stage
     * @return MutantFigureStage
     */
    @SuppressWarnings("unchecked")
    public MutantFigureStage getMutant(String genotypeID, String figureID, String startID, String endID, String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select phenotype, figure from Phenotype phenotype, Figure figure";
        hql += "       join phenotype.genotypeExperiment.genotype geno";
        hql += "     where phenotype.publication.zdbID = :pubID ";
        hql += "           and geno.zdbID = :fishID ";
        hql += "           and figure.zdbID = :figureID ";
        hql += "           and phenotype.startStage.zdbID= :startID ";
        hql += "           and phenotype.endStage.zdbID= :endID ";
        hql += " AND figure member of phenotype.figures ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setString("fishID", genotypeID);
        query.setString("figureID", figureID);
        query.setString("startID", startID);
        query.setString("endID", endID);
        Iterable<Object[]> phenoFigures = (List<Object[]>) query.list();

        if (phenoFigures == null)
            return null;

        MutantFigureStage mutantFigureStage = new MutantFigureStage();
        int index = 0;
        for (Object[] phenotypeFigure : phenoFigures) {
            Phenotype phenotype = (Phenotype) phenotypeFigure[0];
            Figure figure = (Figure) phenotypeFigure[1];

            GenotypeExperiment genotypeExperiment = phenotype.getGenotypeExperiment();
            if (index == 0) {
                mutantFigureStage.setGenotypeExperiment(genotypeExperiment);
                mutantFigureStage.setStart(phenotype.getStartStage());
                mutantFigureStage.setEnd(phenotype.getEndStage());
                mutantFigureStage.setFigure(figure);
                mutantFigureStage.setPublication(phenotype.getPublication());
            }
            genotypeExperiment.addPhenotype(phenotype);
            mutantFigureStage.addPhenotype(phenotype);
            index++;
        }
        return mutantFigureStage;
    }

    /**
     * Retrieve a mutant figure stage record from the unique key
     *
     * @param mutantFigureStage mutant figure stage unique key
     * @param figureID          figure
     * @return full mutant figure stage record
     */
    @SuppressWarnings("unchecked")
    public MutantFigureStage getMutant(MutantFigureStage mutantFigureStage, String figureID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select phenotype, figure from Phenotype phenotype, Figure figure";
        hql += "       join phenotype.genotypeExperiment.genotype geno";
        hql += "       join phenotype.genotypeExperiment.experiment environment";
        hql += "     where phenotype.publication.zdbID = :pubID ";
        hql += "           and geno.zdbID = :fishID ";
        hql += "           and environment.zdbID = :environmentID ";
        hql += "           and figure.zdbID = :figureID ";
        hql += "           and phenotype.startStage.zdbID= :startID ";
        hql += "           and phenotype.endStage.zdbID= :endID ";
        hql += " AND figure member of phenotype.figures ";
        Query query = session.createQuery(hql);
        query.setString("pubID", mutantFigureStage.getPublication().getZdbID());
        query.setString("fishID", mutantFigureStage.getGenotypeExperiment().getGenotype().getZdbID());
        query.setString("environmentID", mutantFigureStage.getGenotypeExperiment().getExperiment().getZdbID());
        query.setString("figureID", figureID);
        query.setString("startID", mutantFigureStage.getStart().getZdbID());
        query.setString("endID", mutantFigureStage.getEnd().getZdbID());
        Iterable<Object[]> phenoFigures = (List<Object[]>) query.list();

        if (phenoFigures == null)
            return null;

        MutantFigureStage mfs = new MutantFigureStage();
        int index = 0;
        for (Object[] phenotypeFigure : phenoFigures) {
            Phenotype phenotype = (Phenotype) phenotypeFigure[0];
            Figure figure = (Figure) phenotypeFigure[1];

            GenotypeExperiment genotypeExperiment = phenotype.getGenotypeExperiment();
            if (index == 0) {
                mfs.setGenotypeExperiment(genotypeExperiment);
                mfs.setStart(phenotype.getStartStage());
                mfs.setEnd(phenotype.getEndStage());
                mfs.setFigure(figure);
                mfs.setPublication(phenotype.getPublication());
            }
            genotypeExperiment.addPhenotype(phenotype);
            mfs.addPhenotype(phenotype);
            index++;
        }
        return mfs;
    }

    private List<MutantFigureStage> populatePhenotypeExperimentFigureStage(Iterable<Object[]> objects) {
        List<MutantFigureStage> efses = new ArrayList<MutantFigureStage>(20);
        for (Object[] object : objects) {
            Phenotype phenotype = (Phenotype) object[0];
            Figure figure = (Figure) object[1];
            MutantFigureStage efs = new MutantFigureStage();
            GenotypeExperiment genotypeExperiment = phenotype.getGenotypeExperiment();
            genotypeExperiment.addPhenotype(phenotype);
            efs.setGenotypeExperiment(genotypeExperiment);
            efs.setFigure(figure);
            efs.addPhenotype(phenotype);
            if (!efses.contains(efs)) {
                efses.add(efs);
            } else {
                for (MutantFigureStage ef : efses) {
                    if (ef.equals(efs)) {
                        ef.addPhenotype(phenotype);
                    }
                }
            }
        }
        return efses;
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
     * Create a new phenotype record.
     *
     * @param phenotype Phenotype
     */
    public void createPhenotype(Phenotype phenotype, Figure figure) {
        Session session = HibernateUtil.currentSession();

        Phenotype unspecifiedPhenotype = getUnspecifiedPhenotype(phenotype);

        // ignore unspecified addition if not the first creation.
        //String ontologyName = phenotype.getSuperterm().getOntology();
        Term aoSuperTerm = phenotype.getSuperterm();
        if (aoSuperTerm != null && aoSuperTerm.getTermName().equals(Term.UNSPECIFIED)) {
            if (phenotype.getZdbID() == null)
                // new unspecified record
                // if there is an 'unspecified' add figure to it.
                if (unspecifiedPhenotype != null) {
                    unspecifiedPhenotype.addFigure(figure);
                } else {
                    session.save(phenotype);
                }
            return;
        }

        Phenotype existingPhenotype = getMutantPhenotypes(phenotype);

        // is the mutant phenotype already used in a different figure?
        if (existingPhenotype != null && !existingPhenotype.getFigures().contains(figure)) {
            existingPhenotype.addFigure(figure);
            if (unspecifiedPhenotype != null) {
                Set<Figure> figures = unspecifiedPhenotype.getFigures();
                // has no figures associated
                if (figures == null || figures.size() < 2) {
                    session.delete(unspecifiedPhenotype);
                }
            }
        } else {
            // no phenotype record with given structures found
            // unspecified expression result record exists
            if (unspecifiedPhenotype != null) {
                Set<Figure> figures = unspecifiedPhenotype.getFigures();
                // has no figures associated
                if (figures == null || figures.isEmpty()) {
                    session.delete(unspecifiedPhenotype);
                } else if (figures.size() == 1) {
                    // has one figure associated
                    // check if it is associated to the figure in question.
                    // if yes, remove it.
                    if (figures.contains(figure)) {
                        session.delete(unspecifiedPhenotype);
                        session.flush();
                    }
                } else {
                    // has more than one figure associated
                    unspecifiedPhenotype.getFigures().remove(figure);
                }
            }
            phenotype.getGenotypeExperiment().addPhenotype(phenotype);
            session.save(phenotype);
        }
        runRegenGenotypeFigureScript(phenotype);
    }

    /**
     * Run database script to regenerate fast search tables.
     *
     * @param phenotype Phenotype
     */
    public void runRegenGenotypeFigureScript(Phenotype phenotype) {
        InformixUtil.runInformixProcedure("regen_genofig_genotype", phenotype.getGenotypeExperiment().getGenotype().getZdbID());
        Set<ExperimentCondition> conditions = phenotype.getGenotypeExperiment().getExperiment().getMorpholinoConditions();
        if (conditions != null) {
            for (ExperimentCondition condition : conditions) {
                Marker morpholino = condition.getMorpholino();
                InformixUtil.runInformixProcedure("regen_genox_marker", morpholino.getZdbID());
                List<Marker> targetGenes = getMarkerRepository().getTargetGenesForMorpholino(morpholino);
                for (Marker targetGene : targetGenes)
                    InformixUtil.runInformixProcedure("regen_genox_marker", targetGene.getZdbID());
            }
        }
    }

    /**
     * Remove a phenotype associated to a given figure.
     * If the phenotype is associated to more than one figure then only
     * the association is removed. Otherwise, the phenotype record is deleted as well.
     * If this was the last phenotype for the mutant figure stage records the default phenotype
     * is added.
     *
     * @param phenotype phenotype
     * @param figure    Figure
     */
    public void deletePhenotype(Phenotype phenotype, Figure figure) {
        if (phenotype == null)
            return;

        Session session = HibernateUtil.currentSession();
        Set<Figure> figures = phenotype.getFigures();
        if (figures == null)
            return;

        boolean lastPhenotypeOnMutant = true;
        for (Phenotype phenotypeOnMutantFigureStage : phenotype.getGenotypeExperiment().getPhenotypes()) {
            // filter out the ones that have the same stage info, i.e. the same efs
            if (phenotype.getStartStage().equals(phenotypeOnMutantFigureStage.getStartStage()) && phenotype.getEndStage().equals(phenotypeOnMutantFigureStage.getEndStage())) {
                if (!phenotypeOnMutantFigureStage.equals(phenotype)) {
                    if (phenotypeOnMutantFigureStage.getFigures().contains(figure)) {
                        lastPhenotypeOnMutant = false;
                        break;
                    }
                }
            }
        }

        Phenotype unspecifiedResult = getUnspecifiedPhenotype(phenotype);
        if (figures.size() > 1) {
            phenotype.removeFigure(figure);
            if (lastPhenotypeOnMutant) {
                if (unspecifiedResult != null) {
                    unspecifiedResult.addFigure(figure);
                } else {
                    // add default phenotype
                    Phenotype defaultPhenotype = createDefaultPhenotype(phenotype);
                    defaultPhenotype.addFigure(figure);
                }
            }
        } else if ((figures.size() == 1 && figures.iterator().next().equals(figure))) {
            if (!lastPhenotypeOnMutant) {
                session.delete(phenotype);
                session.flush();
            } else {
                if (unspecifiedResult != null) {
                    session.delete(phenotype);
                    session.flush();
                    unspecifiedResult.addFigure(figure);
                } else {
                    session.delete(phenotype);
                    session.flush();
                    // add default phenotype
                    Phenotype defaultPhenotype = createDefaultPhenotype(phenotype);
                    defaultPhenotype.addFigure(figure);
                }
            }
        }
        session.refresh(phenotype.getGenotypeExperiment());
        runRegenGenotypeFigureScript(phenotype);
    }


    @SuppressWarnings("unchecked")
    private Phenotype getUnspecifiedPhenotype(Phenotype phenotype) {
        Term unspecified = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED);
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Phenotype.class);
        criteria.add(Restrictions.eq("genotypeExperiment", phenotype.getGenotypeExperiment()));
        criteria.add(Restrictions.eq("publication", phenotype.getPublication()));
        criteria.add(Restrictions.eq("startStage", phenotype.getStartStage()));
        criteria.add(Restrictions.eq("endStage", phenotype.getEndStage()));
        criteria.add(Restrictions.eq("superterm", unspecified));
        return (Phenotype) criteria.uniqueResult();
    }


    /**
     * Retrieves all mutant phenotypes that match a given phenotype.
     * It basically means, a mutant phenotype is used in a different figure.
     *
     * @param phenotype Phenotype
     * @return list of mutant phenotypes in which the given phenotype is used
     */
    @SuppressWarnings("unchecked")
    private Phenotype getMutantPhenotypes(Phenotype phenotype) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Phenotype.class);
        Term subterm = phenotype.getSubterm();
        if (subterm == null)
            criteria.add(Restrictions.isNull("subterm"));
        else
            criteria.add(Restrictions.eq("subterm", subterm));
        criteria.add(Restrictions.eq("superterm", phenotype.getSuperterm()));
        criteria.add(Restrictions.eq("term", phenotype.getTerm()));
        criteria.add(Restrictions.eq("tag", phenotype.getTag()));
        criteria.add(Restrictions.eq("startStage", phenotype.getStartStage()));
        criteria.add(Restrictions.eq("endStage", phenotype.getEndStage()));
        criteria.add(Restrictions.eq("genotypeExperiment", phenotype.getGenotypeExperiment()));
        criteria.add(Restrictions.eq("publication", phenotype.getPublication()));
        return (Phenotype) criteria.uniqueResult();
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
    public Phenotype createDefaultPhenotype(Phenotype pheno) {
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
            return existingPhenotype;
        }

        Term unspecified = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED);
        Term quality = OntologyManager.getInstance().getTermByName(Ontology.QUALITY, GenericTerm.QUALITY);

        Session session = HibernateUtil.currentSession();
        // if this phenotype is a persistent record create a new one.
        if (pheno.getZdbID() != null) {
            Phenotype defaultPhenotype = new Phenotype();
            defaultPhenotype.setSuperterm(unspecified);
            defaultPhenotype.setTerm(quality);
            defaultPhenotype.setTag(Phenotype.Tag.ABNORMAL.toString());
            defaultPhenotype.setStartStage(pheno.getStartStage());
            defaultPhenotype.setEndStage(pheno.getEndStage());
            defaultPhenotype.setPublication(pheno.getPublication());
            defaultPhenotype.setGenotypeExperiment(pheno.getGenotypeExperiment());
            session.save(defaultPhenotype);
            return defaultPhenotype;
        } else {
            pheno.setSuperterm(unspecified);
            pheno.setTerm(quality);
            pheno.setTag(Phenotype.Tag.ABNORMAL.toString());
            session.save(pheno);
            return pheno;
        }
    }

    /**
     * Retrieve a list of phenotypes used for a given publication.
     *
     * @param publicationID publication
     * @return set of phenotypes
     */
    @SuppressWarnings("unchecked")
    public List<Phenotype> getAllPhenotypes(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct phenotype from Phenotype phenotype where" +
                "          phenotype.publication.zdbID = :publicationID";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        return (List<Phenotype>) query.list();
    }

    /**
     * Create the Phenotype pile structure pile if it does not already exist.
     * After closing a publication the structure pile is removed and needs to be
     * re-created when re-opened so curators do not have to re-create the pile manually.
     *
     * @param publicationID publication
     */
    public void createPhenotypePile(String publicationID) {
        List<Phenotype> phenotypes = getAllPhenotypes(publicationID);
        if (phenotypes == null)
            return;
        Publication publication = getPublicationRepository().getPublication(publicationID);
        for (Phenotype phenotype : phenotypes) {
            PhenotypeStructure structure = instantiatePhenotypeStructureFromPheno(publication, phenotype);
            createPhenotypeStructure(structure, publicationID);
        }
    }

    private PhenotypeStructure instantiatePhenotypeStructureFromPheno(Publication publication, Phenotype phenotype) {
        PhenotypeStructure structure = new PhenotypeStructure();
        structure.setDate(new Date());
        structure.setPerson(Person.getCurrentSecurityUser());
        structure.setPublication(publication);
        Term supertermForm = phenotype.getSuperterm();
        Term superterm = getInfrastructureRepository().getTermByName(supertermForm.getTermName(), phenotype.getSuperterm().getOntology());
        structure.setSuperterm(superterm);
        Term subTerm = phenotype.getSubterm();
        if (subTerm != null) {
            Term subterm = getInfrastructureRepository().getTermByName(subTerm.getTermName(), subTerm.getOntology());
            structure.setSubterm(subterm);
        }
        structure.setQuality(phenotype.getTerm());
        structure.setTag(Phenotype.Tag.getTagFromName(phenotype.getTag()));
        return structure;
    }

    /**
     * Create a default phenotype record for a given mutant figure stage.
     *
     * @param mfs MutantFigureStage
     * @return Phenotype
     */
    public Phenotype createDefaultPhenotype(MutantFigureStage mfs) {
        GenotypeExperiment genotypeExperiment = mfs.getGenotypeExperiment();
        Term unspecified = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED);
        Term quality = OntologyManager.getInstance().getTermByName(Ontology.QUALITY, GenericTerm.QUALITY);
        Phenotype phenotype = new Phenotype();
        phenotype.setGenotypeExperiment(genotypeExperiment);
        phenotype.setSuperterm(unspecified);
        phenotype.setTerm(quality);
        phenotype.setTag(Phenotype.Tag.ABNORMAL.toString());
        phenotype.setStartStage(mfs.getStart());
        phenotype.setEndStage(mfs.getEnd());
        phenotype.setPublication(mfs.getPublication());
        phenotype.addFigure(mfs.getFigure());
        Session session = HibernateUtil.currentSession();
        session.save(phenotype);
        genotypeExperiment.addPhenotype(phenotype);
        mfs.addPhenotype(phenotype);
        return phenotype;
    }

}
