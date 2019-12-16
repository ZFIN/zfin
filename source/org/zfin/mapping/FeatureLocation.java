package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;
import org.zfin.ontology.GenericTerm;

/**
 * Feature Location .
 */
@Setter
@Getter
public class FeatureLocation  {

    private String zdbID;
    private Feature feature;
    private Integer ftrStartLocation;
    private Integer ftrEndLocation;
    private String ftrChromosome;
    private String ftrAssembly;
    private GenericTerm ftrLocEvidence;
    private String referenceSequenceAccessionNumber;


}
