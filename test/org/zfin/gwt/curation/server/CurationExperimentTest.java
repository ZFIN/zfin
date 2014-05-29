package org.zfin.gwt.curation.server;

import org.junit.Test;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.ontology.AbstractOntologyTest;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.zfin.ontology.Ontology.ANATOMY;
import static org.zfin.ontology.Ontology.STAGE;

public class CurationExperimentTest extends AbstractOntologyTest {

    @Test
    public void copyExpressions() {
        List<ExpressionFigureStageDTO> copyFromExpressions = new ArrayList<ExpressionFigureStageDTO>(1);
        ExpressionFigureStageDTO fromDto = new ExpressionFigureStageDTO();
        fromDto.setStart(getStageDTO("Zygote:1-cell"));
        fromDto.setEnd(getStageDTO("Gastrula:Bud"));
        List<ExpressedTermDTO> expressedTermDTOs = new ArrayList<ExpressedTermDTO>(1);
        ExpressedTermDTO termOne = new ExpressedTermDTO();
        termOne.setEntity(getEntity("groove", "cell"));
        termOne.setExpressionFound(true);
        expressedTermDTOs.add(termOne);

        ExpressedTermDTO termOne2 = new ExpressedTermDTO();
        termOne2.setEntity(getEntity("yolk", null));
        termOne2.setExpressionFound(true);
        expressedTermDTOs.add(termOne2);

        ExpressedTermDTO termOne3 = new ExpressedTermDTO();
        termOne3.setEntity(getEntity("anatomical structure", null));
        termOne3.setExpressionFound(true);
        expressedTermDTOs.add(termOne3);


        fromDto.setExpressedTerms(expressedTermDTOs);

        copyFromExpressions.add(fromDto);

        List<ExpressionFigureStageDTO> copyToExpressions = new ArrayList<ExpressionFigureStageDTO>(1);
        ExpressionFigureStageDTO toDto = new ExpressionFigureStageDTO();
        toDto.setStart(getStageDTO("Larval:Day 6"));
        toDto.setEnd(getStageDTO("Adult"));
        List<ExpressedTermDTO> expressedTermDTOsTwo = new ArrayList<ExpressedTermDTO>(1);
        ExpressedTermDTO termTwo = new ExpressedTermDTO();
        termTwo.setEntity(getEntity("liver", "melanocyte"));
        expressedTermDTOsTwo.add(termTwo);
        termTwo.setExpressionFound(true);
        toDto.setExpressedTerms(expressedTermDTOsTwo);

        copyToExpressions.add(toDto);

        List<PileStructureAnnotationDTO> pileStructureAnnotationDTOList = new ArrayList<PileStructureAnnotationDTO>(copyFromExpressions.size());
        for (ExpressionFigureStageDTO copyFromAnnotation : copyFromExpressions) {
            pileStructureAnnotationDTOList.addAll(DTOConversionService.getPileStructureDTO(copyFromAnnotation, PileStructureAnnotationDTO.Action.ADD));
        }
        UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateExpressionDTO = new UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO>();
        updateExpressionDTO.setFigureAnnotations(copyToExpressions);
        updateExpressionDTO.setStructures(pileStructureAnnotationDTOList);

        CurationExperimentRPCImpl service = new CurationExperimentRPCImpl();
        service.removeInvalidExpressionsFromAnnotation(updateExpressionDTO);

        assertNotNull(updateExpressionDTO);
        // the 'yolk' does not have a stage overlap with liver... So it is removed
        assertEquals("There are two expressed terms (One is out of stage range)", 2 , updateExpressionDTO.getStructures().size());
    }

    private EntityDTO getEntity(String superTermName, String subTermName) {
        EntityDTO dto = new EntityDTO();
        dto.setSuperTerm(OntologyManager.getInstance().getTermByName(superTermName, ANATOMY));
        if (subTermName != null)
            dto.setSubTerm(OntologyManager.getInstance().getTermByName(subTermName, ANATOMY));
        return dto;
    }

    private StageDTO getStageDTO(String stageName) {
        TermDTO term = OntologyManager.getInstance().getTermByName(stageName, STAGE);
        return term.getStartStage();
    }

    @Override
    protected Ontology[] getOntologiesToLoad() {
        return new Ontology[]{
                ANATOMY,
                STAGE};
    }

}