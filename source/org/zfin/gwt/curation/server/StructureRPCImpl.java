package org.zfin.gwt.curation.server;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.ExpressionStructure;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.PileStructureExistsException;
import org.zfin.gwt.curation.ui.PileStructuresRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.gwt.root.server.rpc.ZfinRemoteServiceServlet;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Subset;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * GWT Class to facilitate pile structure activities, such as create, retrieve and delete.
 */
public class StructureRPCImpl extends ZfinRemoteServiceServlet implements PileStructuresRPC {

    private static final Logger LOG = RootLogger.getLogger(StructureRPCImpl.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    /**
     * Retrieve all phenotype structures on the structure pile.
     *
     * @param publicationID Publication ID
     * @return list fo structure objects
     */
    public List<PhenotypePileStructureDTO> getPhenotypePileStructures(String publicationID) {
        List<PhenotypeStructure> structures = getPhenotypeRepository().retrievePhenotypeStructures(publicationID);

        if (structures == null)
            return null;

        Collections.sort(structures);
        List<PhenotypePileStructureDTO> phenotypePileStructures = new ArrayList<PhenotypePileStructureDTO>(structures.size());
        for (PhenotypeStructure structure : structures) {
            PhenotypePileStructureDTO dto = DTOConversionService.convertToPhenotypePileStructureDTO(structure);
            phenotypePileStructures.add(dto);
        }
        return phenotypePileStructures;
    }

    /**
     * Remove a phenotype structure from the pile.
     *
     * @param structure PhenotypePileStructureDTO
     */
    public PhenotypePileStructureDTO deletePhenotypeStructure(PhenotypePileStructureDTO structure) {
        if (structure == null)
            return null;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            getPhenotypeRepository().deletePhenotypeStructureById(structure.getZdbID());
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not delete phenotype structure '" + structure.getZdbID() + "'", e);
            tx.rollback();
            throw e;
        }
        return structure;
    }

    /**
     * Create a new structure for the pile.
     *
     * @param expressedTerm Expressed Term dto
     * @param publicationID pub id
     */
    public ExpressionPileStructureDTO createPileStructure(ExpressedTermDTO expressedTerm, String publicationID)
            throws PileStructureExistsException, TermNotFoundException {
        if (expressedTerm == null || publicationID == null)
            throw new TermNotFoundException("No Term or publication provided");


        LOG.info("Request: Create Composed term: " + expressedTerm.getDisplayName());
        if (getExpressionRepository().pileStructureExists(expressedTerm, publicationID)) {
            PileStructureExistsException exception = new PileStructureExistsException(expressedTerm);
            LOG.info(exception.getMessage());
            throw exception;
        }
        ExpressionPileStructureDTO pileStructure;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            if (expressedTerm.getEntity().getSubTerm() == null) {
                ExpressionStructure structure = createSuperterm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure);
            } else {
                OntologyDTO ontology = expressedTerm.getEntity().getSubTerm().getOntology();
                if (ontology == null)
                    throw new RuntimeException("No ontology provided:");

                ExpressionStructure structure = createPostComposedTerm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure);
            }
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        return pileStructure;
    }

    /**
     * Create a new structure for the pile.
     *
     * @param phenotypeDTO  Expressed Term dto
     * @param publicationID pub id
     */
    public PhenotypePileStructureDTO createPhenotypePileStructure(PhenotypeStatementDTO phenotypeDTO, String publicationID)
            throws PileStructureExistsException, TermNotFoundException, RelatedEntityNotFoundException {

        if (phenotypeDTO == null || publicationID == null)
            throw new TermNotFoundException("No Term or publication provided");

        LOG.info("Request: Create Composed term: " + phenotypeDTO.getDisplayName());
        PhenotypeStructure structure = DTOConversionService.getPhenotypeStructure(phenotypeDTO);
        if (getPhenotypeRepository().isPhenotypePileStructureExists(structure, publicationID)) {
            PileStructureExistsException exception = new PileStructureExistsException(phenotypeDTO);
            LOG.info(exception.getMessage());
            throw exception;
        }
        PhenotypePileStructureDTO pileStructure;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            structure = createPostComposedPhenotypeTerm(phenotypeDTO, publicationID);
            checkForValidTerms(structure);
            pileStructure = populatePileStructureDTOObject(structure);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        return pileStructure;
    }

    private void checkForValidTerms(PhenotypeStructure structure) throws TermNotFoundException, RelatedEntityNotFoundException {
        if (structure == null)
            return;
        if (structure.getEntity() == null || structure.getEntity().getSuperterm() == null)
            return;

        if (structure.getQualityTerm() == null)
            throw new TermNotFoundException("No Quality Term found.");

        if (structure.getQualityTerm().isPartOfSubset(Subset.RELATIONAL_SLIM)) {
            if ((structure.getRelatedEntity() == null || structure.getRelatedEntity().getSuperterm() == null))
                throw new RelatedEntityNotFoundException("No related entity found for related quality [" + structure.getQualityTerm().getTermName() + "]");
        }
    }

    /**
     * Re-create the phenotype structure pile.
     *
     * @param publicationID publication
     */
    public List<PhenotypePileStructureDTO> recreatePhenotypeStructurePile(String publicationID) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            getPhenotypeRepository().createPhenotypePile(publicationID);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
        }
        return getPhenotypePileStructures(publicationID);

    }

    /**
     * Re-create the complete structure pile. This is needed in case none of the structures being used
     * in expression records are on the pile.
     *
     * @param publicationID Publication id
     * @return complete structure pile.
     */
    @Override
    public List<ExpressionPileStructureDTO> recreateExpressionStructurePile(String publicationID) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            getExpressionRepository().createExpressionPile(publicationID);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
        }
        return getExpressionPileStructures(publicationID);
    }

    private List<ExpressionPileStructureDTO> getExpressionPileStructures(String publicationID) {
        Collection<ExpressionStructure> structures = getExpressionRepository().retrieveExpressionStructures(publicationID);
        if (structures == null)
            return null;

        List<ExpressionPileStructureDTO> expressionPileStructures = new ArrayList<ExpressionPileStructureDTO>(structures.size());
        for (ExpressionStructure structure : structures) {
            ExpressionPileStructureDTO dto = DTOConversionService.convertToExpressionPileStructureDTO(structure);
            expressionPileStructures.add(dto);
        }
        Collections.sort(expressionPileStructures);
        return expressionPileStructures;
    }

    /**
     * Remove a structure from the structure pile.
     *
     * @param structureDto Structure DTO
     */
    public ExpressionPileStructureDTO deleteStructure(ExpressionPileStructureDTO structureDto) {
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            ExpressionStructure structure = getExpressionRepository().getExpressionStructure(structureDto.getZdbID());
            getExpressionRepository().deleteExpressionStructure(structure);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Delete", e);
            tx.rollback();
            throw e;
        }
        return structureDto;
    }

    private ExpressionStructure createSuperterm(ExpressedTermDTO expressedTerm, String publicationID)
            throws TermNotFoundException {
        GenericTerm superterm = ontologyRepository.getTermByName(expressedTerm.getEntity().getSuperTerm().getName(), Ontology.ANATOMY);
        if (superterm == null)
            throw new TermNotFoundException("No superterm [" + expressedTerm.getEntity().getSuperTerm().getTermName() + "] found.");
        ExpressionStructure structure = new ExpressionStructure();
        structure.setSuperterm(superterm);
        structure.setPerson(Person.getCurrentSecurityUser());
        Publication pub = getPublicationRepository().getPublication(publicationID);
        structure.setPublication(pub);
        structure.setDate(new Date());
        getAnatomyRepository().createPileStructure(structure);
        LOG.info("Issued Term creation " + expressedTerm.getDisplayName());
        return structure;
    }

    private PhenotypeStructure createPostComposedPhenotypeTerm(PhenotypeStatementDTO phenotypeTerm, String publicationID)
            throws TermNotFoundException {

        PhenotypeStructure structure = DTOConversionService.getPhenotypeStructure(phenotypeTerm);
        getPhenotypeRepository().createPhenotypeStructure(structure, publicationID);
        LOG.info("Issued post-composed creation " + phenotypeTerm.getDisplayName());
        return structure;
    }

    private ExpressionPileStructureDTO populatePileStructureDTOObject(ExpressionStructure structure) {
        if (structure == null)
            return null;

        ExpressionPileStructureDTO dto = new RelatedPileStructureDTO();
        ExpressedTermDTO expDto = DTOConversionService.convertToExpressedTermDTO(structure);
        dto.setExpressedTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        Person person = structure.getPerson();
        if (person == null)
            throw new IllegalStateException("No Security Person found (not logged in)");
        dto.setCreator(person.getName());
        dto.setDate(structure.getDate());

        GenericTerm genericTerm = structure.getSuperterm();
        if(genericTerm.getOntology()==Ontology.ANATOMY){
            AnatomyItem anatomyItem = RepositoryFactory.getAnatomyRepository().getAnatomyItem(genericTerm.getTermName());
            dto.setStart(DTOConversionService.convertToStageDTO(anatomyItem.getStart()));
            dto.setEnd(DTOConversionService.convertToStageDTO(anatomyItem.getEnd()));
        }
        return dto;

    }

    private ExpressionStructure createPostComposedTerm(ExpressedTermDTO expressedTerm, String publicationID)
            throws TermNotFoundException {
        Ontology supertermOntology = DTOConversionService.convertToOntology(expressedTerm.getEntity().getSuperTerm().getOntology());
        GenericTerm superterm = ontologyRepository.getTermByName(expressedTerm.getEntity().getSuperTerm().getName(), supertermOntology);
        if (superterm == null)
            throw new TermNotFoundException("No Superterm term [" + expressedTerm.getEntity().getSuperTerm().getTermName() + "] found.");
        ExpressionStructure structure;
        Ontology subtermOntology = DTOConversionService.convertToOntology(expressedTerm.getEntity().getSubTerm().getOntology());
        GenericTerm subterm = ontologyRepository.getTermByName(expressedTerm.getEntity().getSubTerm().getName(), subtermOntology);
        if (subterm == null)
            throw new TermNotFoundException(expressedTerm.getEntity().getSubTerm().getTermName(), OntologyDTO.ANATOMY);
        ExpressionStructure aoStructure = new ExpressionStructure();
        aoStructure.setSuperterm(superterm);
        aoStructure.setSubterm(subterm);
        aoStructure.setPerson(Person.getCurrentSecurityUser());
        Publication pub = getPublicationRepository().getPublication(publicationID);
        aoStructure.setPublication(pub);
        aoStructure.setDate(new Date());
        getAnatomyRepository().createPileStructure(aoStructure);
        structure = aoStructure;
        LOG.info("Issued post-composed creation " + expressedTerm.getDisplayName());
        return structure;
    }

    private PhenotypePileStructureDTO populatePileStructureDTOObject(PhenotypeStructure structure) {
        if (structure == null)
            return null;

        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        PhenotypeStatementDTO expDto = DTOConversionService.convertToPhenotypeTermDTO(structure);
        dto.setPhenotypeTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        if (structure.getPerson() == null)
            throw new NullPointerException("No user info found for pile structure " + structure.getZdbID());
        dto.setCreator(structure.getPerson().getName());
        dto.setDate(structure.getDate());
        return dto;

    }

}
