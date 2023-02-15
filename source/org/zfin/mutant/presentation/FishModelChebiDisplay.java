package org.zfin.mutant.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.GenericTerm;

@Setter
@Getter
public class FishModelChebiDisplay extends FishModelDisplay {

    private GenericTerm chebiTerm;
}
