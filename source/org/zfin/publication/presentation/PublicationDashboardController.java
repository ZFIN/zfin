package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.repository.PublicationRepository;

@Controller
@RequestMapping("/publication")
public class PublicationDashboardController {

    public enum Page {
        PROCESSING_BIN("Processing Bin", "publication/processing-bin", "/action/publication/processing-bin"),
        INDEXING_BIN("Indexing Bin", "publication/indexing-bin", "/action/publication/indexing-bin"),
        CURATING_BIN("Curation Bins", "publication/curating-bin", "/action/publication/curating-bin"),
        DASHBOARD("My Dashboard", "publication/dashboard", "/action/publication/dashboard");

        private String title;
        private String view;
        private String url;

        Page(String title, String view, String url) {
            this.title = title;
            this.view = view;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getView() {
            return view;
        }

        public String getUrl() {
            return url;
        }
    }

    private final static Logger LOG = LogManager.getLogger(PublicationDashboardController.class);

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    CurationDTOConversionService converter;

    @ModelAttribute("pages")
    public Page[] getDashboardPages() {
        return Page.values();
    }

    @RequestMapping("/processing-bin")
    public String showReadyForProcessingBin(Model model) {
        return showBin(model,
                publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.READY_FOR_PROCESSING),
                publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.PROCESSING),
                Page.PROCESSING_BIN);
    }

    @RequestMapping("/curating-bin")
    public String showReadyForCurationBin(Model model) {
        return showBin(model,
                publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.READY_FOR_CURATION),
                publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.CURATING),
                Page.CURATING_BIN);
    }

    @RequestMapping("/indexing-bin")
    public String showReadyForIndexingBin(Model model) {
        return showBin(model,
                publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.READY_FOR_INDEXING),
                publicationRepository.getPublicationStatusByName(PublicationTrackingStatus.Name.INDEXING),
                Page.INDEXING_BIN);
    }

    private String showBin(Model model, PublicationTrackingStatus current, PublicationTrackingStatus next, Page page) {
        model.addAttribute("currentStatus", current);
        model.addAttribute("nextStatus", next);
        model.addAttribute("currentPage", page);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, page.title);
        return page.view;
    }

    @RequestMapping("/dashboard")
    public String showUserDashboad(Model model) {
        model.addAttribute("currentPage", Page.DASHBOARD);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Page.DASHBOARD.title);
        return Page.DASHBOARD.view;
    }

    @ResponseBody
    @RequestMapping(value = "/search-status", method = RequestMethod.GET)
    @JsonView(View.API.class)
    public DashboardPublicationList getListOfPubsInBin(@RequestParam(required = false) Long status,
                                                                         @RequestParam(required = false) Long location,
                                                                         @RequestParam(required = false) String owner,
                                                                         @RequestParam(required = false, defaultValue = "0") int offset,
                                                                         @RequestParam(required = false, defaultValue = "50") int count,
                                                                         @RequestParam(required = false) String sort) {
        return publicationRepository.getPublicationsByStatus(status, location, owner, count, offset, sort);
    }
}
