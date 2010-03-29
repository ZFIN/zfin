package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.AbstractFigureStageDTO;
import org.zfin.gwt.root.dto.AbstractPileStructureDTO;
import org.zfin.gwt.root.dto.ExpressedTermDTO;

import java.util.List;

/**
 * This class defines a structure pile.
 */
public interface StructurePile<T extends ExpressedTermDTO, W extends AbstractFigureStageDTO, Q extends AbstractPileStructureDTO> 
        extends PileStructureListener<Q> {

    public static final String UNSPECIFIED = "unspecified";

    /**
     * Check to see if the current structure pile holds a given structure.
     * Can be used for new structure creation to avoid a server request.
     * @param expressedTerm expressedTermDTO.
     * @return true or false
     */
    boolean hasStructureOnPile(T expressedTerm);

    /**
     * This notifies the structure pile about selected expressions. The structure pile will
     * display all the annotated structures to all expressions according to certain rules:
     * 1) Bold face all structures that match (have a stage overlap) all selected expressions.
     * 2) Set the action radio button ADD to true if given structure is common to all selected expressions.
     *
     * @param selectedExpressions all selected expression records
     */
    void updateFigureAnnotations(List<W> selectedExpressions);

    /**
     * Set a pile construction zone module.
     * @param constructionZoneModule pile construction zone module.
     */
    void setPileStructureClickListener(ConstructionZone constructionZoneModule);

    /**
     * Inject an Expression Section module.
     * @param expressionSection expression Section
     */
    public void setExpressionSection(ExpressionSection expressionSection);

    /**
     * Start the module. This method needs to be called to initialize the module fully.
     */
    void runModule();
}
