package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.PhenotypeExperimentDTO;
import org.zfin.gwt.root.dto.PileStructureAnnotationDTO;

import java.util.List;

/**
 * RPC Async Class for the phenotype curation module.
 */
public interface CurationPhenotypeRPCAsync {

    void getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID, AsyncCallback<List<PhenotypeExperimentDTO>> async);

    void createPhenotypeExperiments(List<PhenotypeExperimentDTO> newFigureAnnotations, AsyncCallback<List<PhenotypeExperimentDTO>> callback);

    void deleteFigureAnnotation(PhenotypeExperimentDTO figureAnnotation, AsyncCallback callback);

    void updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeExperimentDTO> updateEntity, AsyncCallback<List<PhenotypeExperimentDTO>> callback);

    /**
     * Checks if the phenotype structure pile needs to be recreated.
     * Yes, if
     * 1) publication is open
     * 2) one or more mutant records with non-unspecified structures exist
     * 3) phenotype pile is empty
     * @param publicationID publication
     * @param callback callback
     */
    void isReCreatePhenotypePileLinkNeeded(String publicationID, AsyncCallback<Boolean> callback);
}