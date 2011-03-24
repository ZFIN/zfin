package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.*;

/**
 * Defines a Construction zone, such as for FX and PATO.
 */
public interface ConstructionZone {

    /**
     * This method takes an ExpressedTermDTO and pre-populates the construction
     * zone with the given entities. The EntityPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    void prepopulateConstructionZone(ExpressedTermDTO term, EntityPart selectedEntity);

    /**
     * This method takes an PhenotypeTermDTO and pre-populates the construction
     * zone with the given entities. The EntityPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    void prepopulateConstructionZoneWithPhenotype(PhenotypeStatementDTO term, EntityPart selectedEntity);

    /**
     * Sets the construction zone to the default setting:
     * 1. all post-composed parts set to an empty string
     * 2. set ontology selectors to AO
     * 3. displays the default structure in the term info box (currently AO:anatomical structure)
     */
    void resetConstructionZone();

    /**
     * Set the object that validated a new structure according to given business logic.
     *
     * @param validator StructureValidator
     */
    void setStructureValidator(StructureValidator validator);

    /**
     * Add a change handler.
     *
     * @param listener handler
     */
    void addCreatePileChangeListener(PileStructureListener listener);
}
