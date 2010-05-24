package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.expression.ExpressionStructure;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.PileStructureExistsException;
import org.zfin.gwt.curation.ui.PileStructuresRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * GWT Class to facilitate pile structure activities, such as create, retrieve and delete.
 */
public class StructureRPCImpl extends RemoteServiceServlet implements PileStructuresRPC {

    private static final Logger LOG = RootLogger.getLogger(StructureRPCImpl.class);

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
            if (expressedTerm.getSubterm() == null) {
                ExpressionStructure structure = createSuperterm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure);
            } else {
                OntologyDTO ontology = expressedTerm.getSubterm().getOntology();
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
     * @param expressedTerm Expressed Term dto
     * @param publicationID pub id
     */
    public PhenotypePileStructureDTO createPhenotypePileStructure(PhenotypeTermDTO expressedTerm, String publicationID)
            throws PileStructureExistsException, TermNotFoundException {

        if (expressedTerm == null || publicationID == null)
            throw new TermNotFoundException("No Term or publication provided");

        LOG.info("Request: Create Composed term: " + expressedTerm.getDisplayName());
        if (getPhenotypeRepository().isPhenotypePileStructureExists(expressedTerm, publicationID)) {
            PileStructureExistsException exception = new PileStructureExistsException(expressedTerm);
            LOG.info(exception.getMessage());
            throw exception;
        }
        PhenotypePileStructureDTO pileStructure;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            PhenotypeStructure structure = createPostComposedPhenotypeTerm(expressedTerm, publicationID);
            pileStructure = populatePileStructureDTOObject(structure);
            tx.commit();
        } catch (HibernateException e) {
            LOG.error("Could not Add or Delete terms", e);
            tx.rollback();
            throw e;
        }
        return pileStructure;
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
        Term superterm = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, expressedTerm.getSuperterm().getTermName());
        if (superterm == null)
            throw new TermNotFoundException("No superterm [" + expressedTerm.getSuperterm().getTermName() + "] found.");
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

    private PhenotypeStructure createPostComposedPhenotypeTerm(PhenotypeTermDTO phenotypeTerm, String publicationID)
            throws TermNotFoundException {

        String superTermName = phenotypeTerm.getSuperterm().getTermName();
        if (StringUtils.isEmpty(superTermName))
            throw new TermNotFoundException("No superterm name provided.");

        String qualityTermName = phenotypeTerm.getQuality().getTermName();
        if (StringUtils.isEmpty(qualityTermName))
            throw new TermNotFoundException("No quality term name provided.");

        String tagName = phenotypeTerm.getTag();
        if (StringUtils.isEmpty(tagName))
            throw new TermNotFoundException("No tagName name provided.");

        List<Ontology> zfinOntology = Ontology.getOntologies(phenotypeTerm.getSuperterm().getOntology().getOntologyName());
        Term superterm = OntologyManager.getInstance().getTermByName(zfinOntology, superTermName);
        if (superterm == null)
            throw new TermNotFoundException("No Superterm named [" + superTermName + "] found.");

        PhenotypeStructure structure = new PhenotypeStructure();
        structure.setSuperterm(superterm);
        if (phenotypeTerm.getSubterm() != null) {
            zfinOntology = Ontology.getOntologies(phenotypeTerm.getSubterm().getOntology().getOntologyName());
            Term subterm = OntologyManager.getInstance().getTermByName(zfinOntology, phenotypeTerm.getSubterm().getTermName());
            if (subterm == null)
                throw new TermNotFoundException("No Subterm named [" + phenotypeTerm.getSubterm().getTermName() + "] found.");
            structure.setSubterm(subterm);
        }
        zfinOntology = Ontology.getOntologies(phenotypeTerm.getQuality().getOntology().getOntologyName());
        Term qualityTerm = OntologyManager.getInstance().getTermByName(zfinOntology, qualityTermName);
        if (qualityTerm == null)
            throw new TermNotFoundException("No quality term named [" + qualityTermName + "] found.");
        structure.setQuality(qualityTerm);
        structure.setTag(Phenotype.Tag.getTagFromName(tagName));
        structure.setPerson(Person.getCurrentSecurityUser());
        getPhenotypeRepository().createPhenotypeStructure(structure, publicationID);

        LOG.info("Issued post-composed creation " + phenotypeTerm.getDisplayName());
        return structure;
    }

    private ExpressionPileStructureDTO populatePileStructureDTOObject(ExpressionStructure structure) {
        if (structure == null)
            return null;

        ExpressionPileStructureDTO dto = new RelatedPileStructureDTO();
        ExpressedTermDTO expDto = new ExpressedTermDTO();
        expDto.setSuperterm(DTOConversionService.convertToTermDTO(structure.getSuperterm()));
        dto.setExpressedTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        Person person = structure.getPerson();
        if (person == null)
            throw new IllegalStateException("No Security Person found (not logged in)");
        dto.setCreator(person.getName());
        dto.setDate(structure.getDate());
        expDto.setSubterm(DTOConversionService.convertToTermDTO(structure.getSubterm()));
        dto.setStart(DTOConversionService.convertToStageDTO(structure.getSuperterm().getStart()));
        dto.setEnd(DTOConversionService.convertToStageDTO(structure.getSuperterm().getEnd()));
        return dto;

    }

    private ExpressionStructure createPostComposedTerm(ExpressedTermDTO expressedTerm, String publicationID)
            throws TermNotFoundException {
        Ontology supertermOntology = DTOConversionService.convertToOntology(expressedTerm.getSuperterm().getOntology());
        Term superterm = OntologyManager.getInstance().getTermByName(supertermOntology, expressedTerm.getSuperterm().getTermName());
        if (superterm == null)
            throw new TermNotFoundException("No Superterm term [" + expressedTerm.getSuperterm().getTermName() + " found.");
        ExpressionStructure structure;
        Ontology subtermOntology = DTOConversionService.convertToOntology(expressedTerm.getSubterm().getOntology());
        Term subterm = OntologyManager.getInstance().getTermByName(subtermOntology, expressedTerm.getSubterm().getTermName());
        if (subterm == null)
            throw new TermNotFoundException(expressedTerm.getSubterm().getTermName(), OntologyDTO.ANATOMY);
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
        PhenotypeTermDTO expDto = new PhenotypeTermDTO();
        expDto.setSuperterm(DTOConversionService.convertToTermDTO(structure.getSuperterm()));

        dto.setPhenotypeTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        dto.setCreator(structure.getPerson().getName());
        dto.setDate(structure.getDate());
        if (structure.getSubterm() != null) {
            expDto.setSubterm(DTOConversionService.convertToTermDTO(structure.getSubterm()));
        }
        expDto.setQuality(DTOConversionService.convertToTermDTO(structure.getQuality()));
        expDto.setTag(structure.getTag().toString());
        return dto;

    }

}
