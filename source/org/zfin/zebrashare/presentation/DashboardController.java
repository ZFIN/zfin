package org.zfin.zebrashare.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/zebrashare")
public class DashboardController {

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String viewDashboard(Model model) {
        Person person = ProfileService.getCurrentSecurityUser();
        List<Publication> publications = zebrashareRepository.getZebraSharePublicationsForPerson(person);
        Map<String, List<Feature>> features = publications.stream()
                .collect(Collectors.toMap(
                        Publication::getZdbID,
                        p -> featureRepository.getFeaturesByPublication(p.getZdbID())
                ));
        model.addAttribute("publications", publications);
        model.addAttribute("features", features);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "ZebraShare Submissions");
        return "zebrashare/dashboard.page";
    }

}
