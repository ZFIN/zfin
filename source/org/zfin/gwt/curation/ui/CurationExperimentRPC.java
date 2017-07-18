package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.curation.dto.UpdateExpressionDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.gwt.root.util.StageRangeIntersectionService;

import java.util.List;

/**
 * GWT class to facilitate curation of FX
 */
public interface CurationExperimentRPC extends RemoteService {

    public List<ExpressionExperimentDTO> getExperimentsByFilter(ExpressionExperimentDTO experimentFilter);

    /**
     * Retrieve all genotypes for a given publication:
     * 1) WT
     * 2)
     * 3)
     *
     * @param publicationID pub ID
     */
    List<GenotypeDTO> getGenotypes(String publicationID);

    /**
     * Retrieve all genotypes for a given publication:
     * 1) WT
     * 2)
     * 3)
     *
     * @param publicationID pub ID
     */
    List<FishDTO> getFishList(String publicationID);

    /**
     * Update an existing experiment.
     *
     * @param selectedExperiment experiment to be updated
     */
    ExpressionExperimentDTO updateExperiment(ExpressionExperimentDTO selectedExperiment);

    /**
     * Check the visibility of the experiment section
     */
    boolean readExperimentSectionVisibility(String publicationID);

    /**
     * Set Experiment Section visibility.
     *
     * @param pubID                publication ID
     * @param experimentVisibility experiment section visibility
     */
    void setExperimentVisibilitySession(String pubID, boolean experimentVisibility);

    /**
     * Check if for a given publication there is no structure pile available.
     * It's been used to decide if a link to re-create the structure pile should be displayed.
     *
     * @param publicationID publication ID
     * @return boolean
     */
    boolean isReCreatePhenotypePileLinkNeeded(String publicationID);

    List<RelatedEntityDTO> getBackgroundGenotypes(String publicationID);

    List<FishDTO> getWildTypeFishList();

    /**
     * Utility/Convenience class.
     * Use CurationExperimentRPC.App.getInstance() to access static instance of CurationExperimentRPCAsync
     */
    public static class App {
        private static final CurationExperimentRPCAsync INSTANCE;

        static {
            INSTANCE = (CurationExperimentRPCAsync) GWT.create(CurationExperimentRPC.class);
            ((ServiceDefTarget) INSTANCE).setServiceEntryPoint("/ajax/curation");
        }

        public static CurationExperimentRPCAsync getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Retrieve all figures that are available for this publication
     *
     * @param publicationID string
     * @return list of figures
     */
    public List<String> readFigures(String publicationID);

    /**
     * Retrieve the genes that are attributed to a given pbulication
     *
     * @param pubID pub id
     * @return marker dto
     * @throws PublicationNotFoundException
     */
    public List<MarkerDTO> getGenes(String pubID) throws PublicationNotFoundException;

////////// Expression Section

    /**
     * Checks if the expression section should be hidden or displayed
     *
     * @param publicationID Publication
     * @return boolean
     */
    public boolean readExpressionSectionVisibility(String publicationID);

    /**
     * Retrieve all expression records according to a given filter.
     *
     * @param experimentFilter filter object
     * @return expression figure stage records
     */
    List<ExpressionFigureStageDTO> getExpressionsByFilter(ExpressionExperimentDTO experimentFilter, String figureID);

    /**
     * Retrieve all Figures associated to a given publication.
     *
     * @param publicationID publication
     * @return list of figure dtos
     */
    List<FigureDTO> getFigures(String publicationID);

    /**
     * Retrieve all development stages.
     *
     * @return list of stages
     */
    List<StageDTO> getStages();

    /**
     * Create multiple figure annotations.
     *
     * @param figureAnnotations figure annotations
     */
    List<ExpressionFigureStageDTO> createFigureAnnotations(List<ExpressionFigureStageDTO> figureAnnotations);

    List<ExpressionFigureStageDTO> copyExpressions(List<ExpressionFigureStageDTO> copyFromExpressions,
                                                   List<ExpressionFigureStageDTO> copyToExpressions)
            throws ValidationException;

    /**
     * Delete a figure annotation.
     *
     * @param figureAnnotation figure annotation
     */
    void deleteFigureAnnotation(ExpressionFigureStageDTO figureAnnotation);

    void setExpressionVisibilitySession(String publicationID, boolean visible);

    /**
     * Retrieve the figure for the filter on the fx page.
     *
     * @param publicationID publication
     */
    FigureDTO getFigureFilter(String publicationID);

    /**
     * Retrieve the gene for the filter on the fx page
     *
     * @param publicationID publication
     * @return marker DTO
     */
    MarkerDTO getGeneFilter(String publicationID);

    /**
     * Create a new Pato record
     *
     * @param efs figure annotation
     */
    void createPatoRecord(ExpressionFigureStageDTO efs);

    /**
     * Set the check mark status of a given figure annotation.
     *
     * @param checkedExpression figure annotation
     * @param checked           true or false (= checked or unchecked)
     */
    void setFigureAnnotationStatus(ExpressionFigureStageDTO checkedExpression, boolean checked);

    /**
     * Read the check mark status.
     *
     * @param publicationID Publication
     */
    CheckMarkStatusDTO getFigureAnnotationCheckmarkStatus(String publicationID);

    /**
     * Check if the structure section should be hidden or displayed.
     *
     * @param publicationID publication id
     * @return show: true of false
     */
    boolean readStructureSectionVisibility(String publicationID, boolean isPhenotype);

    /**
     * Set the visibility status for the structure section.
     *
     * @param publicationID publication id
     * @param show          true or false
     */
    void setStructureVisibilitySession(String publicationID, boolean show, boolean isPhenotype);

    /**
     * Retrieve all structures on the structure pile.
     *
     * @param publicationID Publication ID
     * @return list fo structure objects
     */
    List<ExpressionPileStructureDTO> getStructures(String publicationID);

    /**
     * Update individual figure annotations with structures from the pile.
     *
     * @param updateEntity Update Expression dto
     * @return list of updated expression figure stage dtos
     */
    List<ExpressionFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO<PileStructureAnnotationDTO, ExpressionFigureStageDTO> updateEntity)
            throws ValidationException;

    /**
     * Retrieve a list of structures that could be used instead of the selected
     * structure with a stage overlap given by start and end.
     *
     * @param selectedPileStructure pile structure
     * @param intersection          intersection
     * @return list of PileStructureDTO,
     */
    List<RelatedPileStructureDTO> getTermsWithStageOverlap(ExpressionPileStructureDTO selectedPileStructure,
                                                           StageRangeIntersectionService intersection);


    /**
     * Save a given session variable in Application session.
     *
     * @param sessionVariable session variable
     */
    void saveSessionVisibility(SessionVariable sessionVariable);

}
