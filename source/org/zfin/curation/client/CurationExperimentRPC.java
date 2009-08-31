package org.zfin.curation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.curation.dto.*;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface CurationExperimentRPC extends RemoteService {

    public List<ExperimentDTO> getExperimentsByFilter(ExperimentDTO experimentFilter);

    /**
     * Retrieve a list of all fish, figures and genes that are used in the experiment section.
     *
     * @param publicationID Publication
     * @return list of fish
     */
    public FilterValuesDTO getPossibleFilterValues(String publicationID) throws PublicationNotFoundException;

    public List<String> getAssays();

    public List<EnvironmentDTO> getEnvironments(String publicationID);

    /**
     * Retrieve all genotypes for a given publication:
     * 1) WT
     * 2)
     * 3)
     *
     * @param publicationID pub ID
     */
    List<FishDTO> getGenotypes(String publicationID);

    /**
     * Retrieve antibodies that are attributed to a given publication
     */
    List<MarkerDTO> getAntibodies(String publicationID);

    /**
     * Retrieve antibodies for a given publication and gene.
     *
     * @param publicationID String
     * @param geneID        string
     */
    public List<MarkerDTO> readAntibodiesByGene(String publicationID, String geneID);

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     */
    List<MarkerDTO> readGenesByAntibody(String publicationID, String antibodyID) throws PublicationNotFoundException;

    /**
     * Retrieve the accession numbers for a given gene
     *
     * @param geneID string
     */
    List<ExperimentDTO> readGenbankAccessions(String publicationID, String geneID);

    /**
     * Update an existing experiment.
     *
     * @param selectedExperiment experiment to be updated
     */
    ExperimentDTO updateExperiment(ExperimentDTO selectedExperiment);

    /**
     * Create a new expression experiment.
     *
     * @param experiment experiment
     */
    ExperimentDTO createExpressionExperiment(ExperimentDTO experiment) throws Exception;

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
     * Utility/Convenience class.
     * Use CurationExperimentRPC.App.getInstance() to access static instance of CurationExperimentRPCAsync
     */
    public static class App {
        private static final CurationExperimentRPCAsync ourInstance;

        static {
            ourInstance = (CurationExperimentRPCAsync) GWT.create(CurationExperimentRPC.class);
            ((ServiceDefTarget) ourInstance).setServiceEntryPoint("/ajax/curation");
        }

        public static CurationExperimentRPCAsync getInstance() {
            return ourInstance;
        }
    }

    /**
     * Delete an experiment and all related records.
     *
     * @param experimentZdbID
     */
    public void deleteExperiment(String experimentZdbID);

    /**
     * Read all experiments that are available for a given publication.
     *
     * @param publicationID publication
     */
    public List<ExperimentDTO> readExperiments(String publicationID);


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
    List<ExpressionFigureStageDTO> getExpressionsByFilter(ExperimentDTO experimentFilter, String figureID);

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

    /**
     * Delete a figure annotation.
     *
     * @param figureAnnotation figure annotation
     */
    void deleteFigureAnnotation(ExpressionFigureStageDTO figureAnnotation);

    void setExpressionVisibilitySession(String publicationID, boolean b);

    /**
     * Save the filter element zdb ID
     *
     * @param publicationID publication
     * @param zdbID         zdbID
     */
    void setFilterType(String publicationID, String zdbID, String type);

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
     * Retrieve the fish if for the fx filter bar
     *
     * @param publicationID publication
     * @return Fish dto
     */
    FilterValuesDTO getFilterValues(String publicationID);

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
     * Read the checkmark status.
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
    boolean readStructureSectionVisibility(String publicationID);

    /**
     * Set the visibility status for the structure section.
     *
     * @param publicationID publication id
     * @param show          true or false
     */
    void setStructureVisibilitySession(String publicationID, boolean show);

    /**
     * Retrieve all structures on the structure pile.
     *
     * @param publicationID Publication ID
     * @return list fo structure objects
     */
    List<PileStructureDTO> getStructures(String publicationID);

    /**
     * Remove a structure from the structure pile.
     *
     * @param structure Structure DTO
     */
    void deleteStructure(PileStructureDTO structure);

    /**
     * Update inidividual figure annotations with structures from the pile.
     *
     * @param updateEntity Update Expression dto
     * @return list of updated expression figure stage dtos
     */
    List<ExpressionFigureStageDTO> updateStructuresForExpression(UpdateExpressionDTO updateEntity);

    /**
     * Retrieve a list of structures that could be used instead of the selected
     * structure with a stage overlap given by start and end.
     * @param selectedPileStructure pilse structure
     * @param intersection intersection
     * @return list of PileStructureDTO,
     */
    List<RelatedPileStructureDTO> getTermsWithStageOverlap(PileStructureDTO selectedPileStructure,
                                                    StageRangeIntersection intersection);

    /**
     * Create a new structure for the pile.
     * @param expressedTerm  Expressed Term dto
     * @param publicationID pub id
     */
    PileStructureDTO createPileStructure(ExpressedTermDTO expressedTerm, String publicationID)
            throws PileStructureExistsException, TermNotFoundException;


}
