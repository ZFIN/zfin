package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.GenericTerm;

/**
 * Feature Location .
 */
@Setter
@Getter
public class GenericChromosomalLocation {

    protected String zdbID;
    protected Integer ftrStartLocation;
    protected Integer ftrEndLocation;
    protected String ftrChromosome;
    protected String ftrAssembly;
    protected GenericTerm ftrLocEvidence;
    protected String referenceSequenceAccessionNumber;

}
