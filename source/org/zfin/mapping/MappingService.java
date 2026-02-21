package org.zfin.mapping;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.jbrowse.presentation.JBrowse2Image;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ZdbID;
import org.zfin.mapping.presentation.BrowserLink;
import org.zfin.marker.Marker;
import org.zfin.sequence.gff.Assembly;
import org.zfin.sequence.gff.AssemblyDAO;

import java.util.*;
import java.util.stream.Collectors;

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
        if (entity == null) {
            return "Error null entity";
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

    public static Map<String, Integer> assemblyOrder = (new AssemblyDAO()).findAllSortedAssemblies().stream()
        .collect(Collectors.toMap(Assembly::getName, Assembly::getOrder));

    private static List<GenomeLocation> sortAndFilterGenomeBrowserLocations(List<? extends GenomeLocation> genomeLocationList) {
        Collections.sort(genomeLocationList,
            Comparator.comparing(location -> assemblyOrder.getOrDefault(((GenomeLocation) location).getAssembly(), Integer.MAX_VALUE))
                .thenComparing(location -> ((GenomeLocation) location).getSource()));
        List<GenomeLocation> finalGenomeList = new ArrayList<>(genomeLocationList.size());
        for (GenomeLocation genomeLocation : genomeLocationList) {
            GenomeLocation.Source source = genomeLocation.getSource();
            if (source != GenomeLocation.Source.GENERAL_LOAD) {
                finalGenomeList.add(genomeLocation);
            }
        }
        return finalGenomeList;
    }

    public static List<MarkerLocation> getMarkerLocation(String zdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from MarkerLocation where marker.zdbID = :zdbID order by zdbID";
        Query<MarkerLocation> query = session.createQuery(hql, MarkerLocation.class);
        query.setParameter("zdbID", zdbID);
        return query.list();
    }

    public static TreeSet<BrowserLink> getJBrowserBrowserLinks(List<MarkerGenomeLocation> genomeMarkerLocationList, JBrowse2Image genomeBrowserImage, Assembly assembly) {
        TreeSet<BrowserLink> locations = new TreeSet<>();
        for (MarkerGenomeLocation genomeMarkerLocation : genomeMarkerLocationList) {
            BrowserLink location = new BrowserLink();
            location.setUrl(genomeMarkerLocation.getUrl(genomeBrowserImage));
            if (assembly != null) {
                switch (assembly.getName()) {
                    case "GRCz12tu": {
                        switch (genomeMarkerLocation.getSource()) {
                            case ZFIN_NCBI -> {
                                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                                location.setOrder(0);
                            }
                            case NCBI_LOADER -> {
                                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                                location.setOrder(1);
                            }
                            default -> {
                            }
                        }
                        break;
                    }
                    case "GRCz11": {
                        switch (genomeMarkerLocation.getSource()) {
                            case ZFIN -> {
                                location.setName("ZFIN");
                                location.setOrder(0);
                            }
                            case ENSEMBL -> {
                                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                                location.setOrder(1);
                            }
                            case NCBI -> {
                                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                                location.setOrder(2);
                            }
                            case UCSC -> {
                                location.setName(genomeMarkerLocation.getSource().getDisplayName());
                                location.setOrder(3);
                            }
                            default -> {
                            }
                        }
                    }
                    ;
                    default:
                }
                locations.add(location);
            }
        }
        return locations;
    }

    public static TreeSet<BrowserLink> getJBrowserBrowserLinksForClones(JBrowse2Image genomeBrowserImage) {
        TreeSet<BrowserLink> locations = new TreeSet<>();
        BrowserLink location = new BrowserLink();
        location.setUrl(genomeBrowserImage.getFullLinkUrl());
        location.setName("ZFIN");
        location.setOrder(0);
        locations.add(location);
        return locations;
    }
}



