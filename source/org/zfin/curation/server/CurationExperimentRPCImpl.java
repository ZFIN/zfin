package org.zfin.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.client.rpc.SerializableException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.curation.client.*;
import org.zfin.curation.dto.ExperimentDTO;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.people.CuratorSession;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of RPC calls from the client.
 * Retrieves experiment-related info on the FX Curation page.
 */
public class CurationExperimentRPCImpl extends RemoteServiceServlet implements CurationExperimentRPC {

    private final static Logger LOG = RootLogger.getLogger(CurationExperimentRPCImpl.class);

    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static ExpressionRepository expRepository = RepositoryFactory.getExpressionRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static ProfileRepository profileRep = RepositoryFactory.getProfileRepository();
    private static InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();

    public List<MarkerDTO> getGenes(String pubID) throws PublicationNotFoundException {

        Publication publication = pubRepository.getPublication(pubID);
        if (publication == null) {
            throw new PublicationNotFoundException(pubID);
        }

        List<Marker> markers = pubRepository.getGenesByPublication(pubID);
        List<MarkerDTO> genes = new ArrayList<MarkerDTO>();

        for (Marker marker : markers) {
            MarkerDTO gene = new MarkerDTO();
            gene.setAbbreviation(marker.getAbbreviation());
            gene.setZdbID(marker.getZdbID());
            genes.add(gene);
        }

        return genes;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Delete a given experiment.
     *
     * @param experimentZdbID experiment id
     */
    public void deleteExperiment(String experimentZdbID) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment experiment = expRepository.getExpressionExperiment(experimentZdbID);
            expRepository.deleteExpressionExperiment(experiment);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Delete", e);
            tx.rollback();
            throw e;
        }
    }

    /**
     * Retrieve all experiments for a given publication.
     *
     * @param publicationID publication
     * @return list of experiments
     */
    public List<ExperimentDTO> readExperiments(String publicationID) {
        List<ExpressionExperiment> experiments = pubRepository.getExperiments(publicationID);
        if (experiments == null)
            return null;

        List<ExperimentDTO> dtos = new ArrayList<ExperimentDTO>();
        for (ExpressionExperiment experiment : experiments) {
            ExperimentDTO dto = new ExperimentDTO();
            dto.setExperimentZdbID(experiment.getZdbID());
            Marker gene = experiment.getMarker();
            if (gene != null) {
                dto.setGeneName(gene.getAbbreviation());
                dto.setGeneZdbID(gene.getZdbID());
                if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                    String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                    dto.setGenbankNumber(dblink);
                    dto.setGenbankID(experiment.getMarkerDBLink().getZdbID());
                }
            }
            if (experiment.getAntibody() != null) {
                dto.setAntibody(experiment.getAntibody().getAbbreviation());
                dto.setAntibodyID(experiment.getAntibody().getZdbID());
            }
            dto.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
            dto.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
            dto.setEnvironment(experiment.getGenotypeExperiment().getExperiment().getName());
            dto.setEnvironmentID(experiment.getGenotypeExperiment().getExperiment().getZdbID());
            dto.setAssay(experiment.getAssay().getName());
            // check if there are expressions associated
            Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
            if (expressionResults != null)
                dto.setNumberOfExpressions(experiment.getDistinctExpressions());

            dtos.add(dto);
        }
        return dtos;
    }

    public List<ExperimentDTO> getExperimentsByFilter(ExperimentDTO experimentFilter) {
        List<ExpressionExperiment> experiments =
                pubRepository.getExperimentsByGeneAndFish(experimentFilter.getPublicationID(),
                        experimentFilter.getGeneZdbID(),
                        experimentFilter.getFishID());
        if (experiments == null)
            return null;

        List<ExperimentDTO> dtos = new ArrayList<ExperimentDTO>();
        for (ExpressionExperiment experiment : experiments) {
            ExperimentDTO dto = new ExperimentDTO();
            dto.setExperimentZdbID(experiment.getZdbID());
            Marker gene = experiment.getMarker();
            if (gene != null) {
                dto.setGeneName(gene.getAbbreviation());
                dto.setGeneZdbID(gene.getZdbID());
                if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                    String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                    dto.setGenbankNumber(dblink);
                    dto.setGenbankID(experiment.getMarkerDBLink().getZdbID());
                }
            }
            if (experiment.getAntibody() != null) {
                dto.setAntibody(experiment.getAntibody().getAbbreviation());
                dto.setAntibodyID(experiment.getAntibody().getZdbID());
            }
            dto.setFishName(experiment.getGenotypeExperiment().getGenotype().getHandle());
            dto.setFishID(experiment.getGenotypeExperiment().getGenotype().getZdbID());
            dto.setEnvironment(experiment.getGenotypeExperiment().getExperiment().getName());
            dto.setEnvironmentID(experiment.getGenotypeExperiment().getExperiment().getZdbID());
            dto.setAssay(experiment.getAssay().getName());
            dto.setAssayAbbreviation(experiment.getAssay().getAbbreviation());
            // check if there are expressions associated
            Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
            if (expressionResults != null)
                dto.setNumberOfExpressions(experiment.getDistinctExpressions());
            // check if a clone is available
            if (StringUtils.isNotEmpty(experiment.getCloneID())) {
                Marker clone = markerRepository.getMarkerByID(experiment.getCloneID());
                dto.setCloneID(clone.getZdbID());
                dto.setCloneName(clone.getAbbreviation() + " [" + clone.getType().toString() + "]");
            }
            dtos.add(dto);
        }
        return dtos;
    }

    public List<FishDTO> getFish(String publicationID) {
        List<Genotype> genos = pubRepository.getFishUsedInExperiment(publicationID);
        if (genos == null)
            return null;

        List<FishDTO> dtos = new ArrayList<FishDTO>();
        for (Genotype genotype : genos) {
            FishDTO dto = new FishDTO();
            dto.setZdbID(genotype.getZdbID());
            dto.setName(genotype.getHandle());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<String> getAssays() {
        InfrastructureRepository infra = RepositoryFactory.getInfrastructureRepository();
        List<ExpressionAssay> assays = infra.getAllAssays();
        List<String> assayDtos = new ArrayList<String>();
        for (ExpressionAssay assay : assays)
            assayDtos.add(assay.getName());
        return assayDtos;
    }

    public List<EnvironmentDTO> getEnvironments(String publicationID) {
        List<Experiment> experiments = pubRepository.getExperimentsByPublication(publicationID);
        List<EnvironmentDTO> assayDtos = new ArrayList<EnvironmentDTO>();
        for (Experiment experiment : experiments) {
            EnvironmentDTO env = new EnvironmentDTO();
            env.setName(experiment.getName());
            env.setZdbID(experiment.getZdbID());
            assayDtos.add(env);
        }

        return assayDtos;
    }

    public List<FishDTO> getGenotypes(String publicationID) {
        List<FishDTO> genotypes = new ArrayList<FishDTO>();
        Genotype genotype = pubRepository.getGenotypeByNickname("WT");
        FishDTO fish = new FishDTO();
        fish.setZdbID(genotype.getZdbID());
        fish.setName(genotype.getHandle());
        genotypes.add(fish);
        fish = new FishDTO();
        fish.setZdbID(null);
        fish.setName("---------");
        genotypes.add(fish);
        List<Genotype> genos = pubRepository.getNonWTGenotypesByPublication(publicationID);
        for (Genotype geno : genos) {
            FishDTO fishy = new FishDTO();
            fishy.setZdbID(geno.getZdbID());
            fishy.setName(geno.getHandle());
            genotypes.add(fishy);
        }
        fish = new FishDTO();
        fish.setZdbID(null);
        fish.setName("---------");
        genotypes.add(fish);
        MutantRepository mutantRep = RepositoryFactory.getMutantRepository();
        List<Genotype> wiltdyptes = mutantRep.getAllWildtypeGenotypes();
        for (Genotype wiltype : wiltdyptes) {
            FishDTO fishy = new FishDTO();
            fishy.setZdbID(wiltype.getZdbID());
            fishy.setName(wiltype.getNickname());
            genotypes.add(fishy);
        }
        return genotypes;
    }

    public List<MarkerDTO> getAntibodies(String publicationID) {
        List<Antibody> antibodies = pubRepository.getAntibodiesByPublication(publicationID);
        List<MarkerDTO> markers = new ArrayList<MarkerDTO>();
        for (Antibody antibody : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setAbbreviation(antibody.getName());
            env.setZdbID(antibody.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    public List<MarkerDTO> readAntibodiesByGene(String publicationID, String geneID) {
        if (StringUtils.isEmpty(geneID))
            return getAntibodies(publicationID);

        List<Antibody> antibodies = pubRepository.getAntibodiesByPublicationAndGene(publicationID, geneID);
        List<MarkerDTO> markers = new ArrayList<MarkerDTO>();
        for (Antibody antibody : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setAbbreviation(antibody.getName());
            env.setZdbID(antibody.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     */
    public List<MarkerDTO> readGenesByAntibody(String publicationID, String antibodyID) throws PublicationNotFoundException {
        if (StringUtils.isEmpty(antibodyID))
            return getGenes(publicationID);

        List<Marker> antibodies = pubRepository.getGenesByAntibody(publicationID, antibodyID);
        List<MarkerDTO> markers = new ArrayList<MarkerDTO>();
        for (Marker gene : antibodies) {
            MarkerDTO env = new MarkerDTO();
            env.setAbbreviation(gene.getAbbreviation());
            env.setZdbID(gene.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    /**
     * Retrieve the accession numbers for a given gene
     *
     * @param geneID string
     */
    public List<ExperimentDTO> readGenbankAccessions(String publicationID, String geneID) {
        List<MarkerDBLink> geneDBLinks = pubRepository.getDBLinksByGene(publicationID, geneID);
        List<ExperimentDTO> accessionNumbers = new ArrayList<ExperimentDTO>();
        for (MarkerDBLink geneDBLink : geneDBLinks) {
            ExperimentDTO accession = new ExperimentDTO();
            accession.setGenbankNumber(geneDBLink.getAccessionNumber());
            accession.setGenbankID(geneDBLink.getZdbID());
            accessionNumbers.add(accession);
        }
        List<MarkerDBLink> cloneDBLinks = pubRepository.getDBLinksForCloneByGene(publicationID, geneID);
        for (MarkerDBLink cloneDBLink : cloneDBLinks) {
            ExperimentDTO accession = new ExperimentDTO();
            accession.setGenbankNumber(cloneDBLink.getAccessionNumber() + " [" + cloneDBLink.getMarker().getType().toString() + "]");
            accession.setGenbankID(cloneDBLink.getZdbID());
            accessionNumbers.add(accession);
        }
        return accessionNumbers;

    }

    /**
     * Update an existing experiment.
     *
     * @param experimentDTO experiment to be updated
     */
    public ExperimentDTO updateExperiment(ExperimentDTO experimentDTO) {
        if (experimentDTO == null)
            return null;
        String experimentID = experimentDTO.getExperimentZdbID();
        if (experimentID == null)
            throw new RuntimeException("No experiment ID provided");

        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment expressionExperiment = expressionRep.getExpressionExperiment(experimentID);
            //createAuditRecordsForModifications(expressionExperiment, experimentDTO);
            // update assay: never null
            populateExpressionExperiment(experimentDTO, expressionExperiment);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return experimentDTO;
    }

    // ToDo: This will be done much more elegantly within an interceptor class in Hibernate.
    private void createAuditRecordsForModifications(ExpressionExperiment expressionExperiment, ExperimentDTO experimentDTO) {
        // check which attributes have changed when updating an experiment
        String comment = "updated Experiment";
        // gene
        Marker oldGene = expressionExperiment.getMarker();
        String oldGeneID = null;
        if (oldGene != null)
            oldGeneID = oldGene.getZdbID();
        String newGeneID = experimentDTO.getGeneZdbID();
        createAuditRecord(expressionExperiment, comment, oldGeneID, newGeneID, "Gene");

        // antibody
        Antibody oldAntibody = expressionExperiment.getAntibody();
        String oldAntibodyID = null;
        if(oldAntibody != null)
            oldAntibodyID = oldAntibody.getZdbID();
        String newAntibodyID = experimentDTO.getAntibodyID();
        createAuditRecord(expressionExperiment, comment, oldAntibodyID, newAntibodyID, "Antibody");

        String oldAssay = expressionExperiment.getAssay().getName();
        String newAssay = experimentDTO.getAssay();
        createAuditRecord(expressionExperiment, comment, oldAssay, newAssay, "Assay");

        String oldEnvironment = expressionExperiment.getGenotypeExperiment().getExperiment().getName();
        String newEnvironment = experimentDTO.getEnvironment();
        createAuditRecord(expressionExperiment, comment, oldEnvironment, newEnvironment, "Environment");

        String oldGenotypeID = expressionExperiment.getGenotypeExperiment().getGenotype().getZdbID();
        String newGenotypeID = experimentDTO.getFishID();
        createAuditRecord(expressionExperiment, comment, oldGenotypeID, newGenotypeID, "Fish");

    }

    private void createAuditRecord(ExpressionExperiment expressionExperiment, String comment, String oldValue, String newValue, String fieldName) {
        if (!StringUtils.equals(oldValue, newValue)) {
            infraRep.insertUpdatesTable(expressionExperiment.getZdbID(), fieldName, oldValue, newValue, comment);
        }
    }


    public ExperimentDTO createExpressionExperiment(ExperimentDTO experimentDTO) throws Exception {
        if (experimentDTO == null)
            return null;

        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment expressionExperiment = new ExpressionExperiment();
            populateExpressionExperiment(experimentDTO, expressionExperiment);

            expressionRep.createExpressionExperiment(expressionExperiment);
            experimentDTO.setExperimentZdbID(expressionExperiment.getZdbID());
            tx.commit();
        } catch (ConstraintViolationException e) {
            tx.rollback();
            throw new Exception("Experiment already exist. Experiments have to be unique.");
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return experimentDTO;
    }

    /**
     * Check the visibility of the experiment section
     */
    public boolean readExperimentSectionVisibility(String publicationID) {
        CuratorSession session = profileRep.getCuratorSession(publicationID, CuratorSession.Attribute.SHOW_EXPERIMENT_SECTION);
        return session == null || session.getValue().equals("true");
    }

    /**
     * Set Experiment Section visibility.
     *
     * @param pubID                publication ID
     * @param experimentVisibility experiment section visibility
     */
    public void setCuratorSession(String pubID, boolean experimentVisibility) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            profileRep.setCuratorSession(pubID, CuratorSession.Attribute.SHOW_EXPERIMENT_SECTION, experimentVisibility);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    /**
     * Pass in an experiment DTO and an ExpressionExperiment, either an existing one to update
     * or a new instance.
     *
     * @param experimentDTO        dto
     * @param expressionExperiment expression experiment on which the changes are applied.
     */
    public static void populateExpressionExperiment(ExperimentDTO experimentDTO, ExpressionExperiment expressionExperiment) {
        ExpressionRepository expressionRep = RepositoryFactory.getExpressionRepository();
        // update assay: never null
        ExpressionAssay newAssay = expressionRep.getAssayByName(experimentDTO.getAssay());
        expressionExperiment.setAssay(newAssay);
        experimentDTO.setAssayAbbreviation(newAssay.getAbbreviation());
        // update db link: could be null
        String genbankID = experimentDTO.getGenbankID();
        if (!StringUtils.isEmpty(genbankID)) {
            MarkerDBLink dbLink = expressionRep.getMarkDBLink(genbankID);
            expressionExperiment.setMarkerDBLink(dbLink);
            Marker marker = dbLink.getMarker();
            // If the genbank number is an EST or a cDNA then persist their id in the clone column
            if (marker.getType().equals(Marker.Type.EST) ||
                    marker.getType().equals(Marker.Type.CDNA)) {
                // TOdo: Change to setClone(clone) when clone is subclassed from Marker
                expressionExperiment.setCloneID(marker.getZdbID());
            }
        } else {
            expressionExperiment.setMarkerDBLink(null);
        }
        // update Environment (=experiment)
        GenotypeExperiment genox = expressionRep.getGenotypeExperimentByExperimentIDAndGenotype(experimentDTO.getEnvironmentID(), experimentDTO.getFishID());
        LOG.info("Finding Genotype Experiment for :" + experimentDTO.getEnvironmentID() + ", " + experimentDTO.getFishID());
        // if no genotype experiment found create a new one.
        if (genox == null) {
            genox = expressionRep.createGenoteypExperiment(experimentDTO.getEnvironmentID(), experimentDTO.getFishID());
            LOG.info("Created Genotype Experiment :" + genox.getZdbID());
        }
        expressionExperiment.setGenotypeExperiment(genox);
        // update antibody
        String antibodyID = experimentDTO.getAntibodyID();
        if (!StringUtils.isEmpty(antibodyID)) {
            AntibodyRepository antibodyRep = RepositoryFactory.getAntibodyRepository();
            Antibody antibody = antibodyRep.getAntibodyByID(antibodyID);
            expressionExperiment.setAntibody(antibody);
        } else {
            expressionExperiment.setAntibody(null);
        }
        // update gene
        String geneID = experimentDTO.getGeneZdbID();
        if (!StringUtils.isEmpty(geneID)) {
            MarkerRepository antibodyRep = RepositoryFactory.getMarkerRepository();
            Marker gene = antibodyRep.getMarkerByID(geneID);
            expressionExperiment.setMarker(gene);
            experimentDTO.setGeneName(gene.getAbbreviation());
        } else {
            expressionExperiment.setMarker(null);
        }

        Publication pub = pubRepository.getPublication(experimentDTO.getPublicationID());
        expressionExperiment.setPublication(pub);
    }

    public List<String> readFigures(String publicationID) {
        return pubRepository.getDistinctFigureLabels(publicationID);
    }
}