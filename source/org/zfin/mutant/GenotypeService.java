package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.gwt.root.dto.GenotypeFeatureDTO;
import org.zfin.marker.Marker;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.mutant.presentation.GenotypeInformation;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

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

    public static SortedSet<Marker> getAffectedMarker(Genotype genotype) {
        Set<GenotypeFeature> features = genotype.getGenotypeFeatures();
        SortedSet<Marker> markers = new TreeSet<Marker>();
        for (GenotypeFeature feat : features) {
            Feature feature = feat.getFeature();
            Set<FeatureMarkerRelationship> rels = feature.getFeatureMarkerRelations();
            for (FeatureMarkerRelationship rel : rels) {
                if (rel.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                    Marker marker = rel.getMarker();
                    // Only add true genes
                    if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                        markers.add(marker);
                    }
                }
            }
        }
        return markers;
    }

    public static void createGenotypeNames(Genotype genotype, Genotype genotypeBackground) {
        if (genotype == null)
            return;
        if (CollectionUtils.isEmpty(genotype.getGenotypeFeatures())) {
            return;
        }
        String displayName = "";
        String handle = "";
        for (GenotypeFeature gf : genotype.getGenotypeFeatures()) {
            handle += gf.getFeature().getAbbreviation();
            handle += getZygosity(gf.getZygosity(), gf.getMomZygosity(), gf.getDadZygosity());
            handle += " ";
            displayName += gf.getFeature().getAllelicGene().getAbbreviation();
            displayName += "<sup>";
            displayName += gf.getZygosity().getMutantZygosityDisplay(gf.getFeature().getDisplayAbbreviation());
            displayName += "</sup>";
            displayName += " ; ";
        }
        handle = handle.trim();
        if (genotypeBackground != null)
            handle += genotypeBackground.getHandle();
        genotype.setHandle(handle);
        genotype.setNickname(handle);
        genotype.setName(displayName.substring(0, displayName.length() - 3));
    }

    private static String getZygosity(Zygosity zygosity, Zygosity momZygosity, Zygosity dadZygosity) {
        String zygosityAbbreviation = "[";
        zygosityAbbreviation += zygosity.getType().getSymbol();
        zygosityAbbreviation += ",";
        zygosityAbbreviation += momZygosity.getType().getSymbol();
        zygosityAbbreviation += ",";
        zygosityAbbreviation += dadZygosity.getType().getSymbol();
        zygosityAbbreviation += "]";
        return zygosityAbbreviation;
    }

    public static Genotype createGenotype(List<GenotypeFeatureDTO> genotypeFeatureDTOList, Genotype genotypeBackground) {
        Genotype genotype = new Genotype();
        genotype.setWildtype(false);
        genotype.setBackground(genotypeBackground);
        Set<GenotypeFeature> genotypeFeatureSet = new HashSet<>(genotypeFeatureDTOList.size());
        for (GenotypeFeatureDTO dto : genotypeFeatureDTOList) {
            GenotypeFeature genotypeFeature = new GenotypeFeature();
            genotypeFeature.setGenotype(genotype);
            genotypeFeature.setZygosity(getMutantRepository().getZygosity(dto.getZygosity().getZdbID()));
            genotypeFeature.setMomZygosity(getMutantRepository().getZygosity(dto.getMaternalZygosity().getZdbID()));
            genotypeFeature.setDadZygosity(getMutantRepository().getZygosity(dto.getPaternalZygosity().getZdbID()));
            genotypeFeature.setFeature(getFeatureRepository().getFeatureByID(dto.getFeatureDTO().getZdbID()));
            genotypeFeatureSet.add(genotypeFeature);
        }
        genotype.setGenotypeFeatures(genotypeFeatureSet);
        GenotypeService.createGenotypeNames(genotype, genotypeBackground);
        return genotype;
    }


}
