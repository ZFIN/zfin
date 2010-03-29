package org.zfin.gwt.curation.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.AnatomyExpressionStructure;
import org.zfin.expression.ExpressionStructure;
import org.zfin.expression.GOExpressionStructure;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.curation.ui.PileStructureExistsException;
import org.zfin.gwt.curation.ui.PileStructuresRPC;
import org.zfin.gwt.root.dto.*;
import org.zfin.mutant.Phenotype;
import org.zfin.mutant.PhenotypeStructure;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.Ontology;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.BODtoConversionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
            PhenotypePileStructureDTO dto = BODtoConversionService.getPhenotypePileStructureDTO(structure);
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
            throws PileStructureExistsException, org.zfin.gwt.root.util.TermNotFoundException {
        if (expressedTerm == null || publicationID == null)
            throw new org.zfin.gwt.root.util.TermNotFoundException("No Term or publication provided");


        LOG.info("Request: Create Composed term: " + expressedTerm.getDisplayName());
        if (getExpressionRepository().pileStructureExists(expressedTerm, publicationID)) {
            PileStructureExistsException exception = new PileStructureExistsException(expressedTerm);
            LOG.info(exception.getMessage());
            throw exception;
        }
        ExpressionPileStructureDTO pileStructure = null;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            if (expressedTerm.getSubterm() == null) {
                ExpressionStructure structure = createSuperterm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure, expressedTerm);
            } else {
                OntologyDTO ontology = expressedTerm.getSubterm().getOntology();
                if (ontology == null)
                    throw new RuntimeException("No ontology provided:");

                ExpressionStructure structure = createPostcomposedTerm(expressedTerm, publicationID);
                pileStructure = populatePileStructureDTOObject(structure, expressedTerm);
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
            throws PileStructureExistsException, org.zfin.gwt.root.util.TermNotFoundException {

        if (expressedTerm == null || publicationID == null)
            throw new org.zfin.gwt.root.util.TermNotFoundException("No Term or publication provided");

        LOG.info("Request: Create Composed term: " + expressedTerm.getDisplayName());
        if (getPhenotypeRepository().isPhenotypePileStructureExists(expressedTerm, publicationID)) {
            PileStructureExistsException exception = new PileStructureExistsException(expressedTerm);
            LOG.info(exception.getMessage());
            throw exception;
        }
        PhenotypePileStructureDTO pileStructure = null;
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            PhenotypeStructure structure = createPostComposedPhenotypeTerm(expressedTerm, publicationID);
            pileStructure = populatePileStructureDTOObject(structure, expressedTerm);
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

    private ExpressionStructure createSuperterm(ExpressedTermDTO expressedTerm, String publicationID) throws org.zfin.gwt.root.util.TermNotFoundException {
        AnatomyItem superterm = getAnatomyRepository().getAnatomyItem(expressedTerm.getSuperterm().getTermName());
        if (superterm == null)
            throw new org.zfin.gwt.root.util.TermNotFoundException("No superterm [" + expressedTerm.getSuperterm().getTermName() + "] found.");
        ExpressionStructure structure = new AnatomyExpressionStructure();
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
            throws org.zfin.gwt.root.util.TermNotFoundException {

        String superTermName = phenotypeTerm.getSuperterm().getTermName();
        if (StringUtils.isEmpty(superTermName))
            throw new org.zfin.gwt.root.util.TermNotFoundException("No superterm name provided.");

        String qualityTermName = phenotypeTerm.getQuality().getTermName();
        if (StringUtils.isEmpty(qualityTermName))
            throw new org.zfin.gwt.root.util.TermNotFoundException("No quality term name provided.");

        String tagName = phenotypeTerm.getTag();
        if (StringUtils.isEmpty(tagName))
            throw new org.zfin.gwt.root.util.TermNotFoundException("No tagName name provided.");

        List<Ontology> zfinOntology = Ontology.getOntologies(phenotypeTerm.getSuperterm().getOntology().getOntologyName());
        GenericTerm superterm = getInfrastructureRepository().getTermByName(superTermName, zfinOntology);
        if (superterm == null)
            throw new org.zfin.gwt.root.util.TermNotFoundException("No Superterm term [" + superTermName + " found.");

        PhenotypeStructure structure = new PhenotypeStructure();
        structure.setSuperterm(superterm);
        if (phenotypeTerm.getSubterm() != null) {
            zfinOntology = Ontology.getOntologies(phenotypeTerm.getSubterm().getOntology().getOntologyName());
            GenericTerm subterm = getInfrastructureRepository().getTermByName(phenotypeTerm.getSubterm().getTermName(), zfinOntology);
            if (subterm == null)
                throw new org.zfin.gwt.root.util.TermNotFoundException("No Subterm term [" + phenotypeTerm.getSubterm().getTermName() + " found.");
            structure.setSubterm(subterm);
        }
        zfinOntology = Ontology.getOntologies(phenotypeTerm.getQuality().getOntology().getOntologyName());
        GenericTerm subterm = getInfrastructureRepository().getTermByName(qualityTermName, zfinOntology);
        if (subterm == null)
            throw new org.zfin.gwt.root.util.TermNotFoundException("No Subterm term [" + qualityTermName + " found.");
        structure.setQuality(subterm);
        structure.setTag(Phenotype.Tag.getTagFromName(tagName));
        structure.setPerson(Person.getCurrentSecurityUser());
        getPhenotypeRepository().createPhenotypeStructure(structure, publicationID);

        LOG.info("Issued post-composed creation " + phenotypeTerm.getDisplayName());
        return structure;
    }

    private ExpressionPileStructureDTO populatePileStructureDTOObject(ExpressionStructure structure, ExpressedTermDTO expressedTerm) {
        if (structure == null)
            return null;

        ExpressionPileStructureDTO dto = new RelatedPileStructureDTO();
        ExpressedTermDTO expDto = new ExpressedTermDTO();
        expDto.setSuperterm(BODtoConversionService.getTermDto(structure.getSuperterm()));
        dto.setExpressedTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        Person person = structure.getPerson();
        if (person == null)
            throw new IllegalStateException("No Security Person found (not logged in)");
        dto.setCreator(person.getName());
        dto.setDate(structure.getDate());
        if (!org.apache.commons.lang.StringUtils.isEmpty(structure.getSubtermID())) {
            if (structure instanceof AnatomyExpressionStructure) {
                AnatomyExpressionStructure aoStructure = (AnatomyExpressionStructure) structure;
                expDto.setSubterm(BODtoConversionService.getTermDto(aoStructure.getSubterm()));
            } else {
                GOExpressionStructure goStructure = (GOExpressionStructure) structure;
                expDto.setSubterm(BODtoConversionService.getTermDto(goStructure.getSubterm()));
            }
        }
        dto.setStart(BODtoConversionService.getStageDto(structure.getSuperterm().getStart()));
        dto.setEnd(BODtoConversionService.getStageDto(structure.getSuperterm().getEnd()));
        return dto;

    }

    private ExpressionStructure createPostcomposedTerm(ExpressedTermDTO expressedTerm, String publicationID)
            throws org.zfin.gwt.root.util.TermNotFoundException {
        AnatomyItem superterm = getAnatomyRepository().getAnatomyItem(expressedTerm.getSuperterm().getTermName());
        if (superterm == null)
            throw new org.zfin.gwt.root.util.TermNotFoundException("No Superterm term [" + expressedTerm.getSuperterm().getTermName() + " found.");
        ExpressionStructure structure = null;
        OntologyDTO ontology = expressedTerm.getSubterm().getOntology();
        if (ontology == OntologyDTO.ANATOMY) {
            AnatomyItem subterm = getAnatomyRepository().getAnatomyItem(expressedTerm.getSubterm().getTermName());
            if (subterm == null)
                throw new org.zfin.gwt.root.util.TermNotFoundException(expressedTerm.getSubterm().getTermName(), OntologyDTO.ANATOMY);
            AnatomyExpressionStructure aoStructure = new AnatomyExpressionStructure();
            aoStructure.setSuperterm(superterm);
            aoStructure.setSubterm(subterm);
            aoStructure.setPerson(Person.getCurrentSecurityUser());
            Publication pub = getPublicationRepository().getPublication(publicationID);
            aoStructure.setPublication(pub);
            aoStructure.setDate(new Date());
            getAnatomyRepository().createPileStructure(aoStructure);
            structure = aoStructure;
        } else {
            GOExpressionStructure goStructure = new GOExpressionStructure();
            goStructure.setSuperterm(superterm);
            goStructure.setPerson(Person.getCurrentSecurityUser());
            Publication pub = getPublicationRepository().getPublication(publicationID);
            goStructure.setPublication(pub);
            goStructure.setDate(new Date());
            if (!org.apache.commons.lang.StringUtils.isEmpty(expressedTerm.getSubterm().getTermName())) {
                GoTerm subterm = RepositoryFactory.getMutantRepository().getGoTermByName(expressedTerm.getSubterm().getTermName());
                if (subterm == null)
                    throw new org.zfin.gwt.root.util.TermNotFoundException(expressedTerm.getSubterm().getTermName(), OntologyDTO.GO);
                goStructure.setSubterm(subterm);
            }
            getAnatomyRepository().createPileStructure(goStructure);
            structure = goStructure;
        }
        LOG.info("Issued post-composed creation " + expressedTerm.getDisplayName());
        return structure;
    }

    private PhenotypePileStructureDTO populatePileStructureDTOObject(PhenotypeStructure structure, PhenotypeTermDTO expressedTerm) {
        if (structure == null)
            return null;

        PhenotypePileStructureDTO dto = new PhenotypePileStructureDTO();
        PhenotypeTermDTO expDto = new PhenotypeTermDTO();
        expDto.setSuperterm(BODtoConversionService.getTermDto(structure.getSuperterm()));

        dto.setPhenotypeTerm(expDto);
        dto.setZdbID(structure.getZdbID());
        dto.setCreator(structure.getPerson().getName());
        dto.setDate(structure.getDate());
        if (structure.getSubterm() != null) {
            expDto.setSubterm(BODtoConversionService.getTermDto(structure.getSubterm()));
        }
        expDto.setQuality(BODtoConversionService.getTermDto(structure.getQuality()));
        expDto.setTag(structure.getTag().toString());
        return dto;

    }

}
