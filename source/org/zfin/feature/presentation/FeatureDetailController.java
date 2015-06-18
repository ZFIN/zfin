package org.zfin.feature.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gbrowse.GBrowseTrack;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.FeatureGenomeLocation;
import org.zfin.mapping.GenomeLocation;
import org.zfin.mapping.MarkerGenomeLocation;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeDisplay;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.mutant.GenotypeService;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.mutant.presentation.GenotypeInformation;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;


@Controller
@RequestMapping("/feature")
public class FeatureDetailController {
    private static final Logger LOG = Logger.getLogger(FeatureDetailController.class);

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private MutantRepository mutantRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private LinkageRepository linkageRepository;

    @RequestMapping(value = "view/{zdbID}")
    protected String getFeatureDetail(@PathVariable String zdbID, Model model) {
        LOG.info("Start Feature Detail Controller");
        Feature feature = featureRepository.getFeatureByID(zdbID);
        if (null == feature) {
            String ftr = featureRepository.getFeatureByIDInTrackingTable(zdbID);
            if (ftr != null) {
                if (zdbID.startsWith("ZDB-ALT-120130") || (zdbID.startsWith("ZDB-ALT-120806"))) {
                    return "redirect:/ZDB-PUB-121121-2";
                } else {
                    //if (!zdbID.startsWith("ZDB-ALT-120130")||!zdbID.startsWith("ZDB-ALT-120806")){
                    String repldFtr = infrastructureRepository.getReplacedZdbID(zdbID);
                    if (repldFtr != null) {
                        feature = featureRepository.getFeatureByID(repldFtr);

                    } else {
                        model.addAttribute(LookupStrings.ZDB_ID, zdbID);
                        return LookupStrings.RECORD_NOT_FOUND_PAGE;
                    }
                }

            } else {
                model.addAttribute(LookupStrings.ZDB_ID, zdbID);
                return LookupStrings.RECORD_NOT_FOUND_PAGE;
            }

        }


        FeatureBean form = new FeatureBean();
        form.setZdbID(zdbID);


        form.setFeature(feature);

        Set<FeatureMarkerRelationship> markerRelationships = FeatureService.getSortedMarkerRelationships(feature);
        Collection<FeatureGenomeLocation> locations = FeatureService.getFeatureGenomeLocations(feature, GenomeLocation.Source.ZFIN);
        if (CollectionUtils.isNotEmpty(locations)) {
            // gbrowse has a location for this feature. if there is a feature marker relationship AND we know where
            // that marker is, show the feature in the context of the marker. Otherwise just show the feature with
            // some appropriate amount of padding.
            GBrowseImage.GBrowseImageBuilder imageBuilder = GBrowseImage.builder();
            FeatureGenomeLocation featureLocation = locations.iterator().next();
            if (markerRelationships.size() == 1) {
                Marker related = markerRelationships.iterator().next().getMarker();
                List<MarkerGenomeLocation> markerLocations = linkageRepository.getGenomeLocation(related, GenomeLocation.Source.ZFIN);
                if (CollectionUtils.isNotEmpty(markerLocations)) {
                    imageBuilder.landmark(markerLocations.get(0))
                            .highlight(feature)
                            .withPadding(0.1);
                } else {
                    imageBuilder.landmark(featureLocation)
                            .highlight(feature)
                            .withPadding(10000);
                }
            } else {
                imageBuilder.landmark(featureLocation)
                        .highlight(feature)
                        .withPadding(10000);
            }
            String subSource = featureLocation.getDetailedSource();
            if (subSource != null) {
                if (subSource.equals("BurgessLin")) {
                    imageBuilder.tracks(GBrowseTrack.GENES, GBrowseTrack.INSERTION, GBrowseTrack.TRANSCRIPTS);
                } else if (subSource.equals("ZMP")) {
                    imageBuilder.tracks(GBrowseTrack.GENES, GBrowseTrack.ZMP, GBrowseTrack.TRANSCRIPTS);
                }
            }
            form.setgBrowseImage(imageBuilder.build());
        }

        form.setSortedConstructRelationships(FeatureService.getSortedConstructRelationships(feature));
        form.setCreatedByRelationship(FeatureService.getCreatedByRelationship(feature));
        form.setFeatureTypeAttributions(FeatureService.getFeatureTypeAttributions(feature));
        form.setSinglePublication(FeatureService.getSinglePublication(feature));
        form.setFeatureMap(FeatureService.getFeatureMap(feature));
        form.setFeatureLocations(FeatureService.getFeatureLocations(feature));
        LOG.debug("got to summary page bit");

        form.setSummaryPageDbLinks(FeatureService.getSummaryDbLinks(feature));
        form.setGenbankDbLinks(FeatureService.getGenbankDbLinks(feature));

        LOG.debug("genbank link count " + form.getGenbankDbLinks().size());
        retrieveSortedGenotypeData(feature, form);
        retrievePubData(feature, form);

        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, feature.getName());

        return "feature/feature-detail.page";
    }

    @RequestMapping(value = "/feature/view/{zdbID}")
    public String retrieveFeatureDetail(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        return getFeatureDetail(zdbID, model);
    }

    private void retrieveSortedGenotypeData(Feature feature, FeatureBean form) {
        Set<GenotypeFeature> genotypeFeatures = feature.getGenotypeFeatures();
        List<GenotypeDisplay> genotypeDisplays = new ArrayList<>(genotypeFeatures.size());
        GenotypeDisplay genotypeDisplay;
        for (GenotypeFeature genotypeFeature : genotypeFeatures) {
            genotypeDisplay = new GenotypeDisplay();
            genotypeDisplay.setGenotype(genotypeFeature.getGenotype());
            genotypeDisplay.setDadZygosity(genotypeFeature.getDadZygosity());
            genotypeDisplay.setMomZygosity(genotypeFeature.getMomZygosity());
            genotypeDisplays.add(genotypeDisplay);
        }
        Collections.sort(genotypeDisplays);
        form.setGenotypeDisplays(genotypeDisplays);
     //   List<GenotypeInformation> genotypeInfo = GenotypeService.getGenotypeInfo(genotypes);
     //   Collections.sort(genotypeInfo);
     //   form.setFeatgenoStats(genotypeInfo);
     //   List<GenoExpStatistics> genoexpStats = GenotypeService.getGenotypeExpStats(genotypes, fr);
     //   form.setGenoexpStats(genoexpStats);
    }

    private void retrievePubData(Feature fr, FeatureBean form) {
        form.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(fr.getZdbID()));
    }


}

