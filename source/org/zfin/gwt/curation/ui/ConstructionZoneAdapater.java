package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.PhenotypeTermDTO;
import org.zfin.gwt.root.dto.PostComposedPart;
import org.zfin.gwt.root.ui.HandlesError;

/**
 * Defines a Construction zone, such as for FX and PATO.
 */
public abstract class ConstructionZoneAdapater implements ConstructionZone, HandlesError {

    /**
     * This method takes an ExpressedTermDTO and pre-populates the construction
     * zone with the given entities. The PostComposedPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    public void prepopulateConstructionZone(ExpressedTermDTO term, PostComposedPart selectedEntity) { }

    /**
     * This method takes an PhenotypeTermDTO and pre-populates the construction
     * zone with the given entities. The PostComposedPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    public void prepopulateConstructionZoneWithPhenotype(PhenotypeTermDTO term, PostComposedPart selectedEntity) { }

    /**
     * Sets the construction zone to the default setting:
     * 1. all post-composed parts set to an empty string
     * 2. set ontology selectors to AO
     * 3. displays the default structure in the term info box (currently AO:anatomical structure)
     */
    public void resetConstructionZone() { }

    /**
     * Set the object that validated a new structure according to given business logic.
     *
     * @param validator StructureValidator
     */
    public void setStructureValidator(StructureValidator validator) { }

    /**
     * Add a change handler.
     *
     * @param listener handler
     */
    public void addCreatePileChangeListener(PileStructureListener listener) { }

}