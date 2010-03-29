package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 * RPC Async Class for the phenotype curation module.
 */
public interface CurationPhenotypeRPCAsync {

    void getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID, AsyncCallback<List<PhenotypeFigureStageDTO>> async);

    void createMutantFigureStages(List<PhenotypeFigureStageDTO> newFigureAnnotations, AsyncCallback<List<PhenotypeFigureStageDTO>> callback);

    void deleteFigureAnnotation(PhenotypeFigureStageDTO figureAnnotation, AsyncCallback callback);

    void updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> updateEntity, AsyncCallback<List<PhenotypeFigureStageDTO>> callback);

    /**
     * Checks if the phenotype structure pile needs to be recreated.
     * Yes, if
     * 1) publication is open
     * 2) one or more mutant records with non-unspecifiec structures exist
     * 3) phenotype pile is empty
     * @param publicationID publication
     * @param callback callback
     */
    void isReCreatePhenotypePileLinkNeeded(String publicationID, AsyncCallback<Boolean> callback);
}