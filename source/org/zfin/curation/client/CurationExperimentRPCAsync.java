package org.zfin.curation.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.curation.dto.ExperimentDTO;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface CurationExperimentRPCAsync {

    void getExperimentsByFilter(ExperimentDTO experimentFilter, AsyncCallback<List<ExperimentDTO>> async);

    /**
     * Retrieve a list of all fish that are used in the experiment section.
     *
     * @param publicationID Publication
     * @param async callback
     */
    void getFish(String publicationID, AsyncCallback<List<FishDTO>> async);

    void getAssays(AsyncCallback<List<String>> async);

    void getEnvironments(String publicationID, AsyncCallback<List<EnvironmentDTO>> async);

    /**
     * Retrieve all genotypes for a given publication:
     * 1) WT
     * 2)
     * 3)
     *
     * @param publicationID pub ID
     * @param async callback
     */
    void getGenotypes(String publicationID, AsyncCallback<List<FishDTO>> async);

    /**
     * Retrieve antibodies that are attributed to a given publication
     * @param publicationID pubID
     * @param async callback
     */
    void getAntibodies(String publicationID, AsyncCallback<List<MarkerDTO>> async);

    /**
     * Retrieve antibodies for a given publication and gene.
     *
     * @param publicationID String
     * @param geneID        string
     * @param async callback
     */
    void readAntibodiesByGene(String publicationID, String geneID, AsyncCallback<List<MarkerDTO>> async);

    /**
     * Retrieve list of associated genes for given pub and antibody
     *
     * @param publicationID String
     * @param antibodyID    string
     * @param async callback
     */
    void readGenesByAntibody(String publicationID, String antibodyID, AsyncCallback<List<MarkerDTO>> async);

    /**
     * Retrieve the accession numbers for a given gene
     *
     * @param publicationID pubID
     * @param geneID string
     * @param async callback
     */
    void readGenbankAccessions(String publicationID, String geneID, AsyncCallback<List<ExperimentDTO>> async);

    /**
     * Update an existing experiment.
     *
     * @param selectedExperiment experiment to be updated
     * @param async callback
     */
    void updateExperiment(ExperimentDTO selectedExperiment, AsyncCallback<ExperimentDTO> async);

    /**
     * Create a new expression experiment.
     *
     * @param experiment experiment
     * @param async callback
     */
    void createExpressionExperiment(ExperimentDTO experiment, AsyncCallback<ExperimentDTO> async);

    /**
     * Check the visibility of the experiment section
     * @param publicationID  pub ID
     * @param async callback
     */
    void readExperimentSectionVisibility(String publicationID, AsyncCallback<Boolean> async);

    /**
     * Set Experiment Section visibility.
     *
     * @param pubID publication ID
     * @param experimentVisibility experiment section visibility
     * @param async call back
     */
    void setCuratorSession(String pubID, boolean experimentVisibility, AsyncCallback<Void> async);

    void deleteExperiment(String experimentZdbID, AsyncCallback<Void> async);

    /**
     * Read all experiments that are available for a given publication.
     *
     * @param publicationID publication
     * @param async call back
     */
    void readExperiments(String publicationID, AsyncCallback<List<ExperimentDTO>> async);

    /**
     * Retrieve all figures that are available for this publication
     *
     * @param publicationID string
     * @param async callback
     */
    void readFigures(String publicationID, AsyncCallback<List<String>> async);

    void getGenes(String pubID, AsyncCallback<List<MarkerDTO>> async);
}
