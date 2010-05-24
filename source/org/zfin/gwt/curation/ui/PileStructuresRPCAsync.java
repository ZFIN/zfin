package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.ExpressionPileStructureDTO;
import org.zfin.gwt.root.dto.PhenotypePileStructureDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;

import java.util.List;

public interface PileStructuresRPCAsync {

    void getPhenotypePileStructures(String publicationID, AsyncCallback retrieveStructuresCallback);

    /**
     * Remove a phenotype structure from the pile.
     *
     * @param structure PhenotypePileStructureDTO
     * @param callback  callback.
     */
    void deletePhenotypeStructure(PhenotypePileStructureDTO structure, AsyncCallback<PhenotypePileStructureDTO> callback);

    /**
     * Create a new FX structure on the structure pile.
     *
     * @param expressedTerm Expression term
     * @param publicationID Publication
     * @param callback      callback
     */
    void createPileStructure(ExpressedTermDTO expressedTerm, String publicationID, AsyncCallback<ExpressionPileStructureDTO> callback);

    /**
     * Create a new phenotype structure on the structure pile.
     *
     * @param phenotypeTerm Expression term
     * @param publicationID Publication
     * @param callback      callback
     */
    void createPhenotypePileStructure(PhenotypeTermDTO phenotypeTerm, String publicationID, AsyncCallback<PhenotypePileStructureDTO> callback);

    void deleteStructure(ExpressionPileStructureDTO structure, AsyncCallback<ExpressionPileStructureDTO> callback);

    /**
     * Re-create the phenotype structure pile.
     * @param publicationID publication
     * @param callback callback
     */
    void recreatePhenotypeStructurePile(String publicationID, AsyncCallback<List<PhenotypePileStructureDTO>> callback);

    void recreateExpressionStructurePile(String publicationID, AsyncCallback<List<ExpressionPileStructureDTO>> callback);
}
