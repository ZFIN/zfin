package org.zfin.mutant;

import org.zfin.feature.Feature;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.mutant.presentation.GenotypeInformation;

import java.util.ArrayList;
import java.util.List;

public class GenotypeService {

    public static List<GenotypeInformation> getGenotypeInfo(List<Genotype> genotypes) {
        if (genotypes == null)
            return null;
        List<GenotypeInformation> genotypeInformations = new ArrayList<>();
        for (Genotype genoType : genotypes) {
            GenotypeInformation genotypeInformation = new GenotypeInformation(genoType);
            genotypeInformations.add(genotypeInformation);
        }
        return genotypeInformations;
    }

    public static List<GenoExpStatistics> getGenotypeExpStats(List<Genotype> genotypes, Feature fr) {
        if (genotypes == null || fr == null)
            return null;

        List<GenoExpStatistics> stats = new ArrayList<>();
        for (Genotype genoType : genotypes) {
            GenoExpStatistics stat = new GenoExpStatistics(genoType, fr);
            stats.add(stat);
        }
        return stats;
    }
}
