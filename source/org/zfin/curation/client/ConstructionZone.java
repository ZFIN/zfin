package org.zfin.curation.client;

import org.zfin.framework.presentation.dto.ExpressedTermDTO;
import org.zfin.framework.presentation.client.PostComposedPart;
import org.zfin.framework.presentation.client.Ontology;

/**
 * Defines a Construction zone, such as for FX and PATO.
 */
public interface ConstructionZone {

    /**
     * This method takes an ExpressedTermDTO and pre-populates the construction
     * zone with the given entities. The PostComposedPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    void prepopulateConstructionZone(ExpressedTermDTO term, PostComposedPart selectedEntity);

    /**
     * Sets the construction zone to the default setting:
     * 1. all postcomposed parts set to an emtpy string
     * 2. set ontology selectors to AO
     * 3. displays the default structure in the term info box (currently AO:anatomical structure)
     */
    void resetConstructionZone();

    /**
     * Display the term info for a given term in a given ontology.
     *
     * @param ontology Ontology
     * @param termID   term ID: zdb ID or obo ID
     */
    void showTermInfo(Ontology ontology, String termID);

    /**
     * Set the object that validated a new structure according to given business logic.
     * @param validator StructureValidator
     */
    void setStructureValidator(StructureValidator validator);

    /**
     * Add a change handler.
     * @param listener handler
     */
    void addCreatePileChangeListener(PileStructureListener listener);
}
