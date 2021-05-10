package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class UpdateStructuresUtil {

    public void onClick(ClickEvent widget) {
        //Window.alert("Update Structures");
        //UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> updateEntity = getSelectedStructures();
        //List<PhenotypeFigureStageDTO> efs = getSelectedMutants();
        //updateEntity.setFigureAnnotations(efs);
        //phenotypeRPCAsync.updateStructuresForExpression(updateEntity, new UpdateExpressionCallback());
    }

/*
    private UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> getSelectedStructures() {
        Set<Integer> keys = displayTable.getDisplayTableMap().keySet();
        UpdateExpressionDTO dto = new UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO>();
        for (Integer row : keys) {
            RadioButton add = displayTable.getAddRadioButton(row);
            RadioButton remove = displayTable.getRemoveRadioButton(row);
            PileStructureAnnotationDTO psa = new PileStructureAnnotationDTO();
            if (add.getValue() || remove.getValue()) {
                PhenotypePileStructureDTO term = (PhenotypePileStructureDTO) displayTable.getDisplayTableMap().get(row).copy();
                psa.setExpressedTerm(term.getExpressedTerm());
                psa.setZdbID(term.getZdbID());
            }
            if (add.getValue())
                psa.setAction(PileStructureAnnotationDTO.Action.ADD);
            else if (remove.getValue())
                psa.setAction(PileStructureAnnotationDTO.Action.REMOVE);
            if (add.getValue() || remove.getValue())
                dto.addPileStructureAnnotationDTO(psa);
        }
        return dto;
    }

    private List<PhenotypeFigureStageDTO> getSelectedMutants() {
        return expressionSection.getSelectedExpressions();
    }
*/


}
