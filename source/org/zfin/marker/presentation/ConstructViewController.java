package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
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
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.mutant.repository.MutantRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/marker")
public class ConstructViewController {

    private Logger logger = Logger.getLogger(ConstructViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private MutantRepository mutantRepository;

    @RequestMapping(value = "/construct/view/{zdbID}")
    public String getGeneView(
            Model model
            , @PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        ConstructBean markerBean = new ConstructBean();

        logger.info("zdbID: " + zdbID);
        Marker construct = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + construct);
        markerBean.setMarker(construct);

        MarkerService.createDefaultViewForMarker(markerBean);


        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(markerRepository.getRelatedMarkerOrderDisplayForTypes(
                construct, true
                , MarkerRelationship.Type.PROMOTER_OF
                , MarkerRelationship.Type.CODING_SEQUENCE_OF
                , MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION
        ));

        for (MarkerRelationshipPresentation markerRelationshipPresentation : cloneRelationships) {
            if (markerRelationshipPresentation.getRelationshipType().equals("Has Promoter")) {
                markerRelationshipPresentation.setArbitraryOrder(1);
                markerRelationshipPresentation.setMappedMarkerRelationshipType("Regulatory Regions:");
            } else if (markerRelationshipPresentation.getRelationshipType().equals("Has Coding Sequence")) {
                markerRelationshipPresentation.setArbitraryOrder(2);
                markerRelationshipPresentation.setMappedMarkerRelationshipType("Coding Sequences:");
            } else if (markerRelationshipPresentation.getRelationshipType().equals("Contains")) {
                markerRelationshipPresentation.setArbitraryOrder(3);
                markerRelationshipPresentation.setMappedMarkerRelationshipType("Contains:");
            }
        }
        Collections.sort(cloneRelationships, new Comparator<MarkerRelationshipPresentation>() {
            @Override
            public int compare(MarkerRelationshipPresentation mr1, MarkerRelationshipPresentation mr2) {
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
            }
        });
        markerBean.setMarkerRelationshipPresentationList(cloneRelationships);

        // Transgenics that utilize the construct
        List<Feature> features = featureRepository.getFeaturesByConstruct(construct);
        markerBean.setTransgenics(features);

        List<GenotypeFishResult> allFish = new ArrayList<>();
        for (Feature feature : features) {
            List<Genotype> genotypes = mutantRepository.getGenotypesByFeature(feature);
            for (Genotype genotype : genotypes) {
                List<GenotypeFishResult> fishSummaryList = FishService.getFishExperiementSummaryForGenotype(genotype);
                for (GenotypeFishResult fishSummary : fishSummaryList) {
                    if (fishSummary.getFish().getStrList().isEmpty()) {
                        allFish.add(fishSummary);
                    }
                }
            }
        }
        markerBean.setFish(allFish);

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, markerBean.getMarkerTypeDisplay() + ": " + construct.getName());

        return "marker/construct-view.page";
    }
}