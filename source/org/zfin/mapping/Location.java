package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.GenericTerm;

/**
 * Feature Location .
 */
@Setter
@Getter
public class Location {

    protected String zdbID;
    protected Integer startLocation;
    protected Integer endLocation;
    protected String chromosome;
    protected String assembly;
    protected GenericTerm locationEvidence;
    protected String referenceSequenceAccessionNumber;

}
