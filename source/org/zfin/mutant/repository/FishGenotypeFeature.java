package org.zfin.mutant.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.zfin.mutant.Fish;
import org.zfin.mutant.GenotypeFeature;

@Setter
@Getter
@AllArgsConstructor
public class FishGenotypeFeature {

    private Fish fish;
    private GenotypeFeature genotypeFeature;

}
