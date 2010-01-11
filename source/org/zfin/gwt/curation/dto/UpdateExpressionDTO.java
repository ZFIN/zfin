package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.ExpressionFigureStageDTO;
import org.zfin.gwt.root.dto.PileStructureAnnotationDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfer object related to updating figure annotations with structures
 * from the pile.
 */
public class UpdateExpressionDTO implements IsSerializable {

    private String publicationID;
    private List<PileStructureAnnotationDTO> structures = new ArrayList<PileStructureAnnotationDTO>();
    private List<ExpressionFigureStageDTO> figureAnnotations;

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public List<PileStructureAnnotationDTO> getStructures() {
        return structures;
    }

    public void setStructures(List<PileStructureAnnotationDTO> structures) {
        this.structures = structures;
    }

    public List<ExpressionFigureStageDTO> getFigureAnnotations() {
        return figureAnnotations;
    }

    public void setFigureAnnotations(List<ExpressionFigureStageDTO> figureAnnotations) {
        this.figureAnnotations = figureAnnotations;
    }

    public void addPileStructureAnnotationDTO(PileStructureAnnotationDTO psa) {

        structures.add(psa);

    }
}
