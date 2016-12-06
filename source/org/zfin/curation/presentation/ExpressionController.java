package org.zfin.curation.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.*;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.ExpressionCurationService;
import org.zfin.gwt.curation.ui.PublicationNotFoundException;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Controller
@RequestMapping("/curation")
public class ExpressionController implements ExpressionCurationService {

    @Autowired
    private ExpressionRepository expRepository;
    @Autowired
    private PublicationRepository pubRepository;
    @Autowired
    private CurationController curationController;


    private final static Logger LOG = RootLogger.getLogger(ExpressionController.class);


    /**
     * Create a new expression experiment
     *
     * @param experimentDTO String
     */
    @RequestMapping(value = "/{publicationID}/expression-experiments", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ExpressionExperimentDTO createExpressionExperiment(@PathVariable String publicationID,
                                                              @RequestBody ExpressionExperimentDTO experimentDTO) {
        if (experimentDTO == null)
            return null;

        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment2 expressionExperiment = new ExpressionExperiment2();
            populateExpressionExperiment(experimentDTO, expressionExperiment);

            expRepository.createExpressionExperiment(expressionExperiment);
            experimentDTO.setExperimentZdbID(expressionExperiment.getZdbID());
            getInfrastructureRepository().insertRecordAttribution(expressionExperiment.getFishExperiment().getFish(), expressionExperiment.getPublication());
            tx.commit();
        } catch (ConstraintViolationException e) {
            tx.rollback();
            throw new RuntimeException("Experiment already exist. Experiments have to be unique.");
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return experimentDTO;
    }

    /**
     * Pass in an experiment DTO and an ExpressionExperiment, either an existing one to update
     * or a new instance.
     *
     * @param experimentDTO        dto
     * @param expressionExperiment expression experiment on which the changes are applied.
     */
    public void populateExpressionExperiment(ExpressionExperimentDTO experimentDTO, ExpressionExperiment2 expressionExperiment) {
        // update assay: never null
        ExpressionAssay newAssay = expRepository.getAssayByName(experimentDTO.getAssay());
        expressionExperiment.setAssay(newAssay);
        experimentDTO.setAssayAbbreviation(newAssay.getAbbreviation());
        // update db link: could be null
        String genbankID = experimentDTO.getGenbankID();
        if (!StringUtils.isEmpty(genbankID)) {
            MarkerDBLink dbLink = expRepository.getMarkDBLink(genbankID);
            expressionExperiment.setMarkerDBLink(dbLink);
            Marker marker = dbLink.getMarker();
            // If the genbank number is an EST or a cDNA then persist their id in the clone column
            if (marker.getType().equals(Marker.Type.EST) ||
                    marker.getType().equals(Marker.Type.CDNA)) {
                // TOdo: Change to setClone(clone) when clone is subclassed from Marker
                Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(marker.getZdbID());
                expressionExperiment.setProbe(clone);
            }
        } else {
            expressionExperiment.setMarkerDBLink(null);
        }
        // update Environment (=experiment)
        FishExperiment genox = expRepository.getFishExperimentByExperimentIDAndFishID(experimentDTO.getEnvironment().getZdbID(), experimentDTO.getFishID());
        LOG.info("Finding Genotype Experiment for :" + experimentDTO.getEnvironment().getZdbID() + ", " + experimentDTO.getFishID());
        // if no genotype experiment found create a new one.
        if (genox == null) {
            genox = expRepository.createFishExperiment(experimentDTO.getEnvironment().getZdbID(), experimentDTO.getFishID());
            LOG.info("Created Genotype Experiment :" + genox.getZdbID());
        }
        expressionExperiment.setFishExperiment(genox);
        // update antibody
        MarkerDTO antibodyDTO = experimentDTO.getAntibodyMarker();
        if (antibodyDTO != null) {
            AntibodyRepository antibodyRep = RepositoryFactory.getAntibodyRepository();
            Antibody antibody = antibodyRep.getAntibodyByID(antibodyDTO.getZdbID());
            expressionExperiment.setAntibody(antibody);
        } else {
            expressionExperiment.setAntibody(null);
        }
        // update gene
        MarkerDTO geneDto = experimentDTO.getGene();
        if (geneDto != null && geneDto.getZdbID() != null) {
            MarkerRepository antibodyRep = RepositoryFactory.getMarkerRepository();
            Marker gene = antibodyRep.getMarkerByID(geneDto.getZdbID());
            expressionExperiment.setGene(gene);
            experimentDTO.setGene(DTOConversionService.convertToMarkerDTO(gene));
        } else {
            expressionExperiment.setGene(null);
        }

        Publication pub = pubRepository.getPublication(experimentDTO.getPublicationID());
        expressionExperiment.setPublication(pub);
    }


    @RequestMapping(value = "/{publicationID}/experiments", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ExperimentDTO> getExperiments(@PathVariable String publicationID) {
        List<Experiment> experiments = pubRepository.getExperimentsByPublication(publicationID);
        List<ExperimentDTO> dtoList = new ArrayList<>();
        for (Experiment experiment : experiments) {
            ExperimentDTO env = DTOConversionService.convertToExperimentDTO(experiment);
            dtoList.add(env);
        }
        return dtoList;
    }


    /**
     * Retrieve all experiments for a given publication.
     *
     * @param publicationID publication
     * @return list of experiments
     */
    @RequestMapping(value = "/{publicationID}/expression-experiments", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ExpressionExperimentDTO> getExpressionExperiments(@PathVariable String publicationID) {
        List<ExpressionExperiment> experiments = expRepository.getExperiments(publicationID);
        if (experiments == null)
            return null;

        List<ExpressionExperimentDTO> dtos = new ArrayList<>();
        for (ExpressionExperiment experiment : experiments) {
            ExpressionExperimentDTO experimentDTO = new ExpressionExperimentDTO();
            experimentDTO.setExperimentZdbID(experiment.getZdbID());
            Marker gene = experiment.getGene();
            if (gene != null) {
                experimentDTO.setGene(DTOConversionService.convertToMarkerDTO(gene));
                if (experiment.getMarkerDBLink() != null && experiment.getMarkerDBLink().getAccessionNumber() != null) {
                    String dblink = experiment.getMarkerDBLink().getAccessionNumber();
                    experimentDTO.setGenbankNumber(dblink);
                    experimentDTO.setGenbankID(experiment.getMarkerDBLink().getZdbID());
                }
            }
            if (experiment.getAntibody() != null) {
                experimentDTO.setAntibodyMarker(DTOConversionService.convertToMarkerDTO(experiment.getAntibody()));
            }
            experimentDTO.setFishName(experiment.getFishExperiment().getFish().getHandle());
            experimentDTO.setFishID(experiment.getFishExperiment().getFish().getZdbID());
            experimentDTO.setEnvironment(DTOConversionService.convertToExperimentDTO(experiment.getFishExperiment().getExperiment()));
            experimentDTO.setAssay(experiment.getAssay().getName());
            // check if there are expressions associated
            Set<ExpressionResult> expressionResults = experiment.getExpressionResults();
            if (expressionResults != null)
                experimentDTO.setNumberOfExpressions(experiment.getDistinctExpressions());

            dtos.add(experimentDTO);
        }
        return dtos;
    }

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     */
    @RequestMapping(value = "/{publicationID}/{antibodyID}/genes", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<MarkerDTO> getGenesByAntibody(@PathVariable String publicationID,
                                              @PathVariable String antibodyID) throws PublicationNotFoundException {
        if (StringUtils.isEmpty(antibodyID))
            return curationController.getGenes(publicationID);

        List<Marker> antibodies = pubRepository.getGenesByAntibody(publicationID, antibodyID);
        return curationController.getListOfMarkerDtos(antibodies);
    }

    /**
     * Retrieve the accession numbers for a given gene
     *
     * @param geneID string
     */
    @RequestMapping(value = "/{publicationID}/{geneID}/genbank-accessions", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ExpressionExperimentDTO> getGenbankAccessions(@PathVariable String publicationID,
                                                              @PathVariable String geneID) {
        List<MarkerDBLink> geneDBLinks = pubRepository.getDBLinksByGene(publicationID, geneID);
        List<ExpressionExperimentDTO> accessionNumbers = new ArrayList<>();
        for (MarkerDBLink geneDBLink : geneDBLinks) {
            ExpressionExperimentDTO accession = new ExpressionExperimentDTO();
            accession.setGenbankNumber(geneDBLink.getAccessionNumber());
            accession.setGenbankID(geneDBLink.getZdbID());
            accessionNumbers.add(accession);
        }
        List<MarkerDBLink> cloneDBLinks = pubRepository.getDBLinksForCloneByGene(publicationID, geneID);
        for (MarkerDBLink cloneDBLink : cloneDBLinks) {
            ExpressionExperimentDTO accession = new ExpressionExperimentDTO();
            accession.setGenbankNumber(cloneDBLink.getAccessionNumber() + " [" + cloneDBLink.getMarker().getType().toString() + "]");
            accession.setGenbankID(cloneDBLink.getZdbID());
            accessionNumbers.add(accession);
        }
        return accessionNumbers;
    }

    @RequestMapping(value = "/{publicationID}/{geneID}/antibodies", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<MarkerDTO> getAntibodiesByGene(@PathVariable String publicationID,
                                               @PathVariable String geneID) {
        if (StringUtils.isEmpty(geneID))
            return curationController.getAntibodies(publicationID);

        List<Antibody> antibodies = pubRepository.getAntibodiesByPublicationAndGene(publicationID, geneID);
        List<MarkerDTO> markers = curationController.getListOfMarkerDtos(antibodies);
        return markers;
    }


    /**
     * Delete a given experiment.
     *
     * @param expressionExperimentID experiment id
     */
    @RequestMapping(value = "/{publicationID}/expression-experiments/{expressionExperimentID}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public void deleteExperiment(@PathVariable String publicationID,
                                 @PathVariable String expressionExperimentID) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionExperiment2 experiment = expRepository.getExpressionExperiment2(expressionExperimentID);
            expRepository.deleteExpressionExperiment(experiment);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Delete", e);
            tx.rollback();
            throw e;
        }
    }


}
