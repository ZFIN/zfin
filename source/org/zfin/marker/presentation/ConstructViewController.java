package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeService;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.mutant.repository.MutantRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/marker")
public class ConstructViewController {

    private Logger logger = LogManager.getLogger(ConstructViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private MutantRepository mutantRepository;

    @Autowired
    private MarkerService markerService;

    @RequestMapping(value = "/construct/view/{zdbID}")
    public String getNewGeneView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        // set base bean
        ConstructBean markerBean = new ConstructBean();

        zdbID = markerService.getActiveMarkerID(zdbID);
        logger.debug("zdbID: " + zdbID);
        Marker construct = markerRepository.getMarkerByID(zdbID);
        logger.debug("gene: " + construct);
        markerBean.setMarker(construct);

        MarkerService.createDefaultViewForMarker(markerBean);

        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                construct, true
                , MarkerRelationship.Type.PROMOTER_OF
                , MarkerRelationship.Type.CODING_SEQUENCE_OF
                , MarkerRelationship.Type.CONTAINS_REGION
        ));

        List<MarkerRelationshipPresentation> regulatoryRegionPresentations = new ArrayList<>();
        List<MarkerRelationshipPresentation> codingSequencePresentations = new ArrayList<>();

        List<MarkerRelationshipPresentation> containsSequencePresentations = new ArrayList<>();

        for (MarkerRelationshipPresentation markerRelationshipPresentation : cloneRelationships) {
            if (markerRelationshipPresentation.getRelationshipType().equals("Has Promoter")) {
                markerRelationshipPresentation.setArbitraryOrder(1);
                markerRelationshipPresentation.setMappedMarkerRelationshipType("Regulatory Regions:");
                regulatoryRegionPresentations.add(markerRelationshipPresentation);
            } else if (markerRelationshipPresentation.getRelationshipType().equals("Has Coding Sequence")) {
                markerRelationshipPresentation.setArbitraryOrder(2);
                markerRelationshipPresentation.setMappedMarkerRelationshipType("Coding Sequences:");
                codingSequencePresentations.add(markerRelationshipPresentation);
            } else if (markerRelationshipPresentation.getRelationshipType().equals("Contains")) {
                markerRelationshipPresentation.setArbitraryOrder(3);
                markerRelationshipPresentation.setMappedMarkerRelationshipType("Contains:");
                containsSequencePresentations.add(markerRelationshipPresentation);
            }
        }
        markerBean.setCodingSequencePresentations(codingSequencePresentations);
        markerBean.setRegulatoryRegionPresentations(regulatoryRegionPresentations);
        markerBean.setContainsSequencePresentations(containsSequencePresentations);
        sortRelationships(cloneRelationships);
        markerBean.setMarkerRelationshipPresentationList(cloneRelationships);

        // Transgenics that utilize the construct
        int numFeatures = featureRepository.getNumberOfFeaturesForConstruct(construct);
        model.addAttribute("numberOfFeatures", numFeatures);
        List<Feature> features = featureRepository.getFeaturesByConstruct(construct);
            markerBean.setTransgenics(features);

            List<GenotypeFishResult> allFish = new ArrayList<>(new LinkedHashSet<>());
            allFish.clear();
            for (Feature feature : features) {

                List<Genotype> genotypes = mutantRepository.getGenotypesByFeature(feature);

                for (Genotype genotype : genotypes) {
                    List<GenotypeFishResult> fishSummaryList = FishService.getFishExperimentSummaryForGenotype(genotype);
                    for (GenotypeFishResult fishSummary : fishSummaryList) {
                        if (fishSummary.getFish().getStrList().isEmpty()) {
                            fishSummary.setAffectedMarkers(GenotypeService.getAffectedMarker(genotype));
                        }
                        if (!allFish.contains(fishSummary)) {
                            allFish.add(fishSummary);
                        }
                    }
                }
            allFish = allFish.stream().distinct().collect(Collectors.toList());
            Collections.sort(allFish);
            markerBean.setFish(allFish);
        }

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerBean.getMarkerTypeDisplay() + ": " + construct.getName());


        return "marker/construct/construct-view";
    }

    private void sortRelationships(List<MarkerRelationshipPresentation> cloneRelationships) {
        cloneRelationships.sort((mr1, mr2) -> {
            if (mr1.getArbitraryOrder() == null && mr2.getArbitraryOrder() != null) {
                return -1;
            }
            if (mr1.getArbitraryOrder() != null && mr2.getArbitraryOrder() == null) {
                return 1;
            }

            int compare;

            if (mr1.getArbitraryOrder() != null && mr2.getArbitraryOrder() != null) {
                compare = mr1.getArbitraryOrder().compareTo(mr2.getArbitraryOrder());
                if (compare != 0) {
                    return compare;
                }
            }

            compare = mr1.getMarkerType().compareTo(mr2.getMarkerType());
            if (compare != 0) {
                return compare;
            }

            return mr1.getAbbreviationOrder().compareTo(mr2.getAbbreviationOrder());
        });
    }

}