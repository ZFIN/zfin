package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface PileStructuresRPC extends RemoteService {

    /**
     * Retrieve all phenotype structures on the structure pile.
     *
     * @param publicationID Publication ID
     * @return list fo structure objects
     */
    List<PhenotypePileStructureDTO> getPhenotypePileStructures(String publicationID);

    /**
     * Remove a phenotype structure from the pile.
     *
     * @param structure PhenotypePileStructureDTO
     */
    PhenotypePileStructureDTO deletePhenotypeStructure(PhenotypePileStructureDTO structure);

    /**
     * Create a new structure for the pile.
     *
     * @param expressedTerm Expressed Term dto
     * @param publicationID pub id
     */
    ExpressionPileStructureDTO createPileStructure(ExpressedTermDTO expressedTerm, String publicationID)
            throws PileStructureExistsException, TermNotFoundException;

    /**
     * Create a new phenotype structure for the pile.
     *
     * @param phenotypeTermDTO Phenotype Term dto
     * @param publicationID    pub id
     */
    PhenotypePileStructureDTO createPhenotypePileStructure(PhenotypeStatementDTO phenotypeTermDTO, String publicationID)
            throws PileStructureExistsException, TermNotFoundException, RelatedEntityNotFoundException, InvalidPhenotypeException;


    /**
     * Remove a structure from the structure pile.
     *
     * @param structure Structure DTO
     */
    ExpressionPileStructureDTO deleteStructure(ExpressionPileStructureDTO structure);

    /**
     * Re-create the phenotype structure pile.
     *
     * @param publicationID publication
     */
    List<PhenotypePileStructureDTO> recreatePhenotypeStructurePile(String publicationID);

    /**
     * Re-create the complete structure pile. This is needed in case none of the structures being used
     * in expression records are on the pile.
     *
     * @param publicationID Publication id
     * @return complete structure pile.
     */
    List<ExpressionPileStructureDTO> recreateExpressionStructurePile(String publicationID);


    public static class App {
        private static final PileStructuresRPCAsync INSTANCE;

        static {
            INSTANCE = (PileStructuresRPCAsync) GWT.create(PileStructuresRPC.class);
            ((ServiceDefTarget) INSTANCE).setServiceEntryPoint("/ajax/curation-structures");
        }

        public static PileStructuresRPCAsync getInstance() {
            return INSTANCE;
        }
    }


}
