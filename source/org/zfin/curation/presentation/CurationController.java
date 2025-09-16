package org.zfin.curation.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionAssay;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.curation.ui.CurationModuleType;
import org.zfin.gwt.curation.ui.CurationService;
import org.zfin.gwt.curation.ui.PublicationNotFoundException;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.EntityID;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Controller is used to facilitate the curation tabs.
 */
@Controller
@RequestMapping("/curation")
@SessionAttributes({"currentTab"})
public class CurationController implements CurationService {

    @Autowired
    private PublicationRepository pubRepository;
    @Autowired
    private PublicationService publicationService;
    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping(value = "/{publicationID}/antibodies", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<MarkerDTO> getAntibodies(@PathVariable String publicationID) {
        List<Antibody> antibodies = pubRepository.getAntibodiesByPublication(publicationID);
        return getListOfMarkerDtos(antibodies);
    }

    @RequestMapping(value = "/{publicationID}/genes", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<MarkerDTO> getGenes(@PathVariable String publicationID) throws PublicationNotFoundException {

        Publication publication = pubRepository.getPublication(publicationID);
        if (publication == null) {
            throw new PublicationNotFoundException(publicationID);
        }

        List<Marker> markers = pubRepository.getGenesByPublication(publicationID);
        return getListOfMarkerDtosForMarker(markers);
    }

    private List<String> assayDtos;

    @RequestMapping(value = "/assays", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<String> getAssays() {
        if (assayDtos != null) {
            return assayDtos;
        }
        InfrastructureRepository infra = RepositoryFactory.getInfrastructureRepository();
        List<ExpressionAssay> assays = infra.getAllAssays();
        assayDtos = new ArrayList<>();
        for (ExpressionAssay assay : assays) {
            assayDtos.add(assay.getName());
        }
        return assayDtos;
    }

    public List<MarkerDTO> getListOfMarkerDtos(List<? extends EntityID> entityIDS) {
        List<MarkerDTO> markers = new ArrayList<>();
        for (EntityID antibody : entityIDS) {
            MarkerDTO env = new MarkerDTO();
            env.setName(antibody.getAbbreviation());
            env.setZdbID(antibody.getZdbID());
            markers.add(env);
        }
        return markers;
    }

    public List<MarkerDTO> getListOfMarkerDtosForMarker(List<Marker> markerList) {
        List<MarkerDTO> markers = new ArrayList<>();
        for (Marker marker : markerList) {
            markers.add(DTOConversionService.convertToMarkerDTO(marker));
        }
        return markers;
    }


    @ModelAttribute("currentTab")
    public String getCurrentTab() {
        return CurationModuleType.FEATURE_CURATION.getValue();
    }

    @RequestMapping("/{pubID}")
    protected String curationPage(@PathVariable String pubID,
                                  @ModelAttribute("currentTab") String currentTab,
                                  Model model) {
        Publication publication = publicationRepository.getPublication(pubID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute("publication", publication);
        model.addAttribute("curationTabs", enabledCurationTabs(CurationModuleType.allCurationTabs()));
        model.addAttribute("currentTab", currentTab);
        model.addAttribute("curatingStatus", publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.CURATING));
        model.addAttribute("hasCorrespondence", publicationService.hasCorrespondence(publication));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Curate: " + publication.getTitle());
        return "curation/curation";
    }

    /**
     * Filter out curation tabs if we want to hide them
     * @param allCurationTabs
     * @return
     */
    private List<CurationModuleType> enabledCurationTabs(List<CurationModuleType> allCurationTabs) {
        return allCurationTabs;
    }

    @ResponseBody
    @RequestMapping("/currentTab/{currentTab}")
    protected String setCurrentTab(@PathVariable String currentTab,
                                   Model model) {
        if (currentTab == null) {
            return "error: no tab name provided";
        }
        if (CurationModuleType.getType(currentTab) == null) {
            return "error: no tab name found by name: " + currentTab;
        }
        String tabName = CurationModuleType.getType(currentTab).getValue();
        model.addAttribute("currentTab", tabName);
        return "success";
    }


}