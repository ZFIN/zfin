package org.zfin.curation.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.curation.dto.ExperimentDTO;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface CurationExperimentRPC extends RemoteService {

    public List<ExperimentDTO> getExperimentsByFilter(ExperimentDTO experimentFilter);

    /**
     * Retrieve a list of all fish that are used in the experiment section.
     *
     * @param publicationID  Publication
     * @return list of fish
     */
    public List<FishDTO> getFish(String publicationID);

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
     * @param geneID        string
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
     * @param experiment    experiment
     */
    ExperimentDTO createExpressionExperiment(ExperimentDTO experiment) throws Exception;

    /**
     * Check the visibility of the experiment section
     *
     */
    boolean readExperimentSectionVisibility(String publicationID);

    /**
     * Set Experiment Section visibility.
     *
     * @param pubID  publication ID
     * @param experimentVisibility           experiment section visibility
     */
    void setCuratorSession(String pubID, boolean experimentVisibility);

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
     * @param pubID pub id
     * @return marker dto
     * @throws PublicationNotFoundException
     */
    public List<MarkerDTO> getGenes(String pubID) throws PublicationNotFoundException;

}
