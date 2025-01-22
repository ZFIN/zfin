package org.zfin.mutant;

import lombok.Data;

import java.io.Serializable;

@Data
public class GeneGenotypeExperimentId implements Serializable {
    private String markerId;

    private String fishExperimentId;

}
