package org.zfin.gwt.curation.server;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.curation.ui.CurationPhenotypeRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.PhenotypeExperiment;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.LoggingUtil;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * GWT controller for the Pheno tab page
 */
public class CurationPhenotypeRPCImpl extends ZfinRemoteServiceServlet implements CurationPhenotypeRPC {

    private static final Logger logger = Logger.getLogger(CurationPhenotypeRPCImpl.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    public List<PhenotypeExperimentDTO> getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID) {

        List<PhenotypeExperiment> phenotypes = getPhenotypeRepository().getMutantExpressionsByFigureFish(experimentFilter.getPublicationID(),
                figureID, experimentFilter.getFishID(), experimentFilter.getFeatureID());
        if (phenotypes == null)
            return null;
        List<PhenotypeExperimentDTO> dtos = new ArrayList<PhenotypeExperimentDTO>(30);
        for (PhenotypeExperiment phenotypeExperiment : phenotypes) {
            PhenotypeExperimentDTO dto = new PhenotypeExperimentDTO();
            dto.setId(phenotypeExperiment.getId());
            dto.setFigure(DTOConversionService.convertToFigureDTO(phenotypeExperiment.getFigure()));
            dto.setStart(DTOConversionService.convertToStageDTO(phenotypeExperiment.getStartStage()));
            dto.setEnd((DTOConversionService.convertToStageDTO(phenotypeExperiment.getEndStage())));
            List<PhenotypeStatementDTO> termStrings = new ArrayList<PhenotypeStatementDTO>(5);
            for (PhenotypeStatement phenotype : phenotypeExperiment.getPhenotypeStatements()) {
                PhenotypeStatementDTO termDto = new PhenotypeStatementDTO();
                termDto.setEntity(DTOConversionService.convertToEntityDTO(phenotype.getEntity()));
                termDto.setRelatedEntity(DTOConversionService.convertToEntityDTO(phenotype.getRelatedEntity()));
                termDto.setQuality(DTOConversionService.convertToTermDTO(phenotype.getQuality()));
                termDto.setTag(phenotype.getTag());
                termDto.setId(phenotype.getId());
                termStrings.add(termDto);

            }
            //dto.setExpressedIn(formatter.getFormattedString());
            Collections.sort(termStrings);
            dto.setExpressedTerms(termStrings);
            GenotypeDTO genotype = DTOConversionService.convertToGenotypeDTO(phenotypeExperiment.getGenotypeExperiment().getGenotype());
            dto.setGenotype(genotype);
            dto.setEnvironment(DTOConversionService.convertToEnvironmentDTO(phenotypeExperiment.getGenotypeExperiment().getExperiment()));
            dto.setPublicationID(experimentFilter.getPublicationID());
            dtos.add(dto);
        }
        Collections.sort(dtos);
        return dtos;
    }

    public List<PhenotypeExperimentDTO> createPhenotypeExperiments(List<PhenotypeExperimentDTO> newFigureAnnotations) {
        if (newFigureAnnotations == null)
            return null;

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        List<PhenotypeExperimentDTO> returnRecords = new ArrayList<PhenotypeExperimentDTO>(newFigureAnnotations.size());
        try {
            tx = session.beginTransaction();
            for (PhenotypeExperimentDTO pfs : newFigureAnnotations) {
                PhenotypeExperiment phenoExperiment = createPhenotypeExperiment(pfs);
                returnRecords.add(DTOConversionService.convertToPhenotypeFigureStageDTO(phenoExperiment));
            }
            tx.commit();
        } catch (HibernateException e) {
            logger.error("failed to create mutant figure stages",e);
            tx.rollback();
        }
        return returnRecords;
    }

    /**
     * This creates a mutant figure stage record with a default phenotype, i.e.
     * AO:unspecified - quality [abnormal].
     *
     * @param mutantFigureStage Mutant Figure Stage
     * @return PhenotypeExperiment
     */
    private PhenotypeExperiment createPhenotypeExperiment(PhenotypeExperimentDTO mutantFigureStage) {
        PhenotypeExperiment phenoExperiment = DTOConversionService.convertToPhenotypeExperimentFilter(mutantFigureStage);
        // check if there is a genotypes experiment already.
        // if not create a new one.
        GenotypeExperiment genotypeExperiment = getExpressionRepository().getGenotypeExperimentByExperimentIDAndGenotype(
                mutantFigureStage.getEnvironment().getZdbID(), mutantFigureStage.getGenotype().getZdbID());
        // create a new genotype experiment if needed
        if (genotypeExperiment == null) {
            genotypeExperiment =
                    getExpressionRepository().createGenoteypExperiment(mutantFigureStage.getEnvironment().getZdbID(), mutantFigureStage.getGenotype().getZdbID());
        }
        phenoExperiment.setGenotypeExperiment(genotypeExperiment);
        getPhenotypeRepository().createPhenotypeExperiment(phenoExperiment);
        return phenoExperiment;
    }

    public void deleteFigureAnnotation(PhenotypeExperimentDTO figureAnnotation) {
        if (figureAnnotation == null)
            return;
        if (figureAnnotation.getId() == 0)
            return;
        PhenotypeExperiment phenotypeExperiment = getPhenotypeRepository().getPhenotypeExperiment(figureAnnotation.getId());
        if (phenotypeExperiment == null) {
            logger.info("No phenotype experiment record found for: " + figureAnnotation);
            return;
        }
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            getMutantRepository().deletePhenotypeExperiment(phenotypeExperiment);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List<PhenotypeExperimentDTO> updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeExperimentDTO> updateEntity) {
        LoggingUtil loggingUtil = new LoggingUtil(logger);
        List<PhenotypeExperimentDTO> mutantsToBeAnnotated = updateEntity.getFigureAnnotations();
        if (mutantsToBeAnnotated == null)
            return null;

        List<PileStructureAnnotationDTO> pileStructures = updateEntity.getStructures();
        if (pileStructures == null || pileStructures.isEmpty())
            return null;

        // Collect all phenotype experiments being used
        Set<PhenotypeExperiment> phenotypeExperiments = new HashSet<PhenotypeExperiment>();
        List<PhenotypeExperimentDTO> updatedAnnotations = new ArrayList<PhenotypeExperimentDTO>(5);
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            // for each figure annotation check which structures need to be added or removed
            for (PhenotypeExperimentDTO dto : mutantsToBeAnnotated) {
                PhenotypeExperiment filterMutantFigureStage = DTOConversionService.convertToPhenotypeExperimentFilter(dto);
                PhenotypeExperiment phenoExperiment = getPhenotypeRepository().getPhenotypeExperiment(filterMutantFigureStage);
                phenotypeExperiments.add(phenoExperiment);
                // ToDo: handle case mutant is not found: throw exception (make sure a finally clause is added!
                for (PileStructureAnnotationDTO pileStructure : pileStructures) {
                    PhenotypeStructure phenotypePileStructure = getPhenotypeRepository().getPhenotypePileStructure(pileStructure.getZdbID());
                    if (phenotypePileStructure == null) {
                        logger.error("Could not find pile structure " + pileStructure.getZdbID());
                        continue;
                    }
                    // add phenotype if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.ADD) {
                        PhenotypeStatementDTO phenotypeTermDTO = addExpressionToAnnotation(phenoExperiment, phenotypePileStructure);
                        if (phenotypeTermDTO != null) {
                            dto.addExpressedTerm(phenotypeTermDTO);
                            updatedAnnotations.add(dto);
                        }
                    }
                    // remove expression if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.REMOVE) {
                        removePhenotypeStructureFromMutant(phenotypePileStructure, phenoExperiment);
                    }
                }
            }
            for(PhenotypeExperiment phenotypeExperiment: phenotypeExperiments){
                getPhenotypeRepository().runRegenGenotypeFigureScript(phenotypeExperiment);
            }
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        loggingUtil.logDuration("Duration of updateStructuresForExpression() method: ");
        return updatedAnnotations;
    }

    /**
     * Checks if the phenotype structure pile needs to be recreated.
     * Yes, if
     * 1) publication is open
     * 2) one or more mutant records with non-unspecified structures exist
     * 3) no phenotype structure on the pile
     *
     * @param publicationID publication
     */
    public boolean isReCreatePhenotypePileLinkNeeded(String publicationID) {
        Publication publication = getPublicationRepository().getPublication(publicationID);
        if (publication.getCloseDate() != null)
            return false;
        Collection<PhenotypeExperiment> mutants = getPhenotypeRepository().getMutantExpressionsByFigureFish(publicationID, null, null, null);
        Collection<PhenotypeStructure> structures = getPhenotypeRepository().retrievePhenotypeStructures(publicationID);
        return CollectionUtils.isNotEmpty(mutants) && CollectionUtils.isEmpty(structures);
    }

    /**
     * Delete a phenotype pile structure from a given mutant record.
     *
     * @param pileStructure   phenotype pile structure
     * @param phenoExperiment mutant figure stage record
     */
    private void removePhenotypeStructureFromMutant(PhenotypeStructure pileStructure, PhenotypeExperiment phenoExperiment) {
        for (PhenotypeStatement phenoStatement : phenoExperiment.getPhenotypeStatements()) {
            PhenotypeStructure comparisonPhenotypeStructure = DTOConversionService.convertToPhenotypeStructure(phenoStatement);
            if (comparisonPhenotypeStructure.equals(pileStructure)) {
                getPhenotypeRepository().deletePhenotypeStatement(phenoStatement);
                logger.info("Removed phenotype statement: " + phenoStatement.getDisplayName());
            }
        }
    }

    /**
     * Add Expression record to an experiment.
     * expression is not added if it already exists for the given experiment including
     * the expressed-modifier. We allow to add the same composed term twice, once with
     * 'not'  modifier and once without it.
     *
     * @param phenoExperiment    PhenotypeExperiment
     * @param phenotypeStructure structure selected from the pile
     * @return expressedTermDTO used to return to RPC caller
     */
    private PhenotypeStatementDTO addExpressionToAnnotation(PhenotypeExperiment phenoExperiment,
                                                       PhenotypeStructure phenotypeStructure) {
        // do nothing term already exists for mutant
        if (hasMutantGivenPhenotypeStructure(phenoExperiment, phenotypeStructure))
            return null;

        // create a new Phenotype Statement record
        // create a new PhenotypeTermDTO object that is passed back to the RPC caller
        PhenotypeStatementDTO phenotypeTermDTO = DTOConversionService.convertToPhenotypeTermDTO(phenotypeStructure);
        PhenotypeStatement phenoStatement = phenotypeStructure.getPhenotypeStatement();
        phenoStatement.setPhenotypeExperiment(phenoExperiment);
        getPhenotypeRepository().createPhenotypeStatement(phenoStatement);
        logger.info("Added phenotype statement: " + phenoStatement.getDisplayName());
        return phenotypeTermDTO;
    }

    private boolean hasMutantGivenPhenotypeStructure(PhenotypeExperiment mutant, PhenotypeStructure pileStructure) {
        if (mutant == null || pileStructure == null)
            return false;

        for (PhenotypeStatement phenotype : mutant.getPhenotypeStatements()) {
            PhenotypeStructure comparisonPhenotypeStructure = DTOConversionService.convertToPhenotypeStructure(phenotype);
            if (comparisonPhenotypeStructure.equals(pileStructure))
                return true;
        }
        return false;
    }

}
