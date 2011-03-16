package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
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
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * GWT controller for the Pheno tab page
 */
public class CurationPhenotypeRPCImpl extends RemoteServiceServlet implements CurationPhenotypeRPC {

    private static final Logger logger = Logger.getLogger(CurationPhenotypeRPCImpl.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    public List<PhenotypeFigureStageDTO> getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID) {

        List<MutantFigureStage> phenotypes = getPhenotypeRepository().getMutantExpressionsByFigureFish(experimentFilter.getPublicationID(),
                figureID, experimentFilter.getFishID(), experimentFilter.getFeatureID());
        if (phenotypes == null)
            return null;
        List<PhenotypeFigureStageDTO> dtos = new ArrayList<PhenotypeFigureStageDTO>(30);
        for (MutantFigureStage efs : phenotypes) {
            PhenotypeFigureStageDTO dto = new PhenotypeFigureStageDTO();
            dto.setFigure(DTOConversionService.convertToFigureDTO(efs.getFigure()));
            dto.setStart(DTOConversionService.convertToStageDTO(efs.getStart()));
            dto.setEnd((DTOConversionService.convertToStageDTO(efs.getEnd())));
            List<PhenotypeTermDTO> termStrings = new ArrayList<PhenotypeTermDTO>(5);
            for (Phenotype phenotype : efs.getPhenotypes()) {
                PhenotypeTermDTO termDto = new PhenotypeTermDTO();
                termDto.setSuperterm(DTOConversionService.convertToTermDTO(phenotype.getSuperterm()));
                if (phenotype.getSubterm() != null) {
                    termDto.setSubterm(DTOConversionService.convertToTermDTO(phenotype.getSubterm()));
                }
                termDto.setQuality(DTOConversionService.convertToTermDTO(phenotype.getQualityTerm()));
                termDto.setTag(phenotype.getTag());
                termDto.setZdbID(phenotype.getZdbID());
                termStrings.add(termDto);

            }
            //dto.setExpressedIn(formatter.getFormattedString());
            Collections.sort(termStrings);
            dto.setExpressedTerms(termStrings);
            GenotypeDTO genotype = DTOConversionService.convertToGenotypeDTO(efs.getGenotypeExperiment().getGenotype());
            dto.setGenotype(genotype);
            dto.setEnvironment(DTOConversionService.convertToEnvironmentDTO(efs.getGenotypeExperiment().getExperiment()));
            dto.setPublicationID(experimentFilter.getPublicationID());
            dtos.add(dto);
        }
        Collections.sort(dtos);
        return dtos;
    }

    public List<PhenotypeFigureStageDTO> createMutantFigureStages(List<PhenotypeFigureStageDTO> newFigureAnnotations) {
        if (newFigureAnnotations == null)
            return null;

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        List<PhenotypeFigureStageDTO> returnRecords = new ArrayList<PhenotypeFigureStageDTO>(newFigureAnnotations.size());
        try {
            tx = session.beginTransaction();
            for (PhenotypeFigureStageDTO pfs : newFigureAnnotations) {
                MutantFigureStage mfs = createMutantFigureStageWithDefaultPhenotype(pfs);
                returnRecords.add(DTOConversionService.convertToPhenotypeFigureStageDTO(mfs));
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
     * AO:unspecified - quality [abnormal]. If the phenotype exists already for a figure reuse it.
     *
     * @param mutantFigureStage Mutant Figure Stage
     */
    private MutantFigureStage createMutantFigureStageWithDefaultPhenotype(PhenotypeFigureStageDTO mutantFigureStage) {
        MutantFigureStage mfs = DTOConversionService.convertToMutantFigureStageFromDTO(mutantFigureStage);
        // create a new genotype experiment if needed
        GenotypeExperiment genotypeExperiment = mfs.getGenotypeExperiment();
        if (genotypeExperiment == null) {
            genotypeExperiment =
                    getExpressionRepository().createGenoteypExperiment(mutantFigureStage.getEnvironment().getZdbID(), mutantFigureStage.getGenotype().getZdbID());
            mfs.setGenotypeExperiment(genotypeExperiment);
        }

        // creating new mutant record so far...
        // create the default phenotype
        // check if default phenotype exists
        Phenotype defaultPhenotype = PhenotypeService.getDefaultPhenotype(mfs);
        if (defaultPhenotype == null) {
            getPhenotypeRepository().createDefaultPhenotype(mfs);
        } else {
            defaultPhenotype.addFigure(mfs.getFigure());
        }
        return mfs;
    }

    public void deleteFigureAnnotation(PhenotypeFigureStageDTO figureAnnotation) {
        if (figureAnnotation == null)
            return;
        if (figureAnnotation.getFigure().getZdbID() == null ||
                figureAnnotation.getStart().getZdbID() == null ||
                figureAnnotation.getEnd().getZdbID() == null)
            return;
        MutantFigureStage mutantFromDto = DTOConversionService.convertToMutantFigureStageFromDTO(figureAnnotation);
        if (mutantFromDto == null) {
            logger.info("No mutant figure stage record found: " + figureAnnotation);
            return;
        }
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            getMutantRepository().deleteMutantFigureStage(mutantFromDto);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List<PhenotypeFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> updateEntity) {
        List<PhenotypeFigureStageDTO> mutantsToBeAnnotated = updateEntity.getFigureAnnotations();
        if (mutantsToBeAnnotated == null)
            return null;

        List<PileStructureAnnotationDTO> pileStructures = updateEntity.getStructures();
        if (pileStructures == null || pileStructures.isEmpty())
            return null;


        List<PhenotypeFigureStageDTO> updatedAnnotations = new ArrayList<PhenotypeFigureStageDTO>(5);
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            // for each figure annotation check which structures need to be added or removed
            for (PhenotypeFigureStageDTO dto : mutantsToBeAnnotated) {
                MutantFigureStage filterMutantFigureStage = DTOConversionService.convertToMutantFigureStageFilter(dto);
                MutantFigureStage mutant = getPhenotypeRepository().getMutant(filterMutantFigureStage, dto.getFigure().getZdbID());
                // ToDo: handle case mutant is not found: throw exception (make sure a finally clause is added!
                for (PileStructureAnnotationDTO pileStructure : pileStructures) {
                    PhenotypeStructure phenotypePileStructure = getPhenotypeRepository().getPhenotypePileStructure(pileStructure.getZdbID());
                    // ToDo: handle case pile structure is not found: throw exception (make sure a finally clause is added!
                    if (phenotypePileStructure == null)
                        logger.error("Could not find pile structure " + pileStructure.getZdbID());
                    // add phenotype if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.ADD) {
                        PhenotypeTermDTO phenotypeTermDTO = addExpressionToAnnotation(mutant, phenotypePileStructure);
                        if (phenotypeTermDTO != null) {
                            dto.addExpressedTerm(phenotypeTermDTO);
                            updatedAnnotations.add(dto);
                        }
                    }
                    // remove expression if marked as such
                    if (pileStructure.getAction() == PileStructureAnnotationDTO.Action.REMOVE) {
                        removePhenotypeStructureFromMutant(phenotypePileStructure, mutant);
                    }
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        for (PhenotypeFigureStageDTO dto : mutantsToBeAnnotated) {
            //setFigureAnnotationStatus(dto, false);
        }
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
        Collection<MutantFigureStage> mutants = getPhenotypeRepository().getMutantExpressionsByFigureFish(publicationID, null, null, null);
        Collection<PhenotypeStructure> structures = getPhenotypeRepository().retrievePhenotypeStructures(publicationID);
        return CollectionUtils.isNotEmpty(mutants) && CollectionUtils.isEmpty(structures);
    }

    /**
     * Delete a phenotype pile structure from a given mutant record.
     *
     * @param phenotypePileStructure phenotype pile structure
     * @param mutantFigureStage      mutant figure stage record
     */
    private void removePhenotypeStructureFromMutant(PhenotypeStructure phenotypePileStructure, MutantFigureStage mutantFigureStage) {
        for (Phenotype phenotype : mutantFigureStage.getPhenotypes()) {

            if (phenotype.getSuperterm().getZdbID().equals(phenotypePileStructure.getSuperterm().getZdbID())) {
                // if quality term is not the same continue with next phenotype
                if (!phenotype.getQualityTerm().getZdbID().equals(phenotypePileStructure.getQualityTerm().getZdbID()))
                    continue;
                // if tag is not the same continue with next phenotype
                if (!phenotype.getTag().equals(phenotypePileStructure.getTag().toString()))
                    continue;

                String subtermID = null;
                GenericTerm term = phenotype.getSubterm();
                if (term != null)
                    subtermID = term.getOboID();
                // check if both subterms are null. If not ignore this phenotype as it is not the one
                // we are asked to remove.
                if (subtermID == null && phenotypePileStructure.getSubterm() == null) {
                    getPhenotypeRepository().deletePhenotype(phenotype, mutantFigureStage.getFigure());
                    logger.info("Removed Phenotype:  " + phenotype.getSuperterm().getTermName());
                    break;
                }
                if (subtermID != null && phenotypePileStructure.getSubterm() != null &&
                        term.getZdbID().equals(phenotypePileStructure.getSubterm().getZdbID())) {
                    getPhenotypeRepository().deletePhenotype(phenotype, mutantFigureStage.getFigure());
                    GenericTerm subterm = phenotype.getSubterm();
                    logger.info("Removed Phenotype:  " + phenotype.getSuperterm().getTermName() + " : " + subterm.getTermName());
                    break;
                }
            }
        }
    }

    /**
     * Add Expression record to an experiment.
     * expression is not added if it already exists for the given experiment including
     * the expressed-modifier. We allow to add the same composed term twice, once with
     * 'not'  modifier and once without it.
     *
     * @param mutant             ExpressionExperiment
     * @param phenotypeStructure structure selected from the pile
     * @return expressedTermDTO used to return to RPC caller
     */
    private PhenotypeTermDTO addExpressionToAnnotation(MutantFigureStage mutant,
                                                       PhenotypeStructure phenotypeStructure) {
        // do nothing term already exists for mutant
        if (hasMutantGivenPhenotypeStructure(mutant, phenotypeStructure))
            return null;

        // create a new Phenotype record
        // create a new PhenotypeTermDTO object that is passed back to the RPC caller
        PhenotypeTermDTO expressedTerm = new PhenotypeTermDTO();
        GenericTerm superterm = phenotypeStructure.getSuperterm();
        GenericTerm subterm = phenotypeStructure.getSubterm();
        Phenotype aoPhenotype = new Phenotype();
        aoPhenotype.setSuperterm(ontologyRepository.getTermByZdbID(superterm.getZdbID()));
        if (subterm != null){
            aoPhenotype.setSubterm(ontologyRepository.getTermByZdbID(subterm.getZdbID()));
        }
        aoPhenotype.addFigure(mutant.getFigure());
        aoPhenotype.setEndStage(mutant.getEnd());
        aoPhenotype.setStartStage(mutant.getStart());
        aoPhenotype.setGenotypeExperiment(mutant.getGenotypeExperiment());
        aoPhenotype.setPublication(mutant.getPublication());
        aoPhenotype.setTag(phenotypeStructure.getTag().toString());
        aoPhenotype.setQualityTerm(phenotypeStructure.getQualityTerm());
        getPhenotypeRepository().createPhenotype(aoPhenotype, mutant.getFigure());

        TermDTO supertermDto = DTOConversionService.convertToTermDTO(phenotypeStructure.getSuperterm());
        expressedTerm.setSuperterm(supertermDto);
        if (subterm != null) {
            TermDTO subtermDto = DTOConversionService.convertToTermDTO(phenotypeStructure.getSubterm());
            expressedTerm.setSubterm(subtermDto);
        }
        return expressedTerm;
    }

    private boolean hasMutantGivenPhenotypeStructure(MutantFigureStage mutant, PhenotypeStructure pileStructure) {
        boolean foundStructure = false;
        for (Phenotype phenotype : mutant.getPhenotypes()) {
            if (phenotype.getSuperterm().getZdbID().equals(pileStructure.getSuperterm().getZdbID())) {
                // check if subterms are unequal
                // if so continue loop checking for an existing phenotype
                GenericTerm phenoSubterm = HibernateUtil.initializeAndUnproxy(phenotype.getSubterm());
                if ((phenoSubterm == null && pileStructure.getSubterm() != null) ||
                        (phenoSubterm != null && pileStructure.getSubterm() == null)) {
                    continue;
                }

                if (phenoSubterm != null && pileStructure.getSubterm() != null) {
                    String phenotypeID = phenoSubterm.getZdbID();
                    String pileStructureID = pileStructure.getSubterm().getZdbID();
                    if (!phenotypeID.equals(pileStructureID)) {
                        // terms are different in the sub term
                        continue;
                    }
                }

                if (phenotype.getQualityTerm().getOboID().equals(pileStructure.getQualityTerm().getOboID())) {
                    if (phenotype.getTag().equals(pileStructure.getTag().toString())) {
                        foundStructure = true;
                        break;
                    }
                }
            }
        }
        return foundStructure;
    }

}
