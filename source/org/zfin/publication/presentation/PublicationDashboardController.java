package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.repository.PublicationRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/publication")
public class PublicationDashboardController {

    private final static Logger LOG = Logger.getLogger(PublicationDashboardController.class);

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    CurationDTOConversionService converter;

    @RequestMapping("/curating-bin")
    public String showReadyForCurationBin(Model model) {
        return showBin(model,
                publicationRepository.getPublicationStatusByName("Ready for Curation"),
                publicationRepository.getPublicationStatusByName("Curating"),
                "publication/curating-bin.page");
    }

    @RequestMapping("/indexing-bin")
    public String showReadyForIndexingBin(Model model) {
        return showBin(model,
                publicationRepository.getPublicationStatusByName("Ready for Indexing"),
                publicationRepository.getPublicationStatusByName("Indexing"),
                "publication/indexing-bin.page");
    }

    private String showBin(Model model, PublicationTrackingStatus current, PublicationTrackingStatus next, String view) {
        model.addAttribute("currentStatus", current);
        model.addAttribute("nextStatus", next);
        model.addAttribute("currentUser", ProfileService.getCurrentSecurityUser());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, current.getName() + " Publications");
        return view;
    }

    @RequestMapping("/dashboard")
    public String showUserDashboad(Model model) {
        model.addAttribute("currentUser", ProfileService.getCurrentSecurityUser());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Dashboard");
        return "publication/dashboard.page";
    }

    @ResponseBody
    @RequestMapping(value = "/search-status", method = RequestMethod.GET)
    public List<DashboardPublicationBean> getListOfPubsInBin(@RequestParam(required = false) Long status,
                                                             @RequestParam(required = false) Long location,
                                                             @RequestParam(required = false) String owner,
                                                             @RequestParam(required = false, defaultValue = "0") int offset,
                                                             @RequestParam(required = false, defaultValue = "50") int count,
                                                             @RequestParam(required = false) String sort) {
        List<PublicationTrackingHistory> histories = publicationRepository.getPublicationsByStatus(status, location, owner, count, offset, sort);

        List<DashboardPublicationBean> beans = new ArrayList<>(histories.size());
        for (PublicationTrackingHistory history : histories) {
            beans.add(converter.toDashboardPublicationBean(history));
        }
        return beans;
    }

}
