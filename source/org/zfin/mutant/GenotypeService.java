package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.repository.FeatureService;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.GenotypeFeatureDTO;
import org.zfin.marker.Marker;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

public class GenotypeService {

    public static final String DELIMITER = ", ";

    public static SortedSet<Marker> getAffectedMarker(Genotype genotype) {
SortedSet<Marker> affectedGenes=new TreeSet<>();
        for (GenotypeFeature genotypeFeature : genotype.getGenotypeFeatures()) {
            Feature feature = genotypeFeature.getFeature();
            SortedSet<Marker> genes = feature.getAffectedGenes();
            for (Marker mkr : genes) {
                if (mkr.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                    affectedGenes.add(mkr);
                }
            }

        }
return  affectedGenes;

    }

    public static void createGenotypeNames(Genotype genotype, List<Genotype> genotypeBackgroundList) {
        if (genotype == null)
            return;
        if (CollectionUtils.isEmpty(genotype.getGenotypeFeatures())) {
            return;
        }
        String displayName = "";
        List<String> handleList = new ArrayList<>();

        for (GenotypeFeature gf : genotype.getGenotypeFeatures()) {
            String handleInt = gf.getFeature().getAbbreviation();
            handleInt += getZygosity(gf.getZygosity(), gf.getMomZygosity(), gf.getDadZygosity());
            handleList.add(handleInt);
            if (gf.getFeature().getAllelicGene() != null) {
                displayName += gf.getFeature().getAllelicGene().getAbbreviation();
                displayName += "<sup>";
                displayName += gf.getZygosity().getMutantZygosityDisplay(gf.getFeature().getDisplayAbbreviation());
                displayName += "</sup>";
                displayName += " ; ";
            } else {
                displayName += gf.getZygosity().getMutantZygosityDisplay(gf.getFeature().getDisplayAbbreviation());
                displayName += " ; ";
            }
        }
        Collections.sort(handleList);
        String handle = "";
        for (String hand : handleList) {
            handle += hand;
            handle += " ";
        }
        handle = handle.trim();
        if (genotypeBackgroundList != null) {
            Collections.sort(genotypeBackgroundList, new Comparator<Genotype>() {
                @Override
                public int compare(Genotype o1, Genotype o2) {
                    return o1.getHandle().compareToIgnoreCase(o2.getHandle());
                }
            });
            for (Genotype genotypeBackground : genotypeBackgroundList) {
                handle += genotypeBackground.getHandle();
                handle += DELIMITER;
            }
            if (genotypeBackgroundList.size() > 0)
                handle = StringUtils.removeEnd(handle, DELIMITER);
        }
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

    public static Genotype createGenotype(List<GenotypeFeatureDTO> genotypeFeatureDTOList, List<Genotype> genotypeBackgroundList) {
        // if empty feature list then use the background genotype as the genotype
        if (CollectionUtils.isEmpty(genotypeFeatureDTOList)) {
            if (genotypeBackgroundList.size() > 1)
                throw new IllegalStateException("Can not have more than one background genotype when no feature is given");
            return genotypeBackgroundList.get(0);
        }
        Genotype genotype = new Genotype();
        genotype.setWildtype(false);
        if (genotypeBackgroundList != null)
            for (Genotype genotypeBackground : genotypeBackgroundList)
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
        GenotypeService.createGenotypeNames(genotype, genotypeBackgroundList);
        return genotype;
    }


}
