package org.zfin.mapping;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getLinkageRepository;

public class MappingService {

    public static final String AMBIGUOUS = "Ambiguous";

    public static Map<String, Map<String, Long>> getChromosomePanelCountMap(Panel panel) {
        List<PanelCount> panelCountList = getLinkageRepository().getPanelCount(panel);
        Map<String, Map<String, Long>> returnMap = new TreeMap<>(new ChromosomeComparator());

        for (PanelCount panelCount : panelCountList) {
            Map<String, Long> chromosomeMap = returnMap.get(panelCount.getLg());
            if (chromosomeMap == null) {
                chromosomeMap = new TreeMap<>();
                returnMap.put(panelCount.getLg(), chromosomeMap);
            }
            chromosomeMap.put(panelCount.getMarkerType(), panelCount.getCount());
        }
        return returnMap;
    }

    public static Map<String, Long> getStatisticMap(Panel panel) {
        Map<String, Long> statisticsMap = new TreeMap<>();
        Map<String, Map<String, Long>> chromosomePanelCountMap = getChromosomePanelCountMap(panel);
        for (String chromosome : chromosomePanelCountMap.keySet()) {
            Map<String, Long> chromosomeMap = chromosomePanelCountMap.get(chromosome);
            for (String markerType : chromosomeMap.keySet()) {
                Long number = statisticsMap.get(markerType);
                if (number == null) {
                    statisticsMap.put(markerType, chromosomeMap.get(markerType));
                } else
                    statisticsMap.put(markerType, number + chromosomeMap.get(markerType));
            }

        }
        return statisticsMap;
    }

    public static long getTotalNumberOfMarker(Panel panel) {
        Map<String, Long> statMap = getStatisticMap(panel);
        long total = 0;
        for (String markerType : statMap.keySet()) {
            total += statMap.get(markerType);
        }
        return total;
    }

    public static String getChromosomeLocationDisplay(ZdbID entity) {
        if (entity instanceof Marker) {
            return getChromosomeLocationDisplay(getLinkageRepository().getGenomeLocation((Marker) entity));
        }
        if (entity instanceof Feature) {
            return getChromosomeLocationDisplay(getLinkageRepository().getGenomeLocation((Feature) entity));
        }
        return "not yet implemented for " + entity.getClass().getName();
    }

    public static String getChromosomeLocationDisplay(List<? extends GenomeLocation> list) {
        if (CollectionUtils.isEmpty(list))
            return null;
        Set<String> chromosomeList = new HashSet<>();
        String chromosome;
        for (GenomeLocation location : list) {
            if (location.getSource().isPhysicalMappingLocation()) {
                chromosomeList.add(location.getChromosome());
            }
        }
        chromosome = getChromosomeNumber(chromosomeList);
        if (StringUtils.isNotEmpty(chromosome))
            return chromosome;

        chromosomeList.clear();
        for (GenomeLocation location : list) {
            if (location.getSource().is2ndDegreePhysicalMappingLocation()) {
                chromosomeList.add(location.getChromosome());
            }
        }
        chromosome = getChromosomeNumber(chromosomeList);
        if (StringUtils.isNotEmpty(chromosome)) {
            if (chromosome.equals("0"))
                return "unknown";
            else
                return chromosome;
        }

        chromosomeList.clear();
        for (GenomeLocation location : list) {
            if (location.getSource().isGeneticMappingLocation()) {
                chromosomeList.add(location.getChromosome());
            }
        }
        chromosome = getChromosomeNumber(chromosomeList);
        if (StringUtils.isNotEmpty(chromosome))
            return chromosome;

        return "";
    }

    public static final String DELIMITER = ", ";

    public static String getChromosomeNumber(Set<String> chromosomeList) {
        if (CollectionUtils.isEmpty(chromosomeList))
            return "";
        if (chromosomeList.size() > 1)
            return AMBIGUOUS;
        return chromosomeList.iterator().next();
    }


    /**
     * Return a list of genome locations, one per source.
     *
     * @param marker
     * @return
     */
    public static List<GenomeLocation> getGenomeBrowserLocations(Marker marker) {
        List<MarkerGenomeLocation> genomeLocationList = getLinkageRepository().getPhysicalGenomeLocations(marker);
        return sortAndFilterGenomeBrowserLocations(genomeLocationList);
    }

    public static List<GenomeLocation> getGenomeBrowserLocations(Feature feature) {
        List<FeatureGenomeLocation> genomeLocationList = getLinkageRepository().getPhysicalGenomeLocations(feature);
        return sortAndFilterGenomeBrowserLocations(genomeLocationList);
    }

    private static List<GenomeLocation> sortAndFilterGenomeBrowserLocations(List<? extends GenomeLocation> genomeLocationList) {
        Collections.sort(genomeLocationList);
        List<GenomeLocation> finalGenomeList = new ArrayList<>(genomeLocationList.size());
        for (GenomeLocation genomeLocation : genomeLocationList) {
            GenomeLocation.Source source = genomeLocation.getSource();
            if (source != GenomeLocation.Source.GENERAL_LOAD) {
                finalGenomeList.add(genomeLocation);
            }
        }
        return finalGenomeList;
    }
}



