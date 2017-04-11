package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.StageRangeIntersectionService;

import java.util.List;

/**
 * RPC Async Class for the Curation module.
 */
public interface CurationExperimentRPCAsync {

    void getExperimentsByFilter(ExpressionExperimentDTO experimentFilter, AsyncCallback<List<ExpressionExperimentDTO>> async);

    /**
     * Retrieve all genotypes for a given publication:
     * 1) WT
     * 2)
     * 3)
     *
     * @param publicationID pub ID
     * @param async         callback
     */
    void getFishList(String publicationID, AsyncCallback<List<FilterSelectionBoxEntry>> async);

    /**
     * Update an existing experiment.
     *
     * @param selectedExperiment experiment to be updated
     * @param async              callback
     */
    void updateExperiment(ExpressionExperimentDTO selectedExperiment, AsyncCallback<ExpressionExperimentDTO> async);

    /**
     * Check the visibility of the experiment section
     *
     * @param publicationID pub ID
     * @param async         callback
     */
    void readExperimentSectionVisibility(String publicationID, AsyncCallback<Boolean> async);

    /**
     * Set Experiment Section visibility.
     *
     * @param pubID                publication ID
     * @param experimentVisibility experiment section visibility
     * @param async                call back
     */
    void setExperimentVisibilitySession(String pubID, boolean experimentVisibility, AsyncCallback<Void> async);

    /**
     * Retrieve all figures that are available for this publication
     *
     * @param publicationID string
     * @param async         callback
     */
    void readFigures(String publicationID, AsyncCallback<List<String>> async);

    void getGenes(String pubID, AsyncCallback<List<FilterSelectionBoxEntry>> async);

    //// Expression Section

    void readExpressionSectionVisibility(String publicationID, AsyncCallback<Boolean> async);

    void getExpressionsByFilter(ExpressionExperimentDTO experimentFilter, String figureID, AsyncCallback<List<ExpressionFigureStageDTO>> async);

    void getFigures(String publicationID, AsyncCallback<List<FilterSelectionBoxEntry>> async);

    void getStages(AsyncCallback<List<StageDTO>> async);

    void createFigureAnnotations(List<ExpressionFigureStageDTO> newFigureAnnotations, AsyncCallback<List<ExpressionFigureStageDTO>> callback);

    void deleteFigureAnnotation(ExpressionFigureStageDTO figureAnnotation, AsyncCallback callback);

    void setExpressionVisibilitySession(String publicationID, boolean b, AsyncCallback callback);

    void getFigureFilter(String publicationID, AsyncCallback<FigureDTO> callback);

    void getGeneFilter(String publicationID, AsyncCallback<MarkerDTO> callback);

    void createPatoRecord(ExpressionFigureStageDTO efs, AsyncCallback<Void> callback);

    void setFigureAnnotationStatus(ExpressionFigureStageDTO checkedExpression, boolean checked, AsyncCallback callback);

    void getFigureAnnotationCheckmarkStatus(String publicationID, AsyncCallback<CheckMarkStatusDTO> callback);

    void readStructureSectionVisibility(String publicationID, boolean isPhenotype,  AsyncCallback<Boolean> callback);

    void setStructureVisibilitySession(String publicationID, boolean b, boolean isPhenotype, AsyncCallback<Void> async);

    void getStructures(String publicationID, AsyncCallback<List<ExpressionPileStructureDTO>> retrieveStructuresCallback);

    void updateStructuresForExpression(UpdateExpressionDTO updateEntity, AsyncCallback<List<ExpressionFigureStageDTO>> callback);

    void getTermsWithStageOverlap(ExpressionPileStructureDTO selectedPileStructure, StageRangeIntersectionService intersection, AsyncCallback<List<RelatedPileStructureDTO>> callback);

    void saveSessionVisibility(SessionVariable sessionVariable, AsyncCallback<Void> callback);

    void isReCreatePhenotypePileLinkNeeded(String publicationID, AsyncCallback callback);

    void copyExpressions(List<ExpressionFigureStageDTO> copyFromExpressions, List<ExpressionFigureStageDTO> copyToExpressions, AsyncCallback<List<ExpressionFigureStageDTO>> callback);

    /**
     * Retrieve all genotypes for a given publication:
     * 1) WT
     * 2)
     * 3)
     *
     * @param publicationID pub ID
     */
    void getGenotypes(String publicationID, AsyncCallback<List<GenotypeDTO>> async);

    void getBackgroundGenotypes(String publicationID, AsyncCallback<List<GenotypeDTO>> callback);

    void getWildTypeFishList(AsyncCallback<List<FishDTO>> async);
}

