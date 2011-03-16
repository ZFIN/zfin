package org.zfin.gwt.root.server;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.gwt.root.dto.*;
import org.zfin.mutant.MutantFigureStage;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test class for service class.
 */
public class DTOConversionServiceTest extends AbstractDatabaseTest{

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository() ;

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    public void getPhenotypeFigureStageFromDto() {
        String startID = "ZDB-STAGE-010723-35";
        String endID = "ZDB-STAGE-010723-49";
        String genotypeID = "ZDB-GENO-030530-1";
        String figureID = "ZDB-FIG-041108-3";
        // Standard
        String envID = "ZDB-EXP-041102-1";
        String pubID = "ZDB-PUB-090731-2";

        StageDTO start = new StageDTO();
        start.setZdbID(startID);
        StageDTO end = new StageDTO();
        end.setZdbID(endID);
        GenotypeDTO geno = new GenotypeDTO();
        geno.setZdbID(genotypeID);
        FigureDTO fig = new FigureDTO();
        fig.setZdbID(figureID);
        PhenotypeFigureStageDTO pfs = new PhenotypeFigureStageDTO();
        pfs.setGenotype(geno);
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
    public void convertTermDTO(){
//        Term t = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-118") ;
        // a GO term
        Term term = ontologyRepository.getTermByZdbID("ZDB-TERM-091209-10003") ;
        TermDTO termDTO = DTOConversionService.convertToTermDTO(term) ;
        assertEquals(term.getAliases().size(),termDTO.getAliases().size()) ;
        assertEquals(term.getTermName(),termDTO.getName()) ;
        assertNull(termDTO.getParentTerms()) ;
        assertNull(termDTO.getChildrenTerms()) ;
        assertEquals(0, termDTO.getAllRelatedTerms().size());
        assertNull(termDTO.getStartStage());
        assertNull(termDTO.getEndStage());
        assertEquals(term.getComment(),termDTO.getComment()) ;
        assertEquals(term.getDefinition(),termDTO.getDefinition()) ;
        assertEquals(term.getOboID(),termDTO.getOboID()) ;
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(term.getOntology().getOntologyName(),termDTO.getOntology().getOntologyName()) ;
        assertEquals(term.getZdbID(),termDTO.getZdbID()) ;

//        assertEquals(term.getEnd().getName(),termDTO.getStartStage().getName()) ;
//        assertEquals(term.getEnd().getName(),termDTO.getStartStage().getName()) ;
    }


    @Test
    public void convertTermDTOWithRelationships(){
//        Term t = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-118") ;
        // a GO term
        Term term = ontologyRepository.getTermByZdbID("ZDB-TERM-091209-10003") ;
        TermDTO termDTO = DTOConversionService.convertToTermDTOWithDirectRelationships(term) ;
        assertEquals(term.getAliases().size(),termDTO.getAliases().size()) ;
        assertEquals(term.getTermName(),termDTO.getName()) ;
        assertEquals(3,termDTO.getParentTerms().size());
        assertEquals(2, termDTO.getChildrenTerms().size());
        Map<String,Set<TermDTO>> allRelatedTerms = termDTO.getAllRelatedTerms() ;
        assertEquals(2, allRelatedTerms.keySet().size());
        assertEquals(2,allRelatedTerms.get("has subtype").size()) ;
        assertEquals(3,allRelatedTerms.get("is a type of").size()) ;
        assertNull(termDTO.getStartStage());
        assertNull(termDTO.getEndStage());
        assertEquals(term.getComment(),termDTO.getComment()) ;
        assertEquals(term.getDefinition(),termDTO.getDefinition()) ;
        assertEquals(term.getOboID(),termDTO.getOboID()) ;
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(term.getOntology().getOntologyName(),termDTO.getOntology().getOntologyName()) ;
        assertEquals(term.getZdbID(),termDTO.getZdbID()) ;

//        assertEquals(term.getEnd().getName(),termDTO.getStartStage().getName()) ;
//        assertEquals(term.getEnd().getName(),termDTO.getStartStage().getName()) ;
    }


    @Test
    public void convertTermDTOWithRelationshipsAndAnatomy(){
        // a GO term
        Term term = ontologyRepository.getTermByZdbID("ZDB-TERM-100331-1014") ;
        TermDTO termDTO = DTOConversionService.convertToTermDTOWithDirectRelationships(term) ;
        assertEquals(term.getAliases().size(),termDTO.getAliases().size()) ;
        assertEquals(term.getTermName(),termDTO.getName()) ;
        assertEquals(4,termDTO.getParentTerms().size());
        assertEquals(1, termDTO.getChildrenTerms().size());
        Map<String,Set<TermDTO>> allRelatedTerms = termDTO.getAllRelatedTerms() ;
        assertEquals(5, allRelatedTerms.keySet().size()); // start stage, end stage, part of, is_a
        assertEquals(1,allRelatedTerms.get("start stage").size()) ;
        assertEquals(1,allRelatedTerms.get("end stage").size()) ;
        assertEquals(1,allRelatedTerms.get("has parts").size()) ;
        assertEquals(1,allRelatedTerms.get("is part of").size()) ;
        assertEquals(1,allRelatedTerms.get("is a type of").size()) ;
//        assertEquals(5,allRelatedTerms.values().iterator().next().size());
        assertEquals(term.getComment(),termDTO.getComment()) ;
        assertEquals(term.getDefinition(),termDTO.getDefinition()) ;
        assertEquals(term.getOboID(),termDTO.getOboID()) ;
        // TODO: Note that this won't work with quality . . . grr
        assertEquals(term.getOntology().getOntologyName(),termDTO.getOntology().getOntologyName()) ;
        assertEquals(term.getZdbID(),termDTO.getZdbID()) ;

        assertNotNull(termDTO.getStartStage());
        assertNotNull(termDTO.getEndStage());
//        assertEquals(term.getEnd().getName(),termDTO.getStartStage().getName()) ;
//        assertEquals(term.getEnd().getName(),termDTO.getStartStage().getName()) ;
    }
}
