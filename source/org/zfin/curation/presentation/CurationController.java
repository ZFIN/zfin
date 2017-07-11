package org.zfin.curation.presentation;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.curation.ui.CurationModuleType;
import org.zfin.gwt.curation.ui.CurationService;
import org.zfin.gwt.curation.ui.PublicationNotFoundException;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.infrastructure.EntityID;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * This Controller is used to facilitate the curation tabs.
 */
@Controller
@RequestMapping("/curation")
@SessionAttributes({"currentTab"})
public class CurationController implements CurationService {

    @Autowired
    private ExpressionRepository expRepository;
    @Autowired
    private PublicationRepository pubRepository;

    private final static Logger LOG = RootLogger.getLogger(CurationController.class);

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
        return getListOfMarkerDtos(markers);
    }

    private List<String> assayDtos;

    @RequestMapping(value = "/assays", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<String> getAssays() {
        if (assayDtos != null)
            return assayDtos;
        InfrastructureRepository infra = RepositoryFactory.getInfrastructureRepository();
        List<ExpressionAssay> assays = infra.getAllAssays();
        assayDtos = new ArrayList<>();
        for (ExpressionAssay assay : assays)
            assayDtos.add(assay.getName());
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


    @ModelAttribute("currentTab")
    public String getCurrentTab() {
        return CurationModuleType.FEATURE_CURATION.getValue();
    }

    @RequestMapping("/{pubID}")
    protected String curationPage(@PathVariable String pubID,
                                  @ModelAttribute("currentTab") String currentTab,
                                  Model model) throws Exception {
        Publication publication = getPublicationRepository().getPublication(pubID);
        if (publication == null) {
            return "record-not-found.page";
        }
        model.addAttribute("publication", publication);
        model.addAttribute("curationTabs", CurationModuleType.values());
        model.addAttribute("currentTab", currentTab);
        model.addAttribute("hasCorrespondence", PublicationService.hasCorrespondence(publication));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Curate: " + publication.getTitle());
        return "curation/curation.page";
    }

    @ResponseBody
    @RequestMapping("/currentTab/{currentTab}")
    protected String setCurrentTab(@PathVariable String currentTab,
                                   Model model) throws Exception {
        if (currentTab == null)
            return "error: no tab name provided";
        if (CurationModuleType.getType(currentTab) == null)
            return "error: no tab name found by name: " + currentTab;
        String tabName = CurationModuleType.getType(currentTab).getValue();
        model.addAttribute("currentTab", tabName);
        return "success";
    }


}