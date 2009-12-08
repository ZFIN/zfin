package org.zfin.curation.client;

import org.zfin.framework.presentation.dto.ExperimentDTO;
import org.zfin.framework.presentation.dto.ExpressionFigureStageDTO;
import org.zfin.framework.presentation.dto.ExpressedTermDTO;
import cvu.html.AttributeList;

import java.util.List;
import java.util.Map;
import java.util.AbstractSet;
import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface ExpressionSection {

    /**
     * Inject a pile structure object into the section.
     *
     * @param structureModule structure pile.
     */
    void setPileStructure(StructurePile structureModule);

    /**
     * This method should be called when an experiment including all expressions is removed.
     * It requires to remove the removed expressions in the display list.
     *
     * @param experiment experiment being removed.
     */
    void removeFigureAnnotations(ExperimentDTO experiment);

    /**
     * Re-read the expressions in case something has changed the epxressions,
     * e.g. modified experiments whose attributes are displayed.
     */
    void retrieveExpressions();

    /**
     * Retrieve a list of selected expression records.
     *
     * @return list of expression.
     */
    List<ExpressionFigureStageDTO> getSelectedExpressions();

    /**
     * Retrieve all expressed Terms.
     *
     * @return set of expressed terms.
     */
    Set<ExpressedTermDTO> getExpressedTermDTOs();

    /**
     * Use to re-display (if visible) the figure annotations and highlight the given
     * structure bold as it needs to be removed before a structure from the structure pile
     * can be deleted.
     *
     * @param dto  ExpressedTermDTO
     * @param mark boolean: if true bold face the structures, if false undo the bold facing.
     */
    void markStructuresForDeletion(ExpressedTermDTO dto, boolean mark);

    /**
     * When structures where added or removed from expressions call this method to synchronize the expression
     * section.
     */
    void postUpdateStructuresOnExpression();

    /**
     * Set the experiment filter that defines what expressions to display and which ones to hide.
     *
     * @param experimentFilter experiment dto
     */
    void setExperimentFilter(ExperimentDTO experimentFilter);

    /**
     * set a figure for filtering purposes. Only display expressions with figure annotations to this figure.
     *
     * @param figureID figure id.
     */
    void setFigureID(String figureID);

    /**
     * Initialize the module.
     */
    void runModule();

    /**
     * Set all filter elements.
     *
     * @param figureID         figure id
     * @param experimentFilter experiment
     */
    void applyFilterElements(String figureID, ExperimentDTO experimentFilter);
}
