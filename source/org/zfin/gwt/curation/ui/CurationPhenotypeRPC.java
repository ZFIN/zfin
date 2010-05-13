package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.PhenotypeFigureStageDTO;
import org.zfin.gwt.root.dto.PileStructureAnnotationDTO;

import java.util.List;

/**
 * GWT interface to handle event for the phenotype tab page.
 */
public interface CurationPhenotypeRPC extends RemoteService {

    public static class App {
        private static final CurationPhenotypeRPCAsync INSTANCE;

        static {
            INSTANCE = (CurationPhenotypeRPCAsync) GWT.create(CurationPhenotypeRPC.class);
            ((ServiceDefTarget) INSTANCE).setServiceEntryPoint("/ajax/curation-phenotype");
        }

        public static CurationPhenotypeRPCAsync getInstance() {
            return INSTANCE;
        }
    }

    List<PhenotypeFigureStageDTO> getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID);

    List<PhenotypeFigureStageDTO> createMutantFigureStages(List<PhenotypeFigureStageDTO> newFigureAnnotations);

    void deleteFigureAnnotation(PhenotypeFigureStageDTO figureAnnotation);

    List<PhenotypeFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, PhenotypeFigureStageDTO> updateEntity);

    /**
     * Checks if the phenotype structure pile needs to be recreated.
     * Yes, if
     * 1) publication is open
     * 2) one or more mutant records with non-unspecifiec structures exist
     *
     * @param publicationID publication
     */
    boolean isReCreatePhenotypePileLinkNeeded(String publicationID);

}
