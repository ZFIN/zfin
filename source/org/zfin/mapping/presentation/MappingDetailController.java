package org.zfin.mapping.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gbrowse.GBrowseService;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.*;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Controller for the mapping module
 * Mapping Detail
 * Panel Detail
 */
@Controller
@RequestMapping(value = "/mapping")
public class MappingDetailController {

    @RequestMapping("/panel-detail/{panelID}")
    protected String showPanelDetail(@PathVariable String panelID,
                                     Model model) throws Exception {
        Panel panel = getLinkageRepository().getPanel(panelID);
        model.addAttribute("panel", panel);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Mapping Panel: " + panel.getName());
        model.addAttribute("citationCount", getPublicationRepository().getNumberDirectPublications(panelID));
        return "mapping/panel-detail.page";
    }

    @RequestMapping("/all-panels")
    protected String showAllPanels(Model model) throws Exception {

        List<MeioticPanel> meioticPanels = getLinkageRepository().getMeioticPanels();
        List<RadiationPanel> radiationPanels = getLinkageRepository().getRadiationPanels();
        model.addAttribute(meioticPanels);
        model.addAttribute(radiationPanels);
        return "mapping/all-panels.page";
    }

    @RequestMapping("/show-scoring")
    protected String showScoring(@RequestParam(value = "markerID", required = false) String markerID,
                                 @RequestParam(value = "panelID", required = false) String panelID,
                                 @RequestParam(value = "lg", required = false) String lg,
                                 Model model) throws Exception {

        Marker marker = null;
        Feature feature = null;
        boolean isFeature = false;
        if (markerID != null) {
            marker = getMarkerRepository().getMarkerByID(markerID);
            if (marker == null) {
                feature = getFeatureRepository().getFeatureByID(markerID);
                if (feature != null)
                    isFeature = true;
            }
        }
        Panel panel = getLinkageRepository().getPanel(panelID);
        if (isFeature) {
            List<MappedMarker> mappedMarkerList = getLinkageRepository().getMappedMarkers(panel, feature, lg);
            Collections.sort(mappedMarkerList, new ScoringSort());
            model.addAttribute("mappedMarkerList", mappedMarkerList);
        } else {
            List<MappedMarker> mappedMarkerList = getLinkageRepository().getMappedMarkers(panel, marker, lg);
            Collections.sort(mappedMarkerList, new ScoringSort());
            model.addAttribute("mappedMarkerList", mappedMarkerList);
        }
        if (marker == null)
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Scoring: " + panel.getName());
        else
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Scoring: " + marker.getAbbreviation() + " on " + panel.getName());
        return "mapping/show-scoring.page";
    }

    @RequestMapping("/summary/{markerID}")
    protected String showMappingSummary(@PathVariable String markerID,
                                        Model model) throws Exception {

        markerID = getMarkerIDFromID(markerID);

        // if feature then find associated marker
        if (ActiveData.validateID(markerID).equals(ActiveData.Type.ALT)) {
            Feature feature = getFeatureRepository().getFeatureByID(markerID);
            Marker marker = getMarkerRepository().getMarkerByFeature(feature);
            markerID = marker.getZdbID();
        }

        Marker marker = markerRepository.getMarkerByID(markerID);
        model.addAttribute("marker", marker);

        return "mapping-summary.simple-page";
    }

    @ModelAttribute("linkage")
    public Linkage getFormBean() {
        return new Linkage();
    }

    @RequestMapping("/linkage/{linkageID}")
    protected String showLinkageInfo(@PathVariable String linkageID,
                                     Model model) throws Exception {

        if (linkageID == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No linkageID found");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Linkage linkage = getLinkageRepository().getLinkage(linkageID);
        model.addAttribute("linkage", linkage);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Linkage: " + linkageID);
        return "mapping/linkage.page";
    }

    @RequestMapping(value = "/linkage/edit-comment", method = RequestMethod.POST)
    public String doSearch(Model model,
                           @ModelAttribute("linkage") Linkage formLinkage,
                           BindingResult result
    ) throws Exception {

        if (formLinkage == null || StringUtils.isEmpty(formLinkage.getZdbID())) {
            model.addAttribute(LookupStrings.ZDB_ID, "No linkageID found");
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        String linkageID = formLinkage.getZdbID();
        Linkage linkage = getLinkageRepository().getLinkage(linkageID);
        if (linkage == null) {
            model.addAttribute(LookupStrings.ZDB_ID, "No linkage found for " + linkageID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        try {
            HibernateUtil.createTransaction();
            getLinkageRepository().saveLinkageComment(linkage, formLinkage.getComments());
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            String errorMessage = "Could not save comment";
            model.addAttribute("error", errorMessage);
            logger.error(errorMessage, e);
        }
        return "redirect:" + linkageID;

    }


    @RequestMapping("/detail/{mutantID}")
    protected String showMappingDetail(@PathVariable String mutantID,
                                       Model model) throws Exception {
        String markerID = getMarkerIDFromID(mutantID);

        // if feature then find associated marker
        boolean isFeature = false;
        Feature feature = null;
        if (ActiveData.validateID(mutantID).equals(ActiveData.Type.ALT)) {
            feature = getFeatureRepository().getFeatureByID(mutantID);
            if (feature == null) {
                model.addAttribute(LookupStrings.ZDB_ID, "No feature found");
                return LookupStrings.RECORD_NOT_FOUND_PAGE;
            }
            isFeature = true;
            model.addAttribute("feature", feature);
            model.addAttribute("isFeature", true);
            model.addAttribute("singleton", getLinkageRepository().getSingletonLinkage(feature));
            List<SingletonLinkage> singletonLinkage = getLinkageRepository().getSingletonLinkage(feature);
            if (singletonLinkage != null) {
                putInfoOnModel(model, "singletonFeatureList", singletonLinkage);
                model.addAttribute("otherMappingDetail", true);
            }

            setOtherMappingInfoForFeature(model, feature);
            Marker marker = getMarkerRepository().getMarkerByFeature(feature);
            if (marker == null) {
                model.addAttribute("pureFeature", true);
                return "mapping/mapping-detail-pure-feature.page";
            }
            markerID = marker.getZdbID();
        }

        if (markerID == null || markerID.isEmpty() || !markerRepository.markerExistsForZdbID(markerID)) {
            model.addAttribute(LookupStrings.ZDB_ID, markerID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Marker marker = markerRepository.getMarkerByID(markerID);


        // genetic mapping Panels
        List<MappedMarker> mappedMarkers = getLinkageRepository().getMappedMarkers(marker);

        List<LinkageMember> linkageList = getLinkageRepository().getLinkageMemberForMarker(marker);
        Collections.sort(linkageList);
        model.addAttribute("mappedMarkers", mappedMarkers);
        model.addAttribute("linkageMemberList", linkageList);
        List<Marker> markerList = getLinkageRepository().getMappedClonesContainingGene(marker);
        model.addAttribute("mappedClones", markerList);

        setOtherMappingInfo(model, marker, feature);

        model.addAttribute("marker", marker);
        if (isFeature) {
            model.addAttribute("gBrowseMarker", GBrowseService.getGbrowseTrackingGene(feature));
        } else
            model.addAttribute("gBrowseMarker", GBrowseService.getGbrowseTrackingGene(marker));
        model.addAttribute("locations", MappingService.getGenomeBrowserLocations(marker));
        if (marker.isInTypeGroup(Marker.TypeGroup.CLONE)) {
            List<MarkerGenomeLocation> genomeMarkerLocationList = getLinkageRepository().getGenomeLocation(marker);
            model.addAttribute("locations", genomeMarkerLocationList);
            model.addAttribute("isClone", true);
        }
        if (isFeature)
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.MAPPING.getTitleString() + feature.getAbbreviation());
        else
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.MAPPING.getTitleString() + marker.getAbbreviation());

        List<MarkerGenomeLocation> genomeLocation = getLinkageRepository().getGenomeLocation(marker);
        Collections.sort(genomeLocation);
        model.addAttribute("markerGenomeLocations", genomeLocation);
        if (isFeature)
            model.addAttribute("featureGenomeLocations", getLinkageRepository().getGenomeLocation(feature));
        return "mapping/mapping-detail.page";
    }

    private void setOtherMappingInfo(Model model, Marker marker, Feature feature) {
        List<Marker> markersEncodedByMarker = getLinkageRepository().getMarkersEncodedByMarker(marker);
        List<Marker> markersContainedIn = getLinkageRepository().getMarkersContainedIn(marker);
        List<Marker> estContainingSNP = getLinkageRepository().getESTContainingSnp(marker);
        List<Marker> geneContainingSNP = getLinkageRepository().getGeneContainingSnp(marker);
        Set<Boolean> hasOtherMappingInfo = new HashSet<>(2);
        hasOtherMappingInfo.add(putInfoOnModel(model, "markersEncodedByMarkers", markersEncodedByMarker));
        hasOtherMappingInfo.add(putInfoOnModel(model, "markersContainedIn", markersContainedIn));
        hasOtherMappingInfo.add(putInfoOnModel(model, "estContainingSNP", estContainingSNP));
        hasOtherMappingInfo.add(putInfoOnModel(model, "geneContainingSNP", geneContainingSNP));
        List<PrimerSet> primerSetList = getLinkageRepository().getPrimerSetList(marker);
        hasOtherMappingInfo.add(putInfoOnModel(model, "primerSetList", primerSetList));
        setOtherMappingInfoForFeature(model, feature);
        hasOtherMappingInfo.add(retrieveAssociatedFeatureMappingData(model, marker));
        hasOtherMappingInfo.add(putInfoOnModel(model, "singletonList", getLinkageRepository().getSingletonLinkage(marker)));

        model.addAttribute("otherMappingDetail", hasOtherMappingInfo.contains(true));
    }

    private void setOtherMappingInfoForFeature(Model model, Feature feature) {
        if (feature != null) {
            List<MappedMarker> mappedMarkers = getLinkageRepository().getMappedMarkers(feature);
            model.addAttribute("mappedFeatures", mappedMarkers);
            List<Marker> presentMarkerList = FeatureService.getPresentMarkerList(feature, FeatureMarkerRelationshipTypeEnum.MARKERS_PRESENT);
            model.addAttribute("markerPresentList", presentMarkerList);
            List<Marker> missingMarkerList = FeatureService.getPresentMarkerList(feature, FeatureMarkerRelationshipTypeEnum.MARKERS_MISSING);
            model.addAttribute("markerMissingList", missingMarkerList);
            boolean attributeValue = CollectionUtils.isNotEmpty(presentMarkerList) || CollectionUtils.isNotEmpty(missingMarkerList);
            model.addAttribute("deletionMarkersPresent", attributeValue);
            model.addAttribute("otherMappingDetail", CollectionUtils.isNotEmpty(mappedMarkers) || attributeValue);
        }
    }

    private boolean putInfoOnModel(Model model, String name, Collection collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
            model.addAttribute(name, collection);
            return true;
        }
        return false;
    }

    private boolean retrieveAssociatedFeatureMappingData(Model model, Marker marker) {
        List<Feature> featureList = getFeatureRepository().getFeaturesByMarker(marker);
        List<List<MappedMarker>> mappedFeatureMarkers = null;
        List<List<LinkageMember>> linkageFeatureList = null;
        if (CollectionUtils.isNotEmpty(featureList)) {
            mappedFeatureMarkers = new ArrayList<>(featureList.size());
            linkageFeatureList = new ArrayList<>(featureList.size());
            for (Feature feat : featureList) {
                List<MappedMarker> mappedMarkers1 = getLinkageRepository().getMappedMarkers(feat);
                if (CollectionUtils.isNotEmpty(mappedMarkers1))
                    mappedFeatureMarkers.add(mappedMarkers1);
                List<LinkageMember> linkageFeatures = getLinkageRepository().getLinkagesForFeature(feat);
                if (CollectionUtils.isNotEmpty(linkageFeatures))
                    linkageFeatureList.add(linkageFeatures);
            }
        }
        model.addAttribute("mappedFeatureMarkers", mappedFeatureMarkers);
        model.addAttribute("linkageFeatureList", linkageFeatureList);
        if (CollectionUtils.isNotEmpty(mappedFeatureMarkers))
            return true;
        if (CollectionUtils.isNotEmpty(linkageFeatureList))
            return true;
        return false;
    }

    private String getMarkerIDFromID(String markerID) {
        if (markerID.startsWith("ZDB-")) {
            if (markerRepository.markerExistsForZdbID(markerID)) {
                return markerID;
            } else {
                String replacedZdbID = infrastructureRepository.getReplacedZdbID(markerID);
                logger.debug("trying to find a replaced zdbID for: " + markerID);
                if (replacedZdbID != null) {
                    if (markerRepository.markerExistsForZdbID(replacedZdbID)) {
                        logger.debug("found a replaced zdbID for: " + markerID + "->" + replacedZdbID);
                        return replacedZdbID;
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Autowired
    private MarkerRepository markerRepository;

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    private Logger logger = Logger.getLogger(MappingDetailController.class);

    class ScoringSort implements Comparator<MappedMarker> {

        @Override
        public int compare(MappedMarker o1, MappedMarker o2) {
            if (!o1.getLgLocation().equals(o2.getLgLocation()))
                return o1.getLgLocation().compareTo(o2.getLgLocation());
            return o1.getEntityAbbreviation().compareTo(o2.getEntityAbbreviation());
        }
    }
}
