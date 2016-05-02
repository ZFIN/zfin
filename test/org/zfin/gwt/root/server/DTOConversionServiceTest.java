package org.zfin.gwt.root.server;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.gwt.root.dto.*;
import org.zfin.mutant.MutantFigureStage;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test class for service class.
 */
public class DTOConversionServiceTest extends AbstractDatabaseTest {

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    public void getPhenotypeFigureStageFromDto() {
        String startID = "ZDB-STAGE-010723-35";
        String endID = "ZDB-STAGE-010723-49";
        String fishID = "ZDB-FISH-150901-14317";
        String figureID = "ZDB-FIG-041108-3";
        // Standard
        String envID = "ZDB-EXP-041102-1";
        String pubID = "ZDB-PUB-090731-2";

        StageDTO start = new StageDTO();
        start.setZdbID(startID);
        StageDTO end = new StageDTO();
        end.setZdbID(endID);
        FishDTO fishDTO = new FishDTO();
        fishDTO.setZdbID(fishID);
        FigureDTO fig = new FigureDTO();
        fig.setZdbID(figureID);
        PhenotypeExperimentDTO pfs = new PhenotypeExperimentDTO();
        pfs.setFish(fishDTO);
        pfs.setStart(start);
        pfs.setEnd(end);
        pfs.setFigure(fig);
        EnvironmentDTO envDto = new EnvironmentDTO();
        envDto.setZdbID(envID);
        pfs.setEnvironment(envDto);
        pfs.setPublicationID(pubID);
        MutantFigureStage mfs = DTOConversionService.convertToMutantFigureStageFromDTO(pfs);
        assertNotNull(mfs);
        assertNotNull(mfs.getEnd());
        assertNotNull(mfs.getStart());
        assertNotNull(mfs.getFigure());
        assertNotNull(mfs.getGenotypeExperiment());
    }


    @Test
    public void convertTermDTO() {
        GenericTerm term = ontologyRepository.getTermByZdbID("ZDB-TERM-091209-10003");
        TermDTO termDTO = DTOConversionService.convertToTermDTO(term);
        assertEquals(term.getAliases().size(), termDTO.getAliases().size());
        assertEquals(term.getTermName(), termDTO.getName());
        assertNull(termDTO.getParentTerms());
        assertNull(termDTO.getChildrenTerms());
        assertEquals(0, termDTO.getAllRelatedTerms().size());
        assertNull(termDTO.getStartStage());
        assertNull(termDTO.getEndStage());
        assertEquals(term.getComment(), termDTO.getComment());
        assertEquals(term.getDefinition(), termDTO.getDefinition());
        assertEquals(term.getOboID(), termDTO.getOboID());
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(term.getOntology().getOntologyName(), termDTO.getOntology().getOntologyName());
        assertEquals(term.getZdbID(), termDTO.getZdbID());

    }


    @Test
    public void convertTermDTOWithRelationships() {
//        Term t = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-118") ;
        // a GO term
        GenericTerm term = ontologyRepository.getTermByZdbID("ZDB-TERM-091209-10003");
        TermDTO termDTO = DTOConversionService.convertToTermDTOWithDirectRelationships(term);
        assertEquals(term.getAliases().size(), termDTO.getAliases().size());
        assertEquals(term.getTermName(), termDTO.getName());
        assertTrue(termDTO.getParentTerms().size() > 1);
        assertEquals(2, termDTO.getChildrenTerms().size());
        Map<String, Set<TermDTO>> allRelatedTerms = termDTO.getAllRelatedTerms();
        assertEquals(2, allRelatedTerms.keySet().size());
        assertEquals(2, allRelatedTerms.get("has subtype").size());
        assertTrue(allRelatedTerms.get("is a type of").size() > 1);
        assertNull(termDTO.getStartStage());
        assertNull(termDTO.getEndStage());
        assertEquals(term.getComment(), termDTO.getComment());
        assertEquals(term.getDefinition(), termDTO.getDefinition());
        assertEquals(term.getOboID(), termDTO.getOboID());
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(term.getOntology().getOntologyName(), termDTO.getOntology().getOntologyName());
        assertEquals(term.getZdbID(), termDTO.getZdbID());

    }


    @Test
    public void convertTermDTOWithRelationshipsAndAnatomy() {
        // a GO term
        GenericTerm term = ontologyRepository.getTermByZdbID("ZDB-TERM-100331-1014");
        TermDTO termDTO = DTOConversionService.convertToTermDTOWithDirectRelationships(term);
        assertEquals(term.getAliases().size(), termDTO.getAliases().size());
        assertEquals(term.getTermName(), termDTO.getName());
        assertEquals(4, termDTO.getParentTerms().size());
        assertEquals(1, termDTO.getChildrenTerms().size());
        Map<String, Set<TermDTO>> allRelatedTerms = termDTO.getAllRelatedTerms();
        assertEquals(5, allRelatedTerms.keySet().size()); // start stage, end stage, part of, is_a
        assertEquals(1, allRelatedTerms.get("start stage").size());
        assertEquals(1, allRelatedTerms.get("end stage").size());
        assertEquals(1, allRelatedTerms.get("has parts").size());
        assertEquals(1, allRelatedTerms.get("is part of").size());
        assertEquals(1, allRelatedTerms.get("is a type of").size());
//        assertEquals(5,allRelatedTerms.values().iterator().next().size());
        assertEquals(term.getComment(), termDTO.getComment());
        assertEquals(term.getDefinition(), termDTO.getDefinition());
        assertEquals(term.getOboID(), termDTO.getOboID());
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(term.getOntology().getOntologyName(), termDTO.getOntology().getOntologyName());
        assertEquals(term.getZdbID(), termDTO.getZdbID());

        assertNotNull(termDTO.getStartStage());
        assertNotNull(termDTO.getEndStage());
    }

    @Test
    public void convertOntologies() {

        OntologyDTO[] ontologyDtoList = OntologyDTO.values();
        for (OntologyDTO ontologyDto : ontologyDtoList) {
            assertNotNull("Ontology <" + ontologyDto + "> has no counter part OntologyDTO! Please define one in the DTOConversionService class",
                    DTOConversionService.convertToOntology(ontologyDto));
        }

        Ontology[] ontologyList = Ontology.values();
        for (Ontology ontology : ontologyList) {
            assertNotNull("OntologyDTO: " + ontology + " has no counter part Ontology!", DTOConversionService.convertToOntologyDTO(ontology));
        }
    }
}
