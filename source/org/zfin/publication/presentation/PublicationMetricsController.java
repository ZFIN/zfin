package org.zfin.publication.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.repository.PublicationRepository;

@Controller
@RequestMapping("/publication")
public class PublicationMetricsController {

    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public String showSearchForm(Model model) {
        model.addAttribute("results", publicationRepository.getMetricsByPETDate());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication Metrics");
        return "publication/metrics.page";
    }
}
