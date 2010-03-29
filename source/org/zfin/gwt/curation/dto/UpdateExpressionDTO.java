package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.AbstractFigureStageDTO;
import org.zfin.gwt.root.dto.AbstractPileStructureDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfer object related to updating figure annotations with structures
 * from the pile.
 */
public class UpdateExpressionDTO<T extends AbstractPileStructureDTO, W extends AbstractFigureStageDTO> implements IsSerializable {

    private String publicationID;
    private List<T> structures = new ArrayList<T>(5);
    private List<W> figureAnnotations;

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public List<T> getStructures() {
        return structures;
    }

    public void setStructures(List<T> structures) {
        this.structures = structures;
    }

    public List<W> getFigureAnnotations() {
        return figureAnnotations;
    }

    public void setFigureAnnotations(List<W> figureAnnotations) {
        this.figureAnnotations = figureAnnotations;
    }

    public void addPileStructureAnnotationDTO(T psa) {
        structures.add(psa);
    }
}
